package com.example.qr_code.controller;

import com.example.qr_code.entity.MessageBoard;
import com.example.qr_code.entity.MessageReply;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MessageController 留言板控制器
 * 处理留言、回复等功能
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取留言列表
     * GET /api/message/list?page=1&size=10
     */
    @GetMapping("/list")
    public Map<String, Object> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        List<MessageBoard> messages = messageService.getMessages(page, size);
        int total = messageService.getMessageCount();
        
        response.put("success", true);
        response.put("messages", messages);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        return response;
    }

    /**
     * 发布留言
     * POST /api/message/create
     */
    @PostMapping("/create")
    public Map<String, Object> createMessage(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String content = data.get("content");
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "留言内容不能为空");
            return response;
        }
        
        if (content.length() > 500) {
            response.put("success", false);
            response.put("message", "留言内容不能超过500字");
            return response;
        }
        
        boolean success = messageService.createMessage(user.getId(), content.trim());
        if (success) {
            response.put("success", true);
            response.put("message", "留言发布成功");
        } else {
            response.put("success", false);
            response.put("message", "留言包含敏感词，请修改后重试");
        }
        return response;
    }

    /**
     * 编辑留言
     * PUT /api/message/{id}
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String content = data.get("content");
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "留言内容不能为空");
            return response;
        }
        
        boolean success = messageService.updateMessage(id, user.getId(), content.trim());
        if (success) {
            response.put("success", true);
            response.put("message", "留言修改成功");
        } else {
            response.put("success", false);
            response.put("message", "修改失败，可能包含敏感词或无权限");
        }
        return response;
    }

    /**
     * 删除留言
     * DELETE /api/message/{id}
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteMessage(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        boolean success = messageService.deleteMessage(id, user.getId());
        if (success) {
            response.put("success", true);
            response.put("message", "留言删除成功");
        } else {
            response.put("success", false);
            response.put("message", "删除失败，无权限或留言不存在");
        }
        return response;
    }

    /**
     * 获取留言的回复列表
     * GET /api/message/{id}/replies
     */
    @GetMapping("/{id}/replies")
    public Map<String, Object> getReplies(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        List<MessageReply> replies = messageService.getReplies(id);
        
        response.put("success", true);
        response.put("replies", replies);
        return response;
    }

    /**
     * 发布回复
     * POST /api/message/{id}/reply
     */
    @PostMapping("/{id}/reply")
    public Map<String, Object> createReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String content = data.get("content");
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "回复内容不能为空");
            return response;
        }
        
        if (content.length() > 200) {
            response.put("success", false);
            response.put("message", "回复内容不能超过200字");
            return response;
        }
        
        boolean success = messageService.createReply(id, user.getId(), content.trim());
        if (success) {
            response.put("success", true);
            response.put("message", "回复成功");
        } else {
            response.put("success", false);
            response.put("message", "回复失败，可能包含敏感词");
        }
        return response;
    }

    /**
     * 删除回复
     * DELETE /api/message/reply/{id}
     */
    @DeleteMapping("/reply/{id}")
    public Map<String, Object> deleteReply(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        boolean success = messageService.deleteReply(id, user.getId());
        if (success) {
            response.put("success", true);
            response.put("message", "回复删除成功");
        } else {
            response.put("success", false);
            response.put("message", "删除失败，无权限或回复不存在");
        }
        return response;
    }

    /**
     * 获取文明提示语
     * GET /api/message/tips
     */
    @GetMapping("/tips")
    public Map<String, Object> getTips() {
        Map<String, Object> response = new HashMap<>();
        
        String[] tips = {
            "💬 友善交流，互相鼓励",
            "🌸 留言区是大家的温馨角落",
            "📚 分享学习心得，一起进步",
            "🎯 专注当下，每天都是新开始",
            "💪 加油！你今天也很努力呢"
        };
        
        String[] rules = {
            "留言内容请文明友善",
            "禁止发布广告、联系方式",
            "禁止传播不良信息",
            "留言将在2周后自动清理"
        };
        
        response.put("success", true);
        response.put("tips", tips);
        response.put("rules", rules);
        return response;
    }
}
