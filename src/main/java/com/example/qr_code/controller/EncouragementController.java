package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.EncouragementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EncouragementController 鼓励卡片控制器
 */
@RestController
@RequestMapping("/api/encouragement")
public class EncouragementController {
    
    @Autowired
    private EncouragementService encouragementService;
    
    /**
     * 获取随机鼓励卡片列表
     */
    @GetMapping("/list")
    public Map<String, Object> getCards(
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> cards = encouragementService.getRandomCards(Math.min(limit, 50));
            result.put("success", true);
            result.put("cards", cards);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 创建新的鼓励卡片
     */
    @PostMapping("/create")
    public Map<String, Object> createCard(
            @RequestBody Map<String, String> params,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        Long userId = user != null ? user.getId() : null;
        
        String emoji = params.get("emoji");
        String message = params.get("message");
        
        return encouragementService.createCard(userId, emoji, message);
    }
    
    /**
     * 点赞卡片
     */
    @PostMapping("/like/{id}")
    public Map<String, Object> likeCard(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        boolean success = encouragementService.likeCard(id);
        result.put("success", success);
        
        return result;
    }
    
    /**
     * 隐藏卡片（管理员）
     */
    @PostMapping("/hide/{id}")
    public Map<String, Object> hideCard(
            @PathVariable Long id,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }
        
        boolean success = encouragementService.hideCard(id);
        result.put("success", success);
        
        return result;
    }
}
