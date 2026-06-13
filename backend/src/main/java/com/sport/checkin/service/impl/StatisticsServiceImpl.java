package com.sport.checkin.service.impl;

import com.sport.checkin.common.IntensityLevel;
import com.sport.checkin.dto.SportPreferenceDTO;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.mapper.SportTypeMapper;
import com.sport.checkin.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;

    @Autowired
    private SportTypeMapper sportTypeMapper;

    @Override
    public Map<String, Object> getWeekStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        Map<String, Object> result = new HashMap<>();
        result.put("checkinCount", checkinRecordMapper.countByDateRange(userId, weekStart, weekEnd));
        result.put("totalDuration", checkinRecordMapper.sumDurationByDateRange(userId, weekStart, weekEnd));
        result.put("totalCalorie", checkinRecordMapper.sumCalorieByDateRange(userId, weekStart, weekEnd));
        result.put("startDate", weekStart.toString());
        result.put("endDate", weekEnd.toString());
        return result;
    }

    @Override
    public Map<String, Object> getMonthStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
        Map<String, Object> result = new HashMap<>();
        result.put("checkinCount", checkinRecordMapper.countByDateRange(userId, monthStart, monthEnd));
        result.put("totalDuration", checkinRecordMapper.sumDurationByDateRange(userId, monthStart, monthEnd));
        result.put("totalCalorie", checkinRecordMapper.sumCalorieByDateRange(userId, monthStart, monthEnd));
        result.put("startDate", monthStart.toString());
        result.put("endDate", monthEnd.toString());
        return result;
    }

    @Override
    public Map<String, Object> getIntensityDistribution(Long userId, String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;

        if ("month".equalsIgnoreCase(period)) {
            startDate = today.with(TemporalAdjusters.firstDayOfMonth());
            endDate = today.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        List<Map<String, Object>> raw = checkinRecordMapper.countByIntensityAndDateRange(userId, startDate, endDate);

        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (IntensityLevel level : IntensityLevel.values()) {
            distribution.put(level.name(), 0);
        }
        for (Map<String, Object> row : raw) {
            String intensity = (String) row.get("intensity");
            Number count = (Number) row.get("count");
            if (intensity != null && count != null) {
                distribution.put(intensity, count.intValue());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("distribution", distribution);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("period", period);
        return result;
    }

    @Override
    public SportPreferenceDTO getUserPreference(Long userId) {
        SportPreferenceDTO dto = new SportPreferenceDTO();
        dto.setUserId(userId);

        Integer totalCount = checkinRecordMapper.getTotalCheckinCount(userId);
        if (totalCount == null || totalCount == 0) {
            dto.setTotalCheckinCount(0);
            dto.setAverageDuration(BigDecimal.ZERO);
            dto.setFrequentSports(Collections.emptyList());
            dto.setPreferredTimeSlots(Collections.emptyList());
            dto.setFrequentWeekdays(Collections.emptyList());
            dto.setSummary("暂无打卡记录，开始你的第一次运动吧！");
            return dto;
        }

        dto.setTotalCheckinCount(totalCount);

        BigDecimal avgDuration = checkinRecordMapper.getAverageDuration(userId);
        dto.setAverageDuration(avgDuration != null ? avgDuration.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        dto.setFrequentSports(buildFrequentSports(userId, totalCount));
        dto.setPreferredTimeSlots(buildPreferredTimeSlots(userId, totalCount));
        dto.setFrequentWeekdays(buildFrequentWeekdays(userId, totalCount));
        dto.setSummary(buildSummary(dto));

        return dto;
    }

    private List<SportPreferenceDTO.SportItem> buildFrequentSports(Long userId, Integer totalCount) {
        List<Map<String, Object>> rawList = checkinRecordMapper.countAndDurationBySportType(userId);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> sportTypeIds = rawList.stream()
                .map(row -> ((Number) row.get("sport_type_id")).longValue())
                .collect(Collectors.toList());

        Map<Long, SportType> sportTypeMap = new HashMap<>();
        if (!sportTypeIds.isEmpty()) {
            List<SportType> sportTypes = sportTypeMapper.selectBatchIds(sportTypeIds);
            sportTypeMap = sportTypes.stream()
                    .collect(Collectors.toMap(SportType::getId, st -> st));
        }

        List<SportPreferenceDTO.SportItem> result = new ArrayList<>();
        for (Map<String, Object> row : rawList) {
            SportPreferenceDTO.SportItem item = new SportPreferenceDTO.SportItem();
            Long sportTypeId = ((Number) row.get("sport_type_id")).longValue();
            Integer count = ((Number) row.get("count")).intValue();
            Integer totalDuration = ((Number) row.get("total_duration")).intValue();

            item.setSportTypeId(sportTypeId);
            item.setCount(count);
            item.setTotalDuration(totalDuration);
            item.setPercentage(BigDecimal.valueOf(count)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP));

            SportType sportType = sportTypeMap.get(sportTypeId);
            if (sportType != null) {
                item.setSportTypeName(sportType.getName());
                item.setIcon(sportType.getIcon());
            } else {
                item.setSportTypeName("未知项目");
            }
            result.add(item);
        }
        return result;
    }

    private List<SportPreferenceDTO.TimeSlot> buildPreferredTimeSlots(Long userId, Integer totalCount) {
        List<Map<String, Object>> rawList = checkinRecordMapper.countByHour(userId);
        Map<String, Integer> slotCountMap = new LinkedHashMap<>();
        slotCountMap.put("深夜", 0);
        slotCountMap.put("清晨", 0);
        slotCountMap.put("上午", 0);
        slotCountMap.put("中午", 0);
        slotCountMap.put("下午", 0);
        slotCountMap.put("傍晚", 0);
        slotCountMap.put("夜间", 0);

        Map<String, String> slotRangeMap = new LinkedHashMap<>();
        slotRangeMap.put("深夜", "23:00-05:00");
        slotRangeMap.put("清晨", "05:00-08:00");
        slotRangeMap.put("上午", "08:00-11:00");
        slotRangeMap.put("中午", "11:00-14:00");
        slotRangeMap.put("下午", "14:00-17:00");
        slotRangeMap.put("傍晚", "17:00-20:00");
        slotRangeMap.put("夜间", "20:00-23:00");

        if (rawList != null) {
            for (Map<String, Object> row : rawList) {
                Integer hour = ((Number) row.get("hour")).intValue();
                Integer count = ((Number) row.get("count")).intValue();
                String slot = getTimeSlot(hour);
                if (slot != null) {
                    slotCountMap.put(slot, slotCountMap.get(slot) + count);
                }
            }
        }

        List<SportPreferenceDTO.TimeSlot> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : slotCountMap.entrySet()) {
            if (entry.getValue() > 0) {
                SportPreferenceDTO.TimeSlot slot = new SportPreferenceDTO.TimeSlot();
                slot.setSlotName(entry.getKey());
                slot.setSlotRange(slotRangeMap.get(entry.getKey()));
                slot.setCount(entry.getValue());
                slot.setPercentage(BigDecimal.valueOf(entry.getValue())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP));
                result.add(slot);
            }
        }

        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        return result;
    }

    private String getTimeSlot(Integer hour) {
        if (hour >= 5 && hour < 8) return "清晨";
        if (hour >= 8 && hour < 11) return "上午";
        if (hour >= 11 && hour < 14) return "中午";
        if (hour >= 14 && hour < 17) return "下午";
        if (hour >= 17 && hour < 20) return "傍晚";
        if (hour >= 20 && hour < 23) return "夜间";
        if (hour >= 23 || hour < 5) return "深夜";
        return null;
    }

    private List<SportPreferenceDTO.WeekdayFreq> buildFrequentWeekdays(Long userId, Integer totalCount) {
        List<Map<String, Object>> rawList = checkinRecordMapper.countByWeekday(userId);
        String[] weekdayNames = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};

        Map<Integer, Integer> weekdayCountMap = new LinkedHashMap<>();
        for (int i = 1; i <= 7; i++) {
            weekdayCountMap.put(i, 0);
        }

        if (rawList != null) {
            for (Map<String, Object> row : rawList) {
                Integer weekday = ((Number) row.get("weekday")).intValue();
                Integer count = ((Number) row.get("count")).intValue();
                weekdayCountMap.put(weekday, count);
            }
        }

        List<SportPreferenceDTO.WeekdayFreq> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : weekdayCountMap.entrySet()) {
            if (entry.getValue() > 0) {
                SportPreferenceDTO.WeekdayFreq freq = new SportPreferenceDTO.WeekdayFreq();
                freq.setWeekday(entry.getKey());
                freq.setWeekdayName(weekdayNames[entry.getKey()]);
                freq.setCount(entry.getValue());
                freq.setPercentage(BigDecimal.valueOf(entry.getValue())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP));
                result.add(freq);
            }
        }

        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        return result;
    }

    private String buildSummary(SportPreferenceDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("已累计打卡").append(dto.getTotalCheckinCount()).append("次，");

        if (!dto.getFrequentSports().isEmpty()) {
            SportPreferenceDTO.SportItem topSport = dto.getFrequentSports().get(0);
            sb.append("最常练习").append(topSport.getSportTypeName());
            if (dto.getFrequentSports().size() > 1) {
                sb.append("、").append(dto.getFrequentSports().get(1).getSportTypeName());
            }
            sb.append("，");
        }

        if (!dto.getPreferredTimeSlots().isEmpty()) {
            SportPreferenceDTO.TimeSlot topSlot = dto.getPreferredTimeSlots().get(0);
            sb.append("偏好").append(topSlot.getSlotName()).append("训练，");
        }

        if (!dto.getFrequentWeekdays().isEmpty()) {
            SportPreferenceDTO.WeekdayFreq topDay = dto.getFrequentWeekdays().get(0);
            sb.append("常在").append(topDay.getWeekdayName()).append("运动，");
        }

        sb.append("平均每次运动").append(dto.getAverageDuration()).append("分钟。");
        sb.append("继续保持，运动达人就是你！");

        return sb.toString();
    }

}
