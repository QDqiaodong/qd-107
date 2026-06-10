package com.sport.checkin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sport.checkin.entity.SportType;

import java.util.List;

public interface SportTypeService extends IService<SportType> {

    List<SportType> getHotTypes();

    List<SportType> getAllTypes();

    void incrementHotCount(Long sportTypeId);

}
