package com.sport.checkin.controller;

import com.sport.checkin.common.Result;
import com.sport.checkin.entity.SportDynamic;
import com.sport.checkin.service.SportDynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dynamic")
public class DynamicController {

    @Autowired
    private SportDynamicService sportDynamicService;

    @GetMapping("/public")
    public Result<List<SportDynamic>> getPublicList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<SportDynamic> list = sportDynamicService.getPublicList(page, size);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/type/{sportTypeId}")
    public Result<List<SportDynamic>> getListByType(
            @PathVariable Long sportTypeId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<SportDynamic> list = sportDynamicService.getListByType(sportTypeId, page, size);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<List<SportDynamic>> getMyList(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<SportDynamic> list = sportDynamicService.getMyList(userId, page, size);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SportDynamic> getDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long viewerId) {
        try {
            SportDynamic dynamic = sportDynamicService.getDetail(id, viewerId);
            return Result.success(dynamic);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<SportDynamic> addDynamic(@RequestBody SportDynamic dynamic) {
        try {
            sportDynamicService.addDynamic(dynamic);
            return Result.success("发布成功", dynamic);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDynamic(@PathVariable Long id, @RequestParam Long userId) {
        try {
            sportDynamicService.deleteDynamic(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/like")
    public Result<String> likeDynamic(@RequestBody Map<String, Long> params) {
        try {
            Long id = params.get("id");
            Long userId = params.get("userId");
            sportDynamicService.likeDynamic(id, userId);
            return Result.success("点赞成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
