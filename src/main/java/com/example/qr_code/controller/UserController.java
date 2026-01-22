package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * UserController 用户个人中心控制器
 * 处理用户资料编辑、头像上传等功能
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户详细信息（含新增字段）
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User sessionUser = (User) session.getAttribute("user");
        
        if (sessionUser == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        // 从数据库获取最新用户信息
        User user = userService.getUserById(sessionUser.getId());
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        // 获取学习统计
        Map<String, Object> stats = userService.getUserStats(user.getId());
        
        // 构建返回数据
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("signature", user.getSignature());
        profile.put("todayStatus", user.getTodayStatus());
        profile.put("studyDays", user.getStudyDays());
        profile.put("role", user.getRole());
        profile.put("createdAt", user.getCreatedAt());
        
        response.put("success", true);
        response.put("profile", profile);
        response.put("stats", stats);
        return response;
    }

    /**
     * 更新用户昵称
     * PUT /api/user/nickname
     */
    @PutMapping("/nickname")
    public Map<String, Object> updateNickname(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String nickname = data.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "昵称不能为空");
            return response;
        }
        
        if (nickname.length() > 20) {
            response.put("success", false);
            response.put("message", "昵称不能超过20个字符");
            return response;
        }
        
        boolean success = userService.updateNickname(user.getId(), nickname.trim());
        if (success) {
            user.setNickname(nickname.trim());
            session.setAttribute("user", user);
            response.put("success", true);
            response.put("message", "昵称修改成功");
        } else {
            response.put("success", false);
            response.put("message", "修改失败，请重试");
        }
        return response;
    }

    /**
     * 更新用户头像
     * PUT /api/user/avatar
     */
    @PutMapping("/avatar")
    public Map<String, Object> updateAvatar(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String avatar = data.get("avatar");
        if (avatar == null || avatar.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "头像不能为空");
            return response;
        }
        
        boolean success = userService.updateAvatar(user.getId(), avatar.trim());
        if (success) {
            user.setAvatar(avatar.trim());
            session.setAttribute("user", user);
            response.put("success", true);
            response.put("message", "头像修改成功");
        } else {
            response.put("success", false);
            response.put("message", "修改失败，请重试");
        }
        return response;
    }

    /**
     * 更新个性签名
     * PUT /api/user/signature
     */
    @PutMapping("/signature")
    public Map<String, Object> updateSignature(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String signature = data.get("signature");
        if (signature != null && signature.length() > 100) {
            response.put("success", false);
            response.put("message", "签名不能超过100个字符");
            return response;
        }
        
        boolean success = userService.updateSignature(user.getId(), signature == null ? "" : signature.trim());
        if (success) {
            user.setSignature(signature);
            session.setAttribute("user", user);
            response.put("success", true);
            response.put("message", "签名修改成功");
        } else {
            response.put("success", false);
            response.put("message", "修改失败，请重试");
        }
        return response;
    }

    /**
     * 更新今日状态
     * PUT /api/user/status
     */
    @PutMapping("/status")
    public Map<String, Object> updateTodayStatus(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String todayStatus = data.get("todayStatus");
        if (todayStatus == null || todayStatus.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "状态不能为空");
            return response;
        }
        
        if (todayStatus.length() > 30) {
            response.put("success", false);
            response.put("message", "状态不能超过30个字符");
            return response;
        }
        
        boolean success = userService.updateTodayStatus(user.getId(), todayStatus.trim());
        if (success) {
            user.setTodayStatus(todayStatus.trim());
            session.setAttribute("user", user);
            response.put("success", true);
            response.put("message", "状态修改成功");
        } else {
            response.put("success", false);
            response.put("message", "修改失败，请重试");
        }
        return response;
    }

    /**
     * 批量更新用户资料
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        // 更新各个字段
        String nickname = data.get("nickname");
        String avatar = data.get("avatar");
        String signature = data.get("signature");
        String todayStatus = data.get("todayStatus");
        
        boolean success = userService.updateProfile(user.getId(), nickname, avatar, signature, todayStatus);
        
        if (success) {
            // 更新 session 中的用户信息
            if (nickname != null) user.setNickname(nickname);
            if (avatar != null) user.setAvatar(avatar);
            if (signature != null) user.setSignature(signature);
            if (todayStatus != null) user.setTodayStatus(todayStatus);
            session.setAttribute("user", user);
            
            response.put("success", true);
            response.put("message", "资料更新成功");
        } else {
            response.put("success", false);
            response.put("message", "更新失败，请重试");
        }
        return response;
    }

    /**
     * 获取可选头像列表
     * GET /api/user/avatars
     */
    @GetMapping("/avatars")
    public Map<String, Object> getAvatarList() {
        Map<String, Object> response = new HashMap<>();
        
        // 预设头像列表（emoji 形式）
        String[] avatars = {
            "👤", "😊", "😎", "🤓", "🧑‍💻", "👨‍🎓", "👩‍🎓", "🦊", "🐱", "🐶",
            "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧",
            "🦉", "🦋", "🌸", "🌺", "🌻", "🌈", "⭐", "🌙", "☀️", "🔥"
        };
        
        response.put("success", true);
        response.put("avatars", avatars);
        return response;
    }

    /**
     * 获取可选状态列表
     * GET /api/user/statuses
     */
    @GetMapping("/statuses")
    public Map<String, Object> getStatusList() {
        Map<String, Object> response = new HashMap<>();
        
        // 预设状态列表
        String[] statuses = {
            "努力学习中 📚",
            "认真复习中 ✍️",
            "准备考试中 💪",
            "写论文中 📝",
            "看书充电中 🔋",
            "专注模式 🎯",
            "今天也要加油 ⛽",
            "冲冲冲 🚀",
            "悠闲自习中 ☕",
            "休息一下 😴"
        };
        
        response.put("success", true);
        response.put("statuses", statuses);
        return response;
    }
    
    /**
     * 获取用户隐私设置
     * GET /api/user/privacy
     */
    @GetMapping("/privacy")
    public Map<String, Object> getPrivacySettings(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User sessionUser = (User) session.getAttribute("user");
        
        if (sessionUser == null) {
            response.put("success", false);
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        
        // 从数据库获取最新用户信息
        User user = userService.getUserById(sessionUser.getId());
        if (user == null) {
            response.put("success", false);
            response.put("code", 404);
            response.put("message", "用户不存在");
            return response;
        }
        
        response.put("success", true);
        response.put("code", 0);
        response.put("hideRanking", user.getHideRanking() != null && user.getHideRanking() == 1);
        return response;
    }
    
    /**
     * 更新排行榜隐私设置
     * PUT /api/user/privacy/ranking
     */
    @PutMapping("/privacy/ranking")
    public Map<String, Object> updateRankingPrivacy(@RequestBody Map<String, Object> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        
        Object hideRankingObj = data.get("hideRanking");
        boolean hideRanking = false;
        
        if (hideRankingObj instanceof Boolean) {
            hideRanking = (Boolean) hideRankingObj;
        } else if (hideRankingObj instanceof Number) {
            hideRanking = ((Number) hideRankingObj).intValue() == 1;
        }
        
        boolean success = userService.updateHideRanking(user.getId(), hideRanking);
        
        if (success) {
            user.setHideRanking(hideRanking ? 1 : 0);
            session.setAttribute("user", user);
            
            response.put("success", true);
            response.put("code", 0);
            response.put("message", hideRanking ? "已隐藏您的排行榜排名" : "您的排名已公开显示");
            response.put("hideRanking", hideRanking);
        } else {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "设置失败，请重试");
        }
        return response;
    }
}
