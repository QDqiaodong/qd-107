package com.sport.checkin.service;

import com.sport.checkin.dto.SportPreferenceDTO;

import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getWeekStatistics(Long userId);

    Map<String, Object> getMonthStatistics(Long userId);

    Map<String, Object> getIntensityDistribution(Long userId, String period);

    SportPreferenceDTO getUserPreference(Long userId);
}
