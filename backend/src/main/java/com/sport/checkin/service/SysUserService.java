package com.sport.checkin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sport.checkin.dto.LoginDTO;
import com.sport.checkin.dto.RegisterDTO;
import com.sport.checkin.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser register(RegisterDTO dto);

    SysUser login(LoginDTO dto);

    SysUser getInfo(Long id);

}
