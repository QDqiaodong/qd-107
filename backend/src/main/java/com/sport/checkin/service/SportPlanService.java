package com.sport.checkin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sport.checkin.entity.SportPlan;

import java.util.List;

public interface SportPlanService extends IService<SportPlan> {

    List<SportPlan> getPlanList(Long userId);

    SportPlan getPlanDetail(Long id);

    boolean addPlan(SportPlan plan);

    boolean updatePlan(SportPlan plan);

    boolean deletePlan(Long id);

    boolean setReminder(Long id, Integer enabled, String reminderTime);

}
