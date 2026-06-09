package com.sport.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sport.checkin.entity.DynamicLike;
import com.sport.checkin.entity.SportDynamic;
import com.sport.checkin.mapper.DynamicLikeMapper;
import com.sport.checkin.mapper.SportDynamicMapper;
import com.sport.checkin.service.SportDynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SportDynamicServiceImpl extends ServiceImpl<SportDynamicMapper, SportDynamic> implements SportDynamicService {

    private static final String HOT_DYNAMICS_KEY = "sport:hot:dynamics";
    private static final String DYNAMIC_LIKE_KEY = "sport:dynamic:like:";

    @Autowired
    private DynamicLikeMapper dynamicLikeMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<SportDynamic> getPublicList(Integer page, Integer size) {
        if (page != null && page == 1 && size != null && size <= 20) {
            List<SportDynamic> cacheList = (List<SportDynamic>) redisTemplate.opsForValue().get(HOT_DYNAMICS_KEY);
            if (cacheList != null && !cacheList.isEmpty()) {
                return cacheList;
            }
        }
        LambdaQueryWrapper<SportDynamic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportDynamic::getIsPublic, 1)
                .orderByDesc(SportDynamic::getLikeCount)
                .orderByDesc(SportDynamic::getCreateTime);
        List<SportDynamic> result;
        if (page != null && size != null) {
            Page<SportDynamic> pageParam = new Page<>(page, size);
            result = page(pageParam, wrapper).getRecords();
        } else {
            result = list(wrapper);
        }
        if (page != null && page == 1 && size != null && size <= 20) {
            redisTemplate.opsForValue().set(HOT_DYNAMICS_KEY, result, 30, TimeUnit.MINUTES);
        }
        return result;
    }

    @Override
    public List<SportDynamic> getListByType(Long sportTypeId, Integer page, Integer size) {
        LambdaQueryWrapper<SportDynamic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportDynamic::getIsPublic, 1)
                .eq(SportDynamic::getSportTypeId, sportTypeId)
                .orderByDesc(SportDynamic::getCreateTime);
        if (page != null && size != null) {
            Page<SportDynamic> pageParam = new Page<>(page, size);
            return page(pageParam, wrapper).getRecords();
        }
        return list(wrapper);
    }

    @Override
    public List<SportDynamic> getMyList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<SportDynamic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportDynamic::getUserId, userId)
                .orderByDesc(SportDynamic::getCreateTime);
        if (page != null && size != null) {
            Page<SportDynamic> pageParam = new Page<>(page, size);
            return page(pageParam, wrapper).getRecords();
        }
        return list(wrapper);
    }

    @Override
    public SportDynamic getDetail(Long id) {
        SportDynamic dynamic = getById(id);
        if (dynamic != null && dynamic.getIsPublic() == 1) {
            dynamic.setViewCount(dynamic.getViewCount() + 1);
            updateById(dynamic);
        }
        return dynamic;
    }

    @Override
    public boolean addDynamic(SportDynamic dynamic) {
        dynamic.setLikeCount(0);
        dynamic.setCommentCount(0);
        dynamic.setViewCount(0);
        if (dynamic.getIsPublic() == null) {
            dynamic.setIsPublic(1);
        }
        redisTemplate.delete(HOT_DYNAMICS_KEY);
        return save(dynamic);
    }

    @Override
    public boolean deleteDynamic(Long id, Long userId) {
        SportDynamic dynamic = getById(id);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }
        if (!dynamic.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除");
        }
        redisTemplate.delete(HOT_DYNAMICS_KEY);
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean likeDynamic(Long id, Long userId) {
        String likeKey = DYNAMIC_LIKE_KEY + id + ":" + userId;
        Boolean liked = redisTemplate.hasKey(likeKey);
        if (Boolean.TRUE.equals(liked)) {
            throw new RuntimeException("已点赞");
        }
        LambdaQueryWrapper<DynamicLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DynamicLike::getDynamicId, id)
                .eq(DynamicLike::getUserId, userId);
        DynamicLike existLike = dynamicLikeMapper.selectOne(wrapper);
        if (existLike != null) {
            throw new RuntimeException("已点赞");
        }
        DynamicLike like = new DynamicLike();
        like.setDynamicId(id);
        like.setUserId(userId);
        dynamicLikeMapper.insert(like);
        SportDynamic dynamic = getById(id);
        if (dynamic != null) {
            dynamic.setLikeCount(dynamic.getLikeCount() + 1);
            updateById(dynamic);
        }
        redisTemplate.opsForValue().set(likeKey, 1, 1, TimeUnit.DAYS);
        redisTemplate.delete(HOT_DYNAMICS_KEY);
        return true;
    }

}
