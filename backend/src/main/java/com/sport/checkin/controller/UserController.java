package com.sport.checkin.controller;

import com.sport.checkin.common.Result;
import com.sport.checkin.dto.LoginDTO;
import com.sport.checkin.dto.RegisterDTO;
import com.sport.checkin.entity.SysUser;
import com.sport.checkin.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/register")
    public Result<SysUser> register(@RequestBody RegisterDTO dto) {
        try {
            SysUser user = sysUserService.register(dto);
            user.setPassword(null);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody LoginDTO dto) {
        try {
            SysUser user = sysUserService.login(dto);
            user.setPassword(null);
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SysUser> getInfo(@PathVariable Long id) {
        try {
            SysUser user = sysUserService.getInfo(id);
            if (user != null) {
                user.setPassword(null);
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<SysUser> update(@RequestBody SysUser user) {
        try {
            user.setPassword(null);
            sysUserService.updateById(user);
            SysUser updated = sysUserService.getById(user.getId());
            updated.setPassword(null);
            return Result.success("更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
