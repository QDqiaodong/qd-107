package com.sport.checkin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sport.checkin.entity.SportDynamic;

import java.util.List;

public interface SportDynamicService extends IService<SportDynamic> {

    List<SportDynamic> getPublicList(Integer page, Integer size);

    List<SportDynamic> getListByType(Long sportTypeId, Integer page, Integer size);

    List<SportDynamic> getMyList(Long userId, Integer page, Integer size);

    SportDynamic getDetail(Long id);

    boolean addDynamic(SportDynamic dynamic);

    boolean deleteDynamic(Long id, Long userId);

    boolean likeDynamic(Long id, Long userId);

}
