package com.sport.checkin.controller;

import com.sport.checkin.common.Result;
import com.sport.checkin.dto.PlanConflictCheckResultDTO;
import com.sport.checkin.dto.PlanConflictDTO;
import com.sport.checkin.dto.PlanExecutionSnapshotDTO;
import com.sport.checkin.entity.SportPlan;
import com.sport.checkin.service.SportPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plan")
public class SportPlanController {

    @Autowired
    private SportPlanService sportPlanService;

    @GetMapping("/list")
    public Result<List<SportPlan>> getPlanList(@RequestParam Long userId) {
        try {
            List<SportPlan> list = sportPlanService.getPlanList(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SportPlan> getPlanDetail(@PathVariable Long id) {
        try {
            SportPlan plan = sportPlanService.getPlanDetail(id);
            return Result.success(plan);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<SportPlan> addPlan(@RequestBody SportPlan plan) {
        try {
            List<String> warnings = sportPlanService.validatePlanFields(plan, plan, false);
            sportPlanService.addPlan(plan);
            Result<SportPlan> result = Result.success("创建成功", plan);
            result.addWarnings(warnings);
            return result;
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<SportPlan> updatePlan(@RequestBody SportPlan plan) {
        try {
            SportPlan mergedPlan = sportPlanService.mergePlanWithExisting(plan);
            List<String> warnings = sportPlanService.validatePlanFields(mergedPlan, plan, true);
            sportPlanService.updatePlan(plan, mergedPlan);
            Result<SportPlan> result = Result.success("更新成功", mergedPlan);
            result.addWarnings(warnings);
            return result;
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deletePlan(@PathVariable Long id) {
        try {
            sportPlanService.deletePlan(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reminder")
    public Result<String> setReminder(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer enabled = Integer.valueOf(params.get("enabled").toString());
            String reminderTime = params.get("reminderTime") != null ? params.get("reminderTime").toString() : null;
            sportPlanService.setReminder(id, enabled, reminderTime);
            return Result.success("设置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/execution-snapshots")
    public Result<List<PlanExecutionSnapshotDTO>> getExecutionSnapshots(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        try {
            List<PlanExecutionSnapshotDTO> snapshots = sportPlanService.getExecutionSnapshots(userId, status);
            return Result.success(snapshots);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/check-conflicts")
    public Result<PlanConflictCheckResultDTO> checkConflicts(
            @RequestBody SportPlan plan,
            @RequestParam(required = false) Long excludePlanId) {
        try {
            PlanConflictCheckResultDTO result = sportPlanService.checkPlanConflictsWithWarnings(plan, excludePlanId);
            Result<PlanConflictCheckResultDTO> response = Result.success(result);
            response.addWarnings(result.getWarnings());
            return response;
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
