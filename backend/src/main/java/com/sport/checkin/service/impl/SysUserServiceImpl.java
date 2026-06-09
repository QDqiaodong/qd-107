package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.dto.LoginDTO;
import com.sport.checkin.dto.RegisterDTO;
import com.sport.checkin.entity.SysUser;
import com.sport.checkin.mapper.SysUserMapper;
import com.sport.checkin.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser register(RegisterDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser existUser = getOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        save(user);
        return user;
    }

    @Override
    public SysUser login(LoginDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        String md5Password = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public SysUser getInfo(Long id) {
        return getById(id);
    }

}
