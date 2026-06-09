package com.sport.checkin.controller;

import com.sport.checkin.common.Result;
import com.sport.checkin.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/week")
    public Result<Map<String, Object>> getWeekStatistics(@RequestParam Long userId) {
        try {
            Map<String, Object> data = statisticsService.getWeekStatistics(userId);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/month")
    public Result<Map<String, Object>> getMonthStatistics(@RequestParam Long userId) {
        try {
            Map<String, Object> data = statisticsService.getMonthStatistics(userId);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
