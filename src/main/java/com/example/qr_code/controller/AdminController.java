package com.example.qr_code.controller;

import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.AdminService;
import com.example.qr_code.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SeatService seatService;

    /**
     * 验证管理员权限
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "admin".equals(user.getRole());
    }

    private Map<String, Object> unauthorized() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "需要管理员权限");
        return result;
    }

    // ==================== 用户管理 ====================

    /**
     * 获取所有用户列表（含统计数据）
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers(
            @RequestParam(defaultValue = "false") boolean withStats,
            HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            if (withStats) {
                List<Map<String, Object>> users = adminService.getAllUsersWithStats();
                result.put("success", true);
                result.put("users", users);
            } else {
                List<User> users = adminService.getAllUsers();
                Map<String, Integer> stats = adminService.getUserStats();
                result.put("success", true);
                result.put("users", users);
                result.put("stats", stats);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取单个用户详细统计
     * GET /api/admin/users/{id}/detail
     */
    @GetMapping("/users/{id}/detail")
    public Map<String, Object> getUserDetail(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userDetail = adminService.getUserDetailStats(id);
            result.put("success", true);
            result.put("user", userDetail);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 更新用户状态
     * PUT /api/admin/users/{id}/status
     * Body: { "status": 1 }
     */
    @PutMapping("/users/{id}/status")
    public Map<String, Object> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            Integer status = (Integer) body.get("status");
            if (status == null) {
                result.put("success", false);
                result.put("message", "请指定状态");
                return result;
            }

            User user = adminService.updateUserStatus(id, status);
            result.put("success", true);
            result.put("user", user);
            result.put("message", status == 1 ? "用户已启用" : "用户已禁用");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 更新用户角色
     * PUT /api/admin/users/{id}/role
     * Body: { "role": "admin" }
     */
    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            String role = (String) body.get("role");
            if (role == null || role.isEmpty()) {
                result.put("success", false);
                result.put("message", "请指定角色");
                return result;
            }

            User user = adminService.updateUserRole(id, role);
            result.put("success", true);
            result.put("user", user);
            result.put("message", "角色已更新");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ==================== 座位管理 ====================

    /**
     * 获取所有座位（含当前使用者）
     * GET /api/admin/seats
     */
    @GetMapping("/seats")
    public Map<String, Object> getAllSeats(HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> seats = adminService.getAllSeatsWithUsers();
            SeatService.SeatStats stats = seatService.getSeatStats();

            result.put("success", true);
            result.put("seats", seats);
            result.put("stats", stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取座位列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 强制释放座位
     * POST /api/admin/seats/{id}/release
     */
    @PostMapping("/seats/{id}/release")
    public Map<String, Object> forceReleaseSeat(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            adminService.forceReleaseSeat(id);
            result.put("success", true);
            result.put("message", "座位已强制释放");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 设置座位维护状态
     * PUT /api/admin/seats/{id}/maintenance
     * Body: { "maintenance": true }
     */
    @PutMapping("/seats/{id}/maintenance")
    public Map<String, Object> setSeatMaintenance(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            Boolean maintenance = (Boolean) body.get("maintenance");
            if (maintenance == null) {
                result.put("success", false);
                result.put("message", "请指定维护状态");
                return result;
            }

            Seat seat = adminService.setSeatMaintenance(id, maintenance);
            result.put("success", true);
            result.put("seat", seat);
            result.put("message", maintenance ? "座位已设为维护中" : "座位已恢复正常");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 控制座位电源
     * POST /api/admin/seats/{id}/power
     * Body: { "powerOn": true }
     */
    @PostMapping("/seats/{id}/power")
    public Map<String, Object> controlPower(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            Boolean powerOn = (Boolean) body.get("powerOn");
            if (powerOn == null) {
                result.put("success", false);
                result.put("message", "请指定电源状态");
                return result;
            }

            Seat seat = seatService.controlPower(id, powerOn);
            result.put("success", true);
            result.put("seat", seat);
            result.put("message", powerOn ? "座位已通电" : "座位已断电");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ==================== 数据统计 ====================

    /**
     * 获取今日概览
     * GET /api/admin/stats/overview
     */
    @GetMapping("/stats/overview")
    public Map<String, Object> getTodayOverview(HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> overview = adminService.getTodayOverview();
            result.put("success", true);
            result.putAll(overview);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取概览数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取最近7天统计
     * GET /api/admin/stats/weekly
     */
    @GetMapping("/stats/weekly")
    public Map<String, Object> getWeeklyStats(HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> stats = adminService.getWeeklyStats();
            result.put("success", true);
            result.put("stats", stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取周统计失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户排行榜
     * GET /api/admin/stats/ranking?limit=10
     */
    @GetMapping("/stats/ranking")
    public Map<String, Object> getUserRanking(@RequestParam(defaultValue = "10") int limit, HttpSession session) {
        if (!isAdmin(session)) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> ranking = adminService.getUserRanking(limit);
            result.put("success", true);
            result.put("ranking", ranking);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取排行榜失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 检查当前用户是否是管理员
     * GET /api/admin/check
     */
    @GetMapping("/check")
    public Map<String, Object> checkAdmin(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            result.put("success", false);
            result.put("isAdmin", false);
            result.put("message", "请先登录");
        } else {
            result.put("success", true);
            result.put("isAdmin", "admin".equals(user.getRole()));
            result.put("username", user.getUsername());
        }
        return result;
    }
}
