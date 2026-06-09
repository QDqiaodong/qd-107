package com.sport.checkin.controller;

import com.sport.checkin.common.Result;
import com.sport.checkin.entity.CheckinRecord;
import com.sport.checkin.service.CheckinRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinRecordService checkinRecordService;

    @GetMapping("/list")
    public Result<List<CheckinRecord>> getCheckinList(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<CheckinRecord> list = checkinRecordService.getCheckinList(userId, page, size);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<CheckinRecord> getCheckinDetail(@PathVariable Long id) {
        try {
            CheckinRecord record = checkinRecordService.getCheckinDetail(id);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<CheckinRecord> addCheckin(@RequestBody CheckinRecord record) {
        try {
            checkinRecordService.addCheckin(record);
            return Result.success("打卡成功", record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
