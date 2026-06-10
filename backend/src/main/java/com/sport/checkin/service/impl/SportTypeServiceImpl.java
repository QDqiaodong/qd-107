package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.mapper.SportTypeMapper;
import com.sport.checkin.service.SportTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SportTypeServiceImpl extends ServiceImpl<SportTypeMapper, SportType> implements SportTypeService {

    private static final Logger log = LoggerFactory.getLogger(SportTypeServiceImpl.class);

    private static final String HOT_TYPES_KEY = "sport:hot:types";

    private static final double DECAY_HALF_LIFE_HOURS = 24;

    private static final double DECAY_LAMBDA = Math.log(2) / DECAY_HALF_LIFE_HOURS;

    private static final int COOLDOWN_WINDOW_HOURS = 2;

    private static final double COOLDOWN_THRESHOLD_MULTIPLIER = 2.0;

    private static final double MIN_COOLDOWN_FACTOR = 0.3;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;

    @Override
    @SuppressWarnings("unchecked")
    public List<SportType> getHotTypes() {
        List<SportType> cacheList = (List<SportType>) redisTemplate.opsForValue().get(HOT_TYPES_KEY);
        if (cacheList != null && !cacheList.isEmpty()) {
            return cacheList;
        }

        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(SportType::getHotCount, 0)
                .orderByAsc(SportType::getSort);
        List<SportType> allTypes = list(wrapper);

        if (allTypes.isEmpty()) {
            return allTypes;
        }

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Integer> recentCheckinCounts = getRecentCheckinCounts(now);
        double averageRecentCount = calculateAverageRecentCount(recentCheckinCounts);

        Map<Long, Double> computedScores = new HashMap<>();
        for (SportType sportType : allTypes) {
            double timeDecayedScore = calculateTimeDecayedScore(sportType, now);
            double cooldownFactor = calculateCooldownFactor(sportType.getId(), recentCheckinCounts, averageRecentCount);
            double finalScore = timeDecayedScore * cooldownFactor;
            computedScores.put(sportType.getId(), finalScore);
            log.debug("运动类型[{}] 基础热度: {}, 时间衰减后: {:.2f}, 冷却系数: {:.2f}, 最终得分: {:.2f}",
                    sportType.getName(), sportType.getHotCount(), timeDecayedScore, cooldownFactor, finalScore);
        }

        List<SportType> sortedList = allTypes.stream()
                .sorted(Comparator.<SportType>comparingDouble(st -> computedScores.getOrDefault(st.getId(), 0.0)).reversed()
                        .thenComparingInt(SportType::getSort))
                .limit(8)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(HOT_TYPES_KEY, sortedList, 10, TimeUnit.MINUTES);
        return sortedList;
    }

    @Override
    public List<SportType> getAllTypes() {
        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SportType::getSort);
        return list(wrapper);
    }

    @Override
    public void incrementHotCount(Long sportTypeId) {
        if (sportTypeId == null) {
            return;
        }
        SportType sportType = getById(sportTypeId);
        if (sportType == null) {
            return;
        }
        int currentHotCount = sportType.getHotCount() == null ? 0 : sportType.getHotCount();
        double decayedHotCount = calculateTimeDecayedScore(sportType, LocalDateTime.now());
        int newHotCount = (int) Math.max(currentHotCount, Math.ceil(decayedHotCount)) + 1;
        sportType.setHotCount(newHotCount);
        sportType.setLastHotUpdateTime(LocalDateTime.now());
        updateById(sportType);
        log.info("运动类型[{}]热度更新: {} -> {}", sportType.getName(), currentHotCount, newHotCount);
        redisTemplate.delete(HOT_TYPES_KEY);
    }

    private double calculateTimeDecayedScore(SportType sportType, LocalDateTime now) {
        Integer hotCount = sportType.getHotCount();
        if (hotCount == null || hotCount <= 0) {
            return 0.0;
        }
        LocalDateTime lastUpdateTime = sportType.getLastHotUpdateTime();
        if (lastUpdateTime == null) {
            lastUpdateTime = sportType.getUpdateTime();
            if (lastUpdateTime == null) {
                lastUpdateTime = sportType.getCreateTime();
            }
        }
        if (lastUpdateTime == null) {
            return hotCount.doubleValue();
        }
        long hoursSinceUpdate = Duration.between(lastUpdateTime, now).toHours();
        if (hoursSinceUpdate < 0) {
            hoursSinceUpdate = 0;
        }
        double decayFactor = Math.exp(-DECAY_LAMBDA * hoursSinceUpdate);
        return hotCount.doubleValue() * decayFactor;
    }

    private Map<Long, Integer> getRecentCheckinCounts(LocalDateTime now) {
        LocalDateTime windowStart = now.minusHours(COOLDOWN_WINDOW_HOURS);
        List<Map<String, Object>> results = checkinRecordMapper.countCheckinBySportTypeSince(windowStart);
        Map<Long, Integer> counts = new HashMap<>();
        for (Map<String, Object> result : results) {
            Long sportTypeId = ((Number) result.get("sport_type_id")).longValue();
            Integer count = ((Number) result.get("count")).intValue();
            counts.put(sportTypeId, count);
        }
        return counts;
    }

    private double calculateAverageRecentCount(Map<Long, Integer> recentCheckinCounts) {
        if (recentCheckinCounts.isEmpty()) {
            return 0.0;
        }
        int total = recentCheckinCounts.values().stream().mapToInt(Integer::intValue).sum();
        int activeTypes = (int) recentCheckinCounts.values().stream().filter(c -> c > 0).count();
        return activeTypes > 0 ? (double) total / activeTypes : 0.0;
    }

    private double calculateCooldownFactor(Long sportTypeId, Map<Long, Integer> recentCheckinCounts, double averageCount) {
        int sportTypeCount = recentCheckinCounts.getOrDefault(sportTypeId, 0);
        if (sportTypeCount <= 0 || averageCount <= 0) {
            return 1.0;
        }
        double threshold = averageCount * COOLDOWN_THRESHOLD_MULTIPLIER;
        if (sportTypeCount <= threshold) {
            return 1.0;
        }
        double excessRatio = (sportTypeCount - threshold) / threshold;
        double cooldownFactor = 1.0 / (1.0 + excessRatio);
        return Math.max(cooldownFactor, MIN_COOLDOWN_FACTOR);
    }

}
