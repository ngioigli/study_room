package com.example.qr_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.User;
import com.example.qr_code.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Map<String, Object> response = new HashMap<>();

        // --- MyBatis-Plus 查询逻辑 开始 ---
        // 构造查询条件：select * from users where username = ?
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        
        // 执行查询
        User user = userMapper.selectOne(queryWrapper);
        // --- MyBatis-Plus 查询逻辑 结束 ---

        // 校验逻辑
        if (user != null && user.getPassword().equals(password)) {
            // 检查用户是否被禁用
            if (user.getStatus() != null && user.getStatus() == 0) {
                response.put("success", false);
                response.put("message", "您的账号已被禁用，请联系管理员");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // 登录成功，写入Session
            session.setAttribute("user", user);
            
            response.put("success", true);
            response.put("message", "欢迎回来，" + user.getNickname());
            
            // 根据角色判断跳转页面
            if ("admin".equals(user.getRole())) {
                // 管理员跳转到管理后台
                response.put("redirectUrl", "/admin.html");
            } else {
                // 普通用户跳转到自习室首页
                response.put("redirectUrl", "/index.html");
            }
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "账号或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // 在 LoginController 类中添加以下方法

    @GetMapping("/api/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        // 销毁 Session，清除用户数据
        session.invalidate();
        return ResponseEntity.ok("已退出");
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Map<String, Object>> logoutPost(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已退出登录");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/info
     */
    @GetMapping("/api/user/info")
    public Map<String, Object> getUserInfo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        // 返回用户信息（排除敏感字段）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        userInfo.put("createdAt", user.getCreatedAt());
        
        response.put("success", true);
        response.put("user", userInfo);
        return response;
    }
}