package com.sport.checkin.service.impl;

import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;

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

}
