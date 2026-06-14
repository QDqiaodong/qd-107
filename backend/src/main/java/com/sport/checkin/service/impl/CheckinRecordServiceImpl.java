package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.common.IntensityClassifier;
import com.sport.checkin.common.IntensityLevel;
import com.sport.checkin.common.RedisLock;
import com.sport.checkin.common.SimilarityUtils;
import com.sport.checkin.dto.CheckinResultDTO;
import com.sport.checkin.entity.CheckinRecord;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.service.CheckinRecordService;
import com.sport.checkin.service.SportTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {

    private static final Logger log = LoggerFactory.getLogger(CheckinRecordServiceImpl.class);

    private static final int MERGE_TIME_WINDOW_MINUTES = 5;

    private static final double DURATION_SIMILARITY_RATIO = 0.1;

    private static final int DURATION_SIMILARITY_MINUTES = 5;

    private static final double DISTANCE_SIMILARITY_RATIO = 0.1;

    private static final double REMARK_SIMILARITY_THRESHOLD = 0.8;

    private static final String LOCK_KEY_PREFIX = "checkin:lock:";

    private static final int LOCK_EXPIRE_SECONDS = 10;

    private static final int LOCK_RETRY_TIMES = 3;

    private static final long LOCK_RETRY_INTERVAL_MS = 300;

    @Autowired
    private SportTypeService sportTypeService;

    @Autowired
    private RedisLock redisLock;

    @Override
    public List<CheckinRecord> getCheckinList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
                .orderByDesc(CheckinRecord::getCheckinTime);
        if (page != null && size != null) {
            Page<CheckinRecord> pageParam = new Page<>(page, size);
            return page(pageParam, wrapper).getRecords();
        }
        return list(wrapper);
    }

    @Override
    public CheckinRecord getCheckinDetail(Long id) {
        return getById(id);
    }

    @Override
    public CheckinResultDTO addCheckin(CheckinRecord record) {
        if (record.getCheckinDate() == null) {
            record.setCheckinDate(LocalDate.now());
        }
        if (record.getCheckinTime() == null) {
            record.setCheckinTime(record.getCheckinDate().atTime(LocalTime.now()));
        }

        SportType sportType = record.getSportTypeId() != null
                ? sportTypeService.getById(record.getSportTypeId()) : null;

        if (record.getCalorie() == null && sportType != null && sportType.getCaloriePerMinute() != null) {
            record.setCalorie(sportType.getCaloriePerMinute().multiply(new BigDecimal(record.getDuration())));
        }

        IntensityLevel level = IntensityClassifier.classify(record, sportType);
        record.setIntensity(level.name());

        String lockKey = buildLockKey(record);
        boolean locked = tryLockWithRetry(lockKey);
        if (!locked) {
            log.warn("获取打卡锁失败，重试后仍未获取，userId:{}, sportTypeId:{}", record.getUserId(), record.getSportTypeId());
            CheckinRecord existing = findSimilarRecord(record);
            if (existing != null) {
                return CheckinResultDTO.of(existing, true, "检测到短时间内存在相似打卡，已自动合并");
            }
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            CheckinRecord similarRecord = findSimilarRecord(record);
            if (similarRecord != null) {
                CheckinRecord mergedRecord = mergeRecords(similarRecord, record);
                mergedRecord.setIntensity(IntensityClassifier.classify(mergedRecord, sportType).name());
                updateById(mergedRecord);
                sportTypeService.incrementHotCount(record.getSportTypeId());
                log.info("打卡合并成功，userId:{}, sportTypeId:{}, 原记录id:{}", record.getUserId(), record.getSportTypeId(), similarRecord.getId());
                return CheckinResultDTO.of(mergedRecord, true, "检测到短时间内存在相似打卡，已自动合并");
            }

            save(record);
            sportTypeService.incrementHotCount(record.getSportTypeId());
            log.info("打卡成功，userId:{}, sportTypeId:{}, 记录id:{}", record.getUserId(), record.getSportTypeId(), record.getId());
            return CheckinResultDTO.of(record, false, "打卡成功");
        } finally {
            redisLock.unlock(lockKey);
        }
    }

    @Override
    public boolean deleteCheckin(Long id, Long userId) {
        CheckinRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("打卡记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除该记录");
        }
        return removeById(id);
    }

    private boolean tryLockWithRetry(String lockKey) {
        for (int i = 0; i < LOCK_RETRY_TIMES; i++) {
            boolean locked = redisLock.tryLock(lockKey, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (locked) {
                return true;
            }
            if (i < LOCK_RETRY_TIMES - 1) {
                try {
                    Thread.sleep(LOCK_RETRY_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private String buildLockKey(CheckinRecord record) {
        return LOCK_KEY_PREFIX + record.getUserId() + ":" + record.getSportTypeId();
    }

    private CheckinRecord findSimilarRecord(CheckinRecord newRecord) {
        LocalDateTime timeWindowStart = newRecord.getCheckinTime().minusMinutes(MERGE_TIME_WINDOW_MINUTES);

        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, newRecord.getUserId())
                .eq(CheckinRecord::getSportTypeId, newRecord.getSportTypeId())
                .ge(CheckinRecord::getCheckinTime, timeWindowStart)
                .lt(CheckinRecord::getCheckinTime, newRecord.getCheckinTime())
                .orderByAsc(CheckinRecord::getCheckinTime);

        List<CheckinRecord> recentRecords = list(wrapper);

        for (CheckinRecord existing : recentRecords) {
            if (isSimilar(existing, newRecord)) {
                return existing;
            }
        }
        return null;
    }

    private boolean isSimilar(CheckinRecord existing, CheckinRecord newRecord) {
        boolean durationSimilar = isDurationSimilar(existing.getDuration(), newRecord.getDuration());
        boolean distanceSimilar = isDistanceSimilar(existing.getDistance(), newRecord.getDistance());
        boolean remarkSimilar = isRemarkSimilar(existing.getRemark(), newRecord.getRemark());

        return durationSimilar && distanceSimilar && remarkSimilar;
    }

    private boolean isDurationSimilar(Integer d1, Integer d2) {
        if (d1 == null || d2 == null) {
            return true;
        }
        int diff = Math.abs(d1 - d2);
        if (diff <= DURATION_SIMILARITY_MINUTES) {
            return true;
        }
        int max = Math.max(d1, d2);
        return (double) diff / max <= DURATION_SIMILARITY_RATIO;
    }

    private boolean isDistanceSimilar(BigDecimal dist1, BigDecimal dist2) {
        if (dist1 == null || dist2 == null) {
            return true;
        }
        if (dist1.compareTo(BigDecimal.ZERO) == 0 && dist2.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        BigDecimal diff = dist1.subtract(dist2).abs();
        BigDecimal max = dist1.max(dist2);
        if (max.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return diff.divide(max, 4, RoundingMode.HALF_UP).doubleValue() <= DISTANCE_SIMILARITY_RATIO;
    }

    private boolean isRemarkSimilar(String r1, String r2) {
        if (!StringUtils.hasText(r1) && !StringUtils.hasText(r2)) {
            return true;
        }
        if (!StringUtils.hasText(r1) || !StringUtils.hasText(r2)) {
            return false;
        }
        return SimilarityUtils.isSimilar(r1.trim(), r2.trim(), REMARK_SIMILARITY_THRESHOLD);
    }

    private CheckinRecord mergeRecords(CheckinRecord existing, CheckinRecord newRecord) {
        if (newRecord.getDuration() != null) {
            Integer existingDuration = Objects.requireNonNullElse(existing.getDuration(), 0);
            existing.setDuration(Math.max(existingDuration, newRecord.getDuration()));
        }

        if (newRecord.getCalorie() != null) {
            if (existing.getCalorie() == null || newRecord.getCalorie().compareTo(existing.getCalorie()) > 0) {
                existing.setCalorie(newRecord.getCalorie());
            }
        }

        if (newRecord.getDistance() != null) {
            if (existing.getDistance() == null || newRecord.getDistance().compareTo(existing.getDistance()) > 0) {
                existing.setDistance(newRecord.getDistance());
            }
        }

        if (newRecord.getPlanId() != null && existing.getPlanId() == null) {
            existing.setPlanId(newRecord.getPlanId());
        }

        String mergedRemark = mergeRemark(existing.getRemark(), newRecord.getRemark());
        existing.setRemark(mergedRemark);

        String mergedImages = mergeImages(existing.getImages(), newRecord.getImages());
        existing.setImages(mergedImages);

        existing.setUpdateTime(LocalDateTime.now());

        return existing;
    }

    private String mergeRemark(String r1, String r2) {
        if (!StringUtils.hasText(r1) && !StringUtils.hasText(r2)) {
            return null;
        }
        if (!StringUtils.hasText(r1)) {
            return r2;
        }
        if (!StringUtils.hasText(r2)) {
            return r1;
        }
        if (r1.equals(r2)) {
            return r1;
        }
        if (SimilarityUtils.isSimilar(r1, r2, REMARK_SIMILARITY_THRESHOLD)) {
            return r1.length() >= r2.length() ? r1 : r2;
        }
        return r1 + "；" + r2;
    }

    private String mergeImages(String existingImg, String newImg) {
        Set<String> allImages = new LinkedHashSet<>();
        if (StringUtils.hasText(existingImg)) {
            Arrays.stream(existingImg.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(allImages::add);
        }
        if (StringUtils.hasText(newImg)) {
            Arrays.stream(newImg.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(allImages::add);
        }
        return allImages.isEmpty() ? null : String.join(",", allImages);
    }

}
