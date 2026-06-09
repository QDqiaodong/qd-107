package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.entity.SportType;
import com.sport.checkin.mapper.SportTypeMapper;
import com.sport.checkin.service.SportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SportTypeServiceImpl extends ServiceImpl<SportTypeMapper, SportType> implements SportTypeService {

    private static final String HOT_TYPES_KEY = "sport:hot:types";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<SportType> getHotTypes() {
        List<SportType> cacheList = (List<SportType>) redisTemplate.opsForValue().get(HOT_TYPES_KEY);
        if (cacheList != null && !cacheList.isEmpty()) {
            return cacheList;
        }
        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SportType::getHotCount)
                .orderByAsc(SportType::getSort)
                .last("limit 8");
        List<SportType> list = list(wrapper);
        redisTemplate.opsForValue().set(HOT_TYPES_KEY, list, 1, TimeUnit.HOURS);
        return list;
    }

    @Override
    public List<SportType> getAllTypes() {
        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SportType::getSort);
        return list(wrapper);
    }

}
