package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.dto.PlanConflictCheckResultDTO;
import com.sport.checkin.dto.PlanConflictDTO;
import com.sport.checkin.dto.PlanExecutionSnapshotDTO;
import com.sport.checkin.entity.SportPlan;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.mapper.SportPlanMapper;
import com.sport.checkin.mapper.SportTypeMapper;
import com.sport.checkin.service.SportPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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

        PlanConflictCheckResultDTO checkResult = checkPlanConflictsWithWarnings(plan, plan, null);
        if (checkResult.hasConflicts()) {
            throw new RuntimeException(buildConflictMessage(checkResult.getConflicts()));
        }

        return save(plan);
    }

    @Override
    public boolean updatePlan(SportPlan plan) {
        if (plan.getId() == null) {
            throw new RuntimeException("计划ID不能为空");
        }

        SportPlan mergedPlan = mergePlanWithExisting(plan);
        return updatePlan(plan, mergedPlan);
    }

    @Override
    public boolean updatePlan(SportPlan originalPlan, SportPlan mergedPlan) {
        if (mergedPlan.getId() == null) {
            throw new RuntimeException("计划ID不能为空");
        }

        PlanConflictCheckResultDTO checkResult = checkPlanConflictsWithWarnings(mergedPlan, originalPlan, mergedPlan.getId());
        if (checkResult.hasConflicts()) {
            throw new RuntimeException(buildConflictMessage(checkResult.getConflicts()));
        }

        return updateById(mergedPlan);
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

    private static final int REMINDER_CONFLICT_MINUTES = 30;

    private static final Map<String, LocalTime[]> TIME_SLOT_RANGE = new HashMap<>();
    static {
        TIME_SLOT_RANGE.put("MORNING", new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(9, 0)});
        TIME_SLOT_RANGE.put("FORENOON", new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(12, 0)});
        TIME_SLOT_RANGE.put("AFTERNOON", new LocalTime[]{LocalTime.of(12, 0), LocalTime.of(18, 0)});
        TIME_SLOT_RANGE.put("EVENING", new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(21, 0)});
        TIME_SLOT_RANGE.put("NIGHT", new LocalTime[]{LocalTime.of(21, 0), LocalTime.of(23, 59, 59)});
    }

    private static final Map<String, String> TIME_SLOT_CN = new HashMap<>();
    static {
        TIME_SLOT_CN.put("MORNING", "早间(06:00-09:00)");
        TIME_SLOT_CN.put("FORENOON", "上午(09:00-12:00)");
        TIME_SLOT_CN.put("AFTERNOON", "下午(12:00-18:00)");
        TIME_SLOT_CN.put("EVENING", "晚间(18:00-21:00)");
        TIME_SLOT_CN.put("NIGHT", "夜间(21:00之后)");
    }

    @Override
    public List<PlanConflictDTO> checkPlanConflicts(SportPlan plan, Long excludePlanId) {
        List<PlanConflictDTO> conflicts = new ArrayList<>();
        if (plan.getUserId() == null) {
            return conflicts;
        }

        LambdaQueryWrapper<SportPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportPlan::getUserId, plan.getUserId())
                .eq(SportPlan::getStatus, 1);
        if (excludePlanId != null) {
            wrapper.ne(SportPlan::getId, excludePlanId);
        }
        List<SportPlan> existingPlans = list(wrapper);

        for (SportPlan existing : existingPlans) {
            checkDateRangeConflict(plan, existing, conflicts);
            checkWeekdayConflict(plan, existing, conflicts);
            checkReminderTimeConflict(plan, existing, conflicts);
            checkTrainingTimeSlotConflict(plan, existing, conflicts);
        }

        return conflicts;
    }

    private String buildConflictMessage(List<PlanConflictDTO> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("检测到计划安排冲突（共").append(conflicts.size()).append("项）：\n");
        int idx = 1;
        for (PlanConflictDTO c : conflicts) {
            sb.append(idx++).append(". [").append(c.getConflictDescription()).append("]");
            if (c.getConflictingPlanTitle() != null) {
                sb.append(" - 与计划《").append(c.getConflictingPlanTitle()).append("》");
            }
            if (c.getConflictDetail() != null) {
                sb.append("：").append(c.getConflictDetail());
            }
            sb.append(";\n");
        }
        return sb.toString();
    }

    private void checkDateRangeConflict(SportPlan newPlan, SportPlan existing, List<PlanConflictDTO> conflicts) {
        LocalDate newStart = newPlan.getStartDate();
        LocalDate newEnd = newPlan.getEndDate();
        LocalDate existingStart = existing.getStartDate();
        LocalDate existingEnd = existing.getEndDate();

        Set<Integer> newWeekdays = parseWeekdaysForConflict(newPlan.getWeekdays());
        Set<Integer> existingWeekdays = parseWeekdaysForConflict(existing.getWeekdays());
        newWeekdays.retainAll(existingWeekdays);

        if (newWeekdays.isEmpty()) {
            return;
        }

        if (newStart == null && newEnd == null && existingStart == null && existingEnd == null) {
            PlanConflictDTO dto = new PlanConflictDTO();
            dto.setConflictType("DATE_RANGE_OVERLAP");
            dto.setConflictDescription("执行日期与训练日存在重叠");
            dto.setConflictingPlanId(existing.getId());
            dto.setConflictingPlanTitle(existing.getTitle());
            String newWeekdaysEmpty = (newPlan.getWeekdays() == null || newPlan.getWeekdays().isEmpty()) ? "（未设置训练日，默认按每天执行）" : "";
            String existingWeekdaysEmpty = (existing.getWeekdays() == null || existing.getWeekdays().isEmpty()) ? "（未设置训练日，默认按每天执行）" : "";
            dto.setConflictDetail(String.format("双方均未设置执行日期，共同训练日：%s%s%s",
                    formatWeekdays(newWeekdays), newWeekdaysEmpty, existingWeekdaysEmpty));
            conflicts.add(dto);
            return;
        }

        LocalDate effectiveNewStart = newStart != null ? newStart : LocalDate.now();
        LocalDate effectiveNewEnd = newEnd != null ? newEnd : LocalDate.now().plusYears(1);
        LocalDate effectiveExistingStart = existingStart != null ? existingStart : LocalDate.now();
        LocalDate effectiveExistingEnd = existingEnd != null ? existingEnd : LocalDate.now().plusYears(1);

        boolean dateOverlap = !effectiveNewStart.isAfter(effectiveExistingEnd)
                && !effectiveNewEnd.isBefore(effectiveExistingStart);

        if (!dateOverlap) {
            return;
        }

        LocalDate overlapStart = effectiveNewStart.isAfter(effectiveExistingStart)
                ? effectiveNewStart : effectiveExistingStart;
        LocalDate overlapEnd = effectiveNewEnd.isBefore(effectiveExistingEnd)
                ? effectiveNewEnd : effectiveExistingEnd;

        PlanConflictDTO dto = new PlanConflictDTO();
        dto.setConflictType("DATE_RANGE_OVERLAP");
        dto.setConflictDescription("执行日期与训练日存在重叠");
        dto.setConflictingPlanId(existing.getId());
        dto.setConflictingPlanTitle(existing.getTitle());
        String newWeekdaysEmpty = (newPlan.getWeekdays() == null || newPlan.getWeekdays().isEmpty()) ? "（新计划未设置训练日，默认按每天执行）" : "";
        String existingWeekdaysEmpty = (existing.getWeekdays() == null || existing.getWeekdays().isEmpty()) ? "（已有计划未设置训练日，默认按每天执行）" : "";
        dto.setConflictDetail(String.format("重叠期：%s 至 %s，共同训练日：%s%s%s",
                overlapStart, overlapEnd, formatWeekdays(newWeekdays), newWeekdaysEmpty, existingWeekdaysEmpty));
        conflicts.add(dto);
    }

    private void checkWeekdayConflict(SportPlan newPlan, SportPlan existing, List<PlanConflictDTO> conflicts) {
        Set<Integer> newWeekdays = parseWeekdaysForConflict(newPlan.getWeekdays());
        Set<Integer> existingWeekdays = parseWeekdaysForConflict(existing.getWeekdays());
        newWeekdays.retainAll(existingWeekdays);

        if (!newWeekdays.isEmpty()) {
            boolean alreadyReported = conflicts.stream()
                    .anyMatch(c -> "DATE_RANGE_OVERLAP".equals(c.getConflictType())
                            && existing.getId().equals(c.getConflictingPlanId()));
            if (alreadyReported) {
                return;
            }

            PlanConflictDTO dto = new PlanConflictDTO();
            dto.setConflictType("WEEKDAY_OVERLAP");
            dto.setConflictDescription("训练日安排在同一天");
            dto.setConflictingPlanId(existing.getId());
            dto.setConflictingPlanTitle(existing.getTitle());
            String newWeekdaysEmpty = (newPlan.getWeekdays() == null || newPlan.getWeekdays().isEmpty()) ? "（新计划未设置训练日，默认按每天执行）" : "";
            String existingWeekdaysEmpty = (existing.getWeekdays() == null || existing.getWeekdays().isEmpty()) ? "（已有计划未设置训练日，默认按每天执行）" : "";
            dto.setConflictDetail(String.format("共同训练日：%s%s%s", formatWeekdays(newWeekdays), newWeekdaysEmpty, existingWeekdaysEmpty));
            conflicts.add(dto);
        }
    }

    private void checkReminderTimeConflict(SportPlan newPlan, SportPlan existing, List<PlanConflictDTO> conflicts) {
        if (newPlan.getReminderTime() == null || existing.getReminderTime() == null) {
            return;
        }
        if (newPlan.getReminderEnabled() != null && newPlan.getReminderEnabled() == 0) {
            return;
        }
        if (existing.getReminderEnabled() != null && existing.getReminderEnabled() == 0) {
            return;
        }

        Set<Integer> newWeekdays = parseWeekdaysForConflict(newPlan.getWeekdays());
        Set<Integer> existingWeekdays = parseWeekdaysForConflict(existing.getWeekdays());
        newWeekdays.retainAll(existingWeekdays);
        if (newWeekdays.isEmpty()) {
            return;
        }

        long minutesDiff = Math.abs(Duration.between(newPlan.getReminderTime(), existing.getReminderTime()).toMinutes());
        if (minutesDiff < REMINDER_CONFLICT_MINUTES) {
            PlanConflictDTO dto = new PlanConflictDTO();
            dto.setConflictType("REMINDER_TIME_TOO_CLOSE");
            dto.setConflictDescription("提醒时间间隔过短");
            dto.setConflictingPlanId(existing.getId());
            dto.setConflictingPlanTitle(existing.getTitle());
            String newWeekdaysEmpty = (newPlan.getWeekdays() == null || newPlan.getWeekdays().isEmpty()) ? "（新计划未设置训练日，默认按每天执行）" : "";
            String existingWeekdaysEmpty = (existing.getWeekdays() == null || existing.getWeekdays().isEmpty()) ? "（已有计划未设置训练日，默认按每天执行）" : "";
            dto.setConflictDetail(String.format("两个计划提醒时间仅相差 %d 分钟（建议至少间隔 %d 分钟），冲突日：%s%s%s",
                    minutesDiff, REMINDER_CONFLICT_MINUTES, formatWeekdays(newWeekdays), newWeekdaysEmpty, existingWeekdaysEmpty));
            conflicts.add(dto);
        }
    }

    private void checkTrainingTimeSlotConflict(SportPlan newPlan, SportPlan existing, List<PlanConflictDTO> conflicts) {
        if (newPlan.getTrainingTimeSlot() == null || newPlan.getTrainingTimeSlot().isEmpty()
                || existing.getTrainingTimeSlot() == null || existing.getTrainingTimeSlot().isEmpty()) {
            return;
        }

        Set<Integer> newWeekdays = parseWeekdaysForConflict(newPlan.getWeekdays());
        Set<Integer> existingWeekdays = parseWeekdaysForConflict(existing.getWeekdays());
        newWeekdays.retainAll(existingWeekdays);
        if (newWeekdays.isEmpty()) {
            return;
        }

        String newSlot = newPlan.getTrainingTimeSlot();
        String existingSlot = existing.getTrainingTimeSlot();
        String newWeekdaysEmpty = (newPlan.getWeekdays() == null || newPlan.getWeekdays().isEmpty()) ? "（新计划未设置训练日，默认按每天执行）" : "";
        String existingWeekdaysEmpty = (existing.getWeekdays() == null || existing.getWeekdays().isEmpty()) ? "（已有计划未设置训练日，默认按每天执行）" : "";

        if (newSlot.equals(existingSlot)) {
            PlanConflictDTO dto = new PlanConflictDTO();
            dto.setConflictType("TRAINING_SLOT_SAME");
            dto.setConflictDescription("主要训练时段完全相同");
            dto.setConflictingPlanId(existing.getId());
            dto.setConflictingPlanTitle(existing.getTitle());
            dto.setConflictDetail(String.format("两个计划都安排在 %s，冲突日：%s%s%s",
                    TIME_SLOT_CN.getOrDefault(newSlot, newSlot), formatWeekdays(newWeekdays), newWeekdaysEmpty, existingWeekdaysEmpty));
            conflicts.add(dto);
        } else if (isAdjacentSlot(newSlot, existingSlot)) {
            PlanConflictDTO dto = new PlanConflictDTO();
            dto.setConflictType("TRAINING_SLOT_ADJACENT");
            dto.setConflictDescription("主要训练时段相邻，可能挤压执行");
            dto.setConflictingPlanId(existing.getId());
            dto.setConflictingPlanTitle(existing.getTitle());
            dto.setConflictDetail(String.format("%s 与 %s 时段相邻，冲突日：%s%s%s",
                    TIME_SLOT_CN.getOrDefault(newSlot, newSlot),
                    TIME_SLOT_CN.getOrDefault(existingSlot, existingSlot),
                    formatWeekdays(newWeekdays), newWeekdaysEmpty, existingWeekdaysEmpty));
            conflicts.add(dto);
        }
    }

    private boolean isAdjacentSlot(String slot1, String slot2) {
        String[] order = {"MORNING", "FORENOON", "AFTERNOON", "EVENING", "NIGHT"};
        int idx1 = -1, idx2 = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(slot1)) idx1 = i;
            if (order[i].equals(slot2)) idx2 = i;
        }
        return idx1 >= 0 && idx2 >= 0 && Math.abs(idx1 - idx2) == 1;
    }

    private Set<Integer> parseWeekdays(String weekdaysStr) {
        return parseWeekdays(weekdaysStr, false);
    }

    private Set<Integer> parseWeekdays(String weekdaysStr, boolean treatEmptyAsAll) {
        Set<Integer> result = new HashSet<>();
        if (weekdaysStr == null || weekdaysStr.isEmpty()) {
            if (treatEmptyAsAll) {
                result.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
            }
            return result;
        }
        String[] parts = weekdaysStr.split("[,，]");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    result.add(Integer.parseInt(trimmed));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }

    private Set<Integer> parseWeekdaysForConflict(String weekdaysStr) {
        return parseWeekdays(weekdaysStr, true);
    }

    private String formatWeekdays(Set<Integer> weekdays) {
        String[] names = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        List<Integer> sorted = new ArrayList<>(weekdays);
        Collections.sort(sorted, (a, b) -> {
            if (a == 0) return 1;
            if (b == 0) return -1;
            return a - b;
        });
        return sorted.stream()
                .filter(d -> d >= 0 && d <= 6)
                .map(d -> names[d])
                .collect(Collectors.joining("、"));
    }

    @Override
    public PlanConflictCheckResultDTO checkPlanConflictsWithWarnings(SportPlan plan, Long excludePlanId) {
        PlanConflictCheckResultDTO result = new PlanConflictCheckResultDTO();

        List<String> warnings = validatePlanFields(plan, plan, excludePlanId != null);
        result.addAllWarnings(warnings);

        List<PlanConflictDTO> conflicts = checkPlanConflicts(plan, excludePlanId);
        result.addAllConflicts(conflicts);

        return result;
    }

    @Override
    public PlanConflictCheckResultDTO checkPlanConflictsWithWarnings(SportPlan plan, SportPlan originalRequest, Long excludePlanId) {
        PlanConflictCheckResultDTO result = new PlanConflictCheckResultDTO();

        List<String> warnings = validatePlanFields(plan, originalRequest, excludePlanId != null);
        result.addAllWarnings(warnings);

        List<PlanConflictDTO> conflicts = checkPlanConflicts(plan, excludePlanId);
        result.addAllConflicts(conflicts);

        return result;
    }

    @Override
    public SportPlan mergePlanWithExisting(SportPlan plan) {
        if (plan.getId() == null) {
            return plan;
        }

        SportPlan existing = getById(plan.getId());
        if (existing == null) {
            throw new RuntimeException("计划不存在");
        }

        SportPlan merged = new SportPlan();
        BeanUtils.copyProperties(existing, merged);

        if (plan.getTitle() != null) {
            merged.setTitle(plan.getTitle());
        }
        if (plan.getDescription() != null) {
            merged.setDescription(plan.getDescription());
        }
        if (plan.getSportTypeId() != null) {
            merged.setSportTypeId(plan.getSportTypeId());
        }
        if (plan.getTargetDuration() != null) {
            merged.setTargetDuration(plan.getTargetDuration());
        }
        if (plan.getTargetFrequency() != null) {
            merged.setTargetFrequency(plan.getTargetFrequency());
        }
        if (plan.getStartDate() != null) {
            merged.setStartDate(plan.getStartDate());
        }
        if (plan.getEndDate() != null) {
            merged.setEndDate(plan.getEndDate());
        }
        if (plan.getReminderTime() != null) {
            merged.setReminderTime(plan.getReminderTime());
        }
        if (plan.getReminderEnabled() != null) {
            merged.setReminderEnabled(plan.getReminderEnabled());
        }
        if (plan.getWeekdays() != null) {
            merged.setWeekdays(plan.getWeekdays());
        }
        if (plan.getTrainingTimeSlot() != null) {
            merged.setTrainingTimeSlot(plan.getTrainingTimeSlot());
        }
        if (plan.getStatus() != null) {
            merged.setStatus(plan.getStatus());
        }

        return merged;
    }

    @Override
    public List<String> validatePlanFields(SportPlan plan, SportPlan originalRequest, boolean isUpdate) {
        List<String> warnings = new ArrayList<>();

        if (plan.getUserId() == null) {
            warnings.add("用户ID为空，无法进行冲突检测");
            return warnings;
        }

        boolean isWeekdaysFromOldData = isUpdate && originalRequest != null
                && (originalRequest.getWeekdays() == null || originalRequest.getWeekdays().isEmpty())
                && plan.getWeekdays() != null && !plan.getWeekdays().isEmpty();

        if (plan.getWeekdays() == null || plan.getWeekdays().isEmpty()) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：训练日(weekdays)" + source + "未设置，系统将默认按每天执行来检测日期重叠和提醒时间冲突");
        } else if (isWeekdaysFromOldData) {
            warnings.add("风险提示：训练日(weekdays)沿用旧数据，系统将按旧数据的训练日设置检测冲突");
        }

        boolean isStartDateFromOldData = isUpdate && originalRequest != null
                && originalRequest.getStartDate() == null
                && plan.getStartDate() != null;

        if (plan.getStartDate() == null) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：开始日期(startDate)" + source + "未设置，系统将默认从今天开始计算执行周期");
        } else if (isStartDateFromOldData) {
            warnings.add("风险提示：开始日期(startDate)沿用旧数据，执行周期计算以旧数据为准");
        }

        boolean isEndDateFromOldData = isUpdate && originalRequest != null
                && originalRequest.getEndDate() == null
                && plan.getEndDate() != null;

        if (plan.getEndDate() == null) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：结束日期(endDate)" + source + "未设置，系统将默认按一年后计算执行周期");
        } else if (isEndDateFromOldData) {
            warnings.add("风险提示：结束日期(endDate)沿用旧数据，执行周期计算以旧数据为准");
        }

        boolean isReminderTimeFromOldData = isUpdate && originalRequest != null
                && originalRequest.getReminderTime() == null
                && plan.getReminderTime() != null;

        if (plan.getReminderEnabled() != null && plan.getReminderEnabled() == 1 && plan.getReminderTime() == null) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：已开启提醒但提醒时间(reminderTime)" + source + "未设置");
        } else if (isReminderTimeFromOldData && plan.getReminderEnabled() != null && plan.getReminderEnabled() == 1) {
            warnings.add("风险提示：提醒时间(reminderTime)沿用旧数据，提醒冲突检测以旧数据为准");
        }

        boolean isTrainingTimeSlotFromOldData = isUpdate && originalRequest != null
                && (originalRequest.getTrainingTimeSlot() == null || originalRequest.getTrainingTimeSlot().isEmpty())
                && plan.getTrainingTimeSlot() != null && !plan.getTrainingTimeSlot().isEmpty();

        if (plan.getTrainingTimeSlot() == null || plan.getTrainingTimeSlot().isEmpty()) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：训练时段(trainingTimeSlot)" + source + "未设置，无法检测训练时段冲突");
        } else if (isTrainingTimeSlotFromOldData) {
            warnings.add("风险提示：训练时段(trainingTimeSlot)沿用旧数据，时段冲突检测以旧数据为准");
        }

        boolean isSportTypeIdFromOldData = isUpdate && originalRequest != null
                && originalRequest.getSportTypeId() == null
                && plan.getSportTypeId() != null;

        if (plan.getSportTypeId() == null) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：运动类型(sportTypeId)" + source + "未设置");
        } else if (isSportTypeIdFromOldData) {
            warnings.add("风险提示：运动类型(sportTypeId)沿用旧数据");
        }

        boolean isTargetFromOldData = isUpdate && originalRequest != null
                && originalRequest.getTargetDuration() == null && originalRequest.getTargetFrequency() == null
                && (plan.getTargetDuration() != null || plan.getTargetFrequency() != null);

        if (plan.getTargetDuration() == null && plan.getTargetFrequency() == null) {
            String source = isUpdate ? "（旧数据或本次请求未传）" : "（本次请求未传）";
            warnings.add("风险提示：目标时长(targetDuration)和目标频率(targetFrequency)" + source + "均未设置");
        } else if (isTargetFromOldData) {
            warnings.add("风险提示：目标数据沿用旧数据");
        }

        if (isUpdate && originalRequest != null && originalRequest.getId() != null) {
            SportPlan existing = getById(originalRequest.getId());
            if (existing != null) {
                boolean hasPartialUpdate = false;
                List<String> updatedFields = new ArrayList<>();
                List<String> missingFields = new ArrayList<>();

                if (originalRequest.getTitle() != null && !Objects.equals(originalRequest.getTitle(), existing.getTitle())) {
                    updatedFields.add("计划名称");
                    hasPartialUpdate = true;
                }
                if (originalRequest.getWeekdays() != null && !Objects.equals(originalRequest.getWeekdays(), existing.getWeekdays())) {
                    updatedFields.add("训练日");
                    hasPartialUpdate = true;
                }
                if (originalRequest.getStartDate() != null && !Objects.equals(originalRequest.getStartDate(), existing.getStartDate())) {
                    updatedFields.add("开始日期");
                    hasPartialUpdate = true;
                }
                if (originalRequest.getEndDate() != null && !Objects.equals(originalRequest.getEndDate(), existing.getEndDate())) {
                    updatedFields.add("结束日期");
                    hasPartialUpdate = true;
                }
                if (originalRequest.getReminderTime() != null && !Objects.equals(originalRequest.getReminderTime(), existing.getReminderTime())) {
                    updatedFields.add("提醒时间");
                    hasPartialUpdate = true;
                }
                if (originalRequest.getTrainingTimeSlot() != null && !Objects.equals(originalRequest.getTrainingTimeSlot(), existing.getTrainingTimeSlot())) {
                    updatedFields.add("训练时段");
                    hasPartialUpdate = true;
                }

                if (originalRequest.getWeekdays() == null || originalRequest.getWeekdays().isEmpty()) {
                    missingFields.add("训练日");
                }
                if (originalRequest.getStartDate() == null) {
                    missingFields.add("开始日期");
                }
                if (originalRequest.getEndDate() == null) {
                    missingFields.add("结束日期");
                }
                if (originalRequest.getReminderEnabled() != null && originalRequest.getReminderEnabled() == 1 && originalRequest.getReminderTime() == null) {
                    missingFields.add("提醒时间");
                }
                if (originalRequest.getTrainingTimeSlot() == null || originalRequest.getTrainingTimeSlot().isEmpty()) {
                    missingFields.add("训练时段");
                }

                if (hasPartialUpdate) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("提示：本次为部分字段更新，已提交字段 [").append(String.join("、", updatedFields)).append("]");
                    if (!missingFields.isEmpty()) {
                        sb.append("，未提交字段 [").append(String.join("、", missingFields)).append("] 将沿用旧数据");
                    }
                    sb.append("，系统已基于合并后的完整数据进行冲突检测");
                    warnings.add(sb.toString());
                }
            }
        }

        return warnings;
    }

}
