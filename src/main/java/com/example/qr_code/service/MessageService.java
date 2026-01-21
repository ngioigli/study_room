package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.qr_code.entity.MessageBoard;
import com.example.qr_code.entity.MessageReply;
import com.example.qr_code.mapper.MessageBoardMapper;
import com.example.qr_code.mapper.MessageReplyMapper;
import com.example.qr_code.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * MessageService 留言板服务类
 * 处理留言、回复等业务逻辑
 */
@Service
public class MessageService {

    @Autowired
    private MessageBoardMapper messageBoardMapper;

    @Autowired
    private MessageReplyMapper messageReplyMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 获取留言列表（分页）
     */
    public List<MessageBoard> getMessages(int page, int size) {
        int offset = (page - 1) * size;
        return messageBoardMapper.selectMessagesWithUser(offset, size);
    }

    /**
     * 获取留言总数
     */
    public int getMessageCount() {
        return messageBoardMapper.countActiveMessages();
    }

    /**
     * 发布留言
     */
    public boolean createMessage(Long userId, String content) {
        // 检查敏感词
        if (containsForbiddenWords(content)) {
            return false;
        }
        
        MessageBoard message = new MessageBoard();
        message.setUserId(userId);
        message.setContent(content);
        message.setStatus(1);
        message.setReplyCount(0);
        
        return messageBoardMapper.insert(message) > 0;
    }

    /**
     * 编辑留言
     */
    public boolean updateMessage(Long messageId, Long userId, String content) {
        // 检查敏感词
        if (containsForbiddenWords(content)) {
            return false;
        }
        
        UpdateWrapper<MessageBoard> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId)
               .eq("user_id", userId)  // 只能编辑自己的留言
               .set("content", content);
        
        return messageBoardMapper.update(null, wrapper) > 0;
    }

    /**
     * 删除留言（软删除）
     */
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        // 检查是否是自己的留言
        MessageBoard message = messageBoardMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            return false;
        }
        
        // 删除留言
        return messageBoardMapper.deleteById(messageId) > 0;
    }

    /**
     * 获取留言的回复列表
     */
    public List<MessageReply> getReplies(Long messageId) {
        return messageReplyMapper.selectRepliesWithUser(messageId);
    }

    /**
     * 发布回复
     */
    @Transactional
    public boolean createReply(Long messageId, Long userId, String content) {
        // 检查敏感词
        if (containsForbiddenWords(content)) {
            return false;
        }
        
        // 检查留言是否存在
        MessageBoard message = messageBoardMapper.selectById(messageId);
        if (message == null || message.getStatus() != 1) {
            return false;
        }
        
        MessageReply reply = new MessageReply();
        reply.setMessageId(messageId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setStatus(1);
        
        if (messageReplyMapper.insert(reply) > 0) {
            // 更新回复数量
            messageBoardMapper.updateReplyCount(messageId, 1);
            return true;
        }
        return false;
    }

    /**
     * 删除回复
     */
    @Transactional
    public boolean deleteReply(Long replyId, Long userId) {
        // 检查是否是自己的回复
        MessageReply reply = messageReplyMapper.selectById(replyId);
        if (reply == null || !reply.getUserId().equals(userId)) {
            return false;
        }
        
        if (messageReplyMapper.deleteById(replyId) > 0) {
            // 更新回复数量
            messageBoardMapper.updateReplyCount(reply.getMessageId(), -1);
            return true;
        }
        return false;
    }

    /**
     * 封禁/解封留言（管理员）
     */
    public boolean toggleMessageStatus(Long messageId, int status) {
        UpdateWrapper<MessageBoard> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId).set("status", status);
        return messageBoardMapper.update(null, wrapper) > 0;
    }

    /**
     * 封禁/解封回复（管理员）
     */
    public boolean toggleReplyStatus(Long replyId, int status) {
        UpdateWrapper<MessageReply> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", replyId).set("status", status);
        return messageReplyMapper.update(null, wrapper) > 0;
    }

    /**
     * 获取所有留言（管理员用，包含被封禁的）
     */
    public List<MessageBoard> getAllMessages(int page, int size) {
        QueryWrapper<MessageBoard> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at")
               .last("LIMIT " + ((page - 1) * size) + ", " + size);
        return messageBoardMapper.selectList(wrapper);
    }

    /**
     * 清理过期留言
     */
    public int cleanExpiredMessages() {
        String daysStr = systemConfigMapper.getConfigValue("message_expire_days");
        int days = daysStr != null ? Integer.parseInt(daysStr) : 14;
        return messageBoardMapper.deleteExpiredMessages(days);
    }

    /**
     * 检查是否包含禁用词
     */
    private boolean containsForbiddenWords(String content) {
        String forbiddenStr = systemConfigMapper.getConfigValue("message_forbidden_words");
        if (forbiddenStr == null || forbiddenStr.isEmpty()) {
            return false;
        }
        
        String[] forbiddenWords = forbiddenStr.split(",");
        String lowerContent = content.toLowerCase();
        
        return Arrays.stream(forbiddenWords)
                     .anyMatch(word -> lowerContent.contains(word.trim().toLowerCase()));
    }
}
