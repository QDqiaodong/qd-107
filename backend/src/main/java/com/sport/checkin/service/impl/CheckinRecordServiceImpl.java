package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.entity.CheckinRecord;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.CheckinRecordMapper;
import com.sport.checkin.service.CheckinRecordService;
import com.sport.checkin.service.SportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {

    @Autowired
    private SportTypeService sportTypeService;

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
    public boolean addCheckin(CheckinRecord record) {
        record.setCheckinDate(LocalDate.now());
        record.setCheckinTime(LocalDateTime.now());
        if (record.getCalorie() == null && record.getSportTypeId() != null) {
            SportType sportType = sportTypeService.getById(record.getSportTypeId());
            if (sportType != null && sportType.getCaloriePerMinute() != null) {
                record.setCalorie(sportType.getCaloriePerMinute().multiply(new java.math.BigDecimal(record.getDuration())));
            }
        }
        return save(record);
    }

}
