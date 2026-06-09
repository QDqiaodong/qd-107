package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.entity.SportPlan;
import com.sport.checkin.mapper.SportPlanMapper;
import com.sport.checkin.service.SportPlanService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class SportPlanServiceImpl extends ServiceImpl<SportPlanMapper, SportPlan> implements SportPlanService {

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

}
