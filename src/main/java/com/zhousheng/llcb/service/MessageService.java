package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.entity.SysMessage;
import com.zhousheng.llcb.entity.SysMessageReceiver;
import com.zhousheng.llcb.mapper.SysMessageMapper;
import com.zhousheng.llcb.mapper.SysMessageReceiverMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageService {

    private final SysMessageMapper messageMapper;
    private final SysMessageReceiverMapper receiverMapper;

    public MessageService(SysMessageMapper messageMapper, SysMessageReceiverMapper receiverMapper) {
        this.messageMapper = messageMapper;
        this.receiverMapper = receiverMapper;
    }

    @Transactional
    public void send(Long receiverId, String title, String content, String type, String scene, String bizType, Long bizId) {
        SysMessage message = new SysMessage();
        message.setMessageTitle(title);
        message.setMessageContent(content);
        message.setMessageType(type);
        message.setTriggerScene(scene);
        message.setBizType(bizType);
        message.setBizId(bizId);
        message.setSentAt(LocalDateTime.now());
        messageMapper.insert(message);

        SysMessageReceiver receiver = new SysMessageReceiver();
        receiver.setMessageId(message.getId());
        receiver.setReceiverId(receiverId);
        receiver.setReadStatus(Constants.STATUS_UNREAD);
        receiver.setReceiverDeleted(0);
        receiverMapper.insert(receiver);
    }

    public void markRead(Long receiverId, Long messageId) {
        requireReceiver(receiverId, messageId);
        receiverMapper.update(null, new LambdaUpdateWrapper<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverId, receiverId)
                .eq(SysMessageReceiver::getMessageId, messageId)
                .set(SysMessageReceiver::getReadStatus, Constants.STATUS_READ)
                .set(SysMessageReceiver::getReadAt, LocalDateTime.now()));
    }

    public void markAllRead(Long receiverId) {
        receiverMapper.update(null, new LambdaUpdateWrapper<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverId, receiverId)
                .eq(SysMessageReceiver::getReadStatus, Constants.STATUS_UNREAD)
                .set(SysMessageReceiver::getReadStatus, Constants.STATUS_READ)
                .set(SysMessageReceiver::getReadAt, LocalDateTime.now()));
    }

    public void requireReceiver(Long receiverId, Long messageId) {
        long count = receiverMapper.selectCount(new LambdaQueryWrapper<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverId, receiverId)
                .eq(SysMessageReceiver::getMessageId, messageId)
                .eq(SysMessageReceiver::getReceiverDeleted, 0));
        if (count == 0) {
            throw new BusinessException(404, "消息不存在");
        }
    }
}
