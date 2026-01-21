package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.EncouragementCard;
import com.example.qr_code.mapper.EncouragementCardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * EncouragementService 鼓励卡片业务逻辑层
 */
@Service
public class EncouragementService {
    
    @Autowired
    private EncouragementCardMapper encouragementCardMapper;
    
    // 违禁词列表
    private static final List<String> FORBIDDEN_WORDS = Arrays.asList(
        "傻", "笨", "蠢", "滚", "死", "废物", "垃圾", "白痴"
    );
    
    /**
     * 获取随机鼓励卡片列表
     */
    public List<Map<String, Object>> getRandomCards(int limit) {
        List<EncouragementCard> cards = encouragementCardMapper.selectRandomCards(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (EncouragementCard card : cards) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", card.getId());
            item.put("emoji", card.getEmoji());
            item.put("message", card.getMessage());
            item.put("likes", card.getLikes());
            item.put("author", "匿名用户"); // 匿名展示
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * 创建新的鼓励卡片
     */
    public Map<String, Object> createCard(Long userId, String emoji, String message) {
        Map<String, Object> result = new HashMap<>();
        
        // 内容校验
        if (message == null || message.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "鼓励内容不能为空");
            return result;
        }
        
        if (message.length() > 100) {
            result.put("success", false);
            result.put("message", "鼓励内容不能超过100字");
            return result;
        }
        
        // 违禁词检查
        String lowerMessage = message.toLowerCase();
        for (String word : FORBIDDEN_WORDS) {
            if (lowerMessage.contains(word)) {
                result.put("success", false);
                result.put("message", "内容包含不当词汇，请修改后重试");
                return result;
            }
        }
        
        // 创建卡片
        EncouragementCard card = new EncouragementCard();
        card.setUserId(userId);
        card.setEmoji(emoji != null ? emoji : "🌸");
        card.setMessage(message.trim());
        card.setLikes(0);
        card.setStatus(1);
        card.setCreatedAt(LocalDateTime.now());
        
        encouragementCardMapper.insert(card);
        
        result.put("success", true);
        result.put("id", card.getId());
        result.put("message", "发送成功");
        return result;
    }
    
    /**
     * 点赞卡片
     */
    public boolean likeCard(Long cardId) {
        return encouragementCardMapper.incrementLikes(cardId) > 0;
    }
    
    /**
     * 隐藏卡片（管理员）
     */
    public boolean hideCard(Long cardId) {
        EncouragementCard card = encouragementCardMapper.selectById(cardId);
        if (card != null) {
            card.setStatus(0);
            return encouragementCardMapper.updateById(card) > 0;
        }
        return false;
    }
    
    /**
     * 获取所有卡片（管理员）
     */
    public List<EncouragementCard> getAllCards() {
        QueryWrapper<EncouragementCard> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        return encouragementCardMapper.selectList(wrapper);
    }
}
