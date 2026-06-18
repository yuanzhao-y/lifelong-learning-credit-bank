package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhousheng.llcb.entity.SysDict;
import com.zhousheng.llcb.entity.SysDictItem;
import com.zhousheng.llcb.mapper.SysDictItemMapper;
import com.zhousheng.llcb.mapper.SysDictMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class DictCacheService {

    private final SysDictMapper dictMapper;
    private final SysDictItemMapper dictItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public DictCacheService(SysDictMapper dictMapper,
                            SysDictItemMapper dictItemMapper,
                            StringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper) {
        this.dictMapper = dictMapper;
        this.dictItemMapper = dictItemMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<SysDictItem> items(String dictCode) {
        String key = key(dictCode);
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<>() {
                });
            }
        } catch (Exception ignored) {
            // Cache must never block dictionary reads.
        }
        SysDict dict = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode)
                .last("limit 1"));
        if (dict == null) {
            return List.of();
        }
        List<SysDictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dict.getId())
                .eq(SysDictItem::getStatus, "enabled")
                .orderByAsc(SysDictItem::getSortNo));
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(items), Duration.ofHours(6));
        } catch (Exception ignored) {
            // Redis is an optimization, not the source of truth.
        }
        return items;
    }

    public void evict(String dictCode) {
        redisTemplate.delete(key(dictCode));
    }

    private String key(String dictCode) {
        return "llcb:dict:" + dictCode;
    }
}
