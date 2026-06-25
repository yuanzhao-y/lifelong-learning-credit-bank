package com.zhousheng.llcb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhousheng.llcb.TestMybatis;
import com.zhousheng.llcb.entity.SysDict;
import com.zhousheng.llcb.entity.SysDictItem;
import com.zhousheng.llcb.mapper.SysDictItemMapper;
import com.zhousheng.llcb.mapper.SysDictMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictCacheServiceTest {

    @BeforeAll
    static void metadata() {
        TestMybatis.initialize(SysDict.class, SysDictItem.class);
    }

    @Mock private SysDictMapper dictMapper;
    @Mock private SysDictItemMapper itemMapper;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private DictCacheService service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new DictCacheService(dictMapper, itemMapper, redisTemplate, new ObjectMapper());
    }

    @Test
    void returnsCachedItemsWithoutDatabaseQuery() {
        when(valueOperations.get("llcb:dict:status"))
                .thenReturn("[{\"itemCode\":\"enabled\",\"itemName\":\"Enabled\"}]");

        assertThat(service.items("status")).extracting(SysDictItem::getItemCode).containsExactly("enabled");
        verify(dictMapper, never()).selectOne(any());
    }

    @Test
    void loadsDatabaseAndCachesItemsOnMiss() {
        when(valueOperations.get(anyString())).thenReturn(null);
        SysDict dict = new SysDict();
        dict.setId(2L);
        when(dictMapper.selectOne(any())).thenReturn(dict);
        SysDictItem item = new SysDictItem();
        item.setItemCode("enabled");
        when(itemMapper.selectList(any())).thenReturn(List.of(item));

        assertThat(service.items("status")).containsExactly(item);
        verify(valueOperations).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void missingDictionaryReturnsEmptyList() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(dictMapper.selectOne(any())).thenReturn(null);

        assertThat(service.items("missing")).isEmpty();
    }

    @Test
    void evictDeletesNamedCacheKey() {
        service.evict("status");

        verify(redisTemplate).delete("llcb:dict:status");
    }
}
