package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.dto.PlanExecutionSnapshotDTO;
import com.sport.checkin.entity.SportPlan;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.mapper.SportPlanMapper;
import com.sport.checkin.mapper.SportTypeMapper;
import com.sport.checkin.service.SportPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SportPlanServiceImpl extends ServiceImpl<SportPlanMapper, SportPlan> implements SportPlanService {

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;

    @Autowired
    private SportTypeMapper sportTypeMapper;

    @Override
    public List<SportPlan> getPlanList(Long userId) {
        LambdaQueryWrapper<SportPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportPlan::getUserId, userId)
                .orderByDesc(SportPlan::getCreateTime);
        return list(wrapper);
    }

    @Override
    public SportPlan getPlanDetail(Long id) {
        return getById(id);
    }

    @Override
    public boolean addPlan(SportPlan plan) {
        plan.setStatus(1);
        return save(plan);
    }

    @Override
    public boolean updatePlan(SportPlan plan) {
        return updateById(plan);
    }

    @Override
    public boolean deletePlan(Long id) {
        return removeById(id);
    }

    @Override
    public boolean setReminder(Long id, Integer enabled, String reminderTime) {
        SportPlan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }
        plan.setReminderEnabled(enabled);
        if (reminderTime != null && !reminderTime.isEmpty()) {
            plan.setReminderTime(LocalTime.parse(reminderTime));
        }
        return updateById(plan);
    }

    @Override
    public List<PlanExecutionSnapshotDTO> getExecutionSnapshots(Long userId, String status) {
        LambdaQueryWrapper<SportPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportPlan::getUserId, userId)
                .eq(SportPlan::getStatus, 1)
                .orderByDesc(SportPlan::getCreateTime);
        List<SportPlan> plans = list(wrapper);

        List<PlanExecutionSnapshotDTO> snapshots = new ArrayList<>();
        for (SportPlan plan : plans) {
            PlanExecutionSnapshotDTO snapshot = buildSnapshot(plan);
            if (status == null || status.isEmpty() || status.equals(snapshot.getStatus())) {
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    private PlanExecutionSnapshotDTO buildSnapshot(SportPlan plan) {
        PlanExecutionSnapshotDTO dto = new PlanExecutionSnapshotDTO();
        dto.setPlanId(plan.getId());
        dto.setTitle(plan.getTitle());
        dto.setSportTypeId(plan.getSportTypeId());

        SportType sportType = sportTypeMapper.selectById(plan.getSportTypeId());
        dto.setSportTypeName(sportType != null ? sportType.getName() : "");

        LocalDate today = LocalDate.now();
        LocalDate cycleStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate cycleEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        if (plan.getStartDate() != null && plan.getStartDate().isAfter(cycleStart)) {
            cycleStart = plan.getStartDate();
        }
        if (plan.getEndDate() != null && plan.getEndDate().isBefore(cycleEnd)) {
            cycleEnd = plan.getEndDate();
        }

        dto.setCycleStart(cycleStart.toString());
        dto.setCycleEnd(cycleEnd.toString());

        int targetFrequency = plan.getTargetFrequency() != null ? plan.getTargetFrequency() : 0;
        int targetDuration = plan.getTargetDuration() != null ? plan.getTargetDuration() : 0;
        dto.setTargetFrequency(targetFrequency);
        dto.setTargetDuration(targetDuration);

        Integer completedCount;
        Integer completedDuration;
        BigDecimal completedCalorie;

        completedCount = checkinRecordMapper.countBySportTypeAndDateRange(
                plan.getUserId(), plan.getSportTypeId(), cycleStart, cycleEnd);
        completedDuration = checkinRecordMapper.sumDurationBySportTypeAndDateRange(
                plan.getUserId(), plan.getSportTypeId(), cycleStart, cycleEnd);
        completedCalorie = checkinRecordMapper.sumCalorieBySportTypeAndDateRange(
                plan.getUserId(), plan.getSportTypeId(), cycleStart, cycleEnd);

        dto.setCompletedCount(completedCount != null ? completedCount : 0);
        dto.setCompletedDuration(completedDuration != null ? completedDuration : 0);
        dto.setCompletedCalorie(completedCalorie != null ? completedCalorie : BigDecimal.ZERO);

        long daysElapsed = ChronoUnit.DAYS.between(cycleStart, today) + 1;
        long totalDays = ChronoUnit.DAYS.between(cycleStart, cycleEnd) + 1;
        dto.setDaysElapsed((int) Math.max(1, daysElapsed));
        dto.setDaysRemaining((int) Math.max(0, totalDays - daysElapsed));

        double completionRate = 0.0;
        if (targetFrequency > 0) {
            completionRate = (dto.getCompletedCount() * 1.0 / targetFrequency) * 100;
        } else if (targetDuration > 0) {
            completionRate = (dto.getCompletedDuration() * 1.0 / targetDuration) * 100;
        }
        dto.setCompletionRate(BigDecimal.valueOf(completionRate).setScale(1, RoundingMode.HALF_UP).doubleValue());

        double expectedProgress = totalDays > 0 ? (daysElapsed * 1.0 / totalDays) * 100 : 100.0;
        dto.setExpectedProgress(BigDecimal.valueOf(expectedProgress).setScale(1, RoundingMode.HALF_UP).doubleValue());

        double deviation = completionRate - expectedProgress;
        dto.setDeviation(BigDecimal.valueOf(deviation).setScale(1, RoundingMode.HALF_UP).doubleValue());

        String planStatus = classifyStatus(completionRate, expectedProgress);
        dto.setStatus(planStatus);

        return dto;
    }

    private String classifyStatus(double completionRate, double expectedProgress) {
        if (completionRate >= 100) {
            return "GOAL_MET";
        }
        if (completionRate < expectedProgress * 0.5) {
            return "SIGNIFICANTLY_BEHIND";
        }
        return "IN_PROGRESS";
    }

}
