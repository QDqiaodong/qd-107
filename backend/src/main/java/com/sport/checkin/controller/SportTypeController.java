package com.sport.checkin.controller;

import com.sport.checkin.common.Result;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.service.SportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sport-type")
public class SportTypeController {

    @Autowired
    private SportTypeService sportTypeService;

    @GetMapping("/hot")
    public Result<List<SportType>> getHotTypes() {
        try {
            List<SportType> list = sportTypeService.getHotTypes();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<SportType>> getAllTypes() {
        try {
            List<SportType> list = sportTypeService.getAllTypes();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
