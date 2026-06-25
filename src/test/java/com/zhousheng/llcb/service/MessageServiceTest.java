package com.zhousheng.llcb.service;

import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.mapper.SysMessageMapper;
import com.zhousheng.llcb.mapper.SysMessageReceiverMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @BeforeAll
    static void initTableMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                com.zhousheng.llcb.entity.SysMessageReceiver.class);
    }

    @Mock
    private SysMessageMapper messageMapper;
    @Mock
    private SysMessageReceiverMapper receiverMapper;

    private MessageService service;

    @BeforeEach
    void setUp() {
        service = new MessageService(messageMapper, receiverMapper);
    }

    @Test
    void rejectsReadingAnotherUsersMessage() {
        when(receiverMapper.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> service.requireReceiver(7L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void markReadDoesNotUpdateWhenOwnershipCheckFails() {
        when(receiverMapper.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> service.markRead(7L, 99L)).isInstanceOf(BusinessException.class);

        verify(receiverMapper, never()).update(any(), any());
    }

    @Test
    void markReadUpdatesOwnedMessage() {
        when(receiverMapper.selectCount(any())).thenReturn(1L);

        service.markRead(7L, 99L);

        verify(receiverMapper).update(any(), any());
    }
}
