package com.sport.checkin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sport.checkin.dto.CheckinResultDTO;
import com.sport.checkin.entity.CheckinRecord;

import java.util.List;

public interface CheckinRecordService extends IService<CheckinRecord> {

    List<CheckinRecord> getCheckinList(Long userId, Integer page, Integer size);

    CheckinRecord getCheckinDetail(Long id);

    CheckinResultDTO addCheckin(CheckinRecord record);

}
