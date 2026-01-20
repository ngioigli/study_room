package com.example.qr_code.controller;

import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    /**
     * 获取所有座位列表
     * GET /api/seats
     */
    @GetMapping
    public Map<String, Object> getAllSeats() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Seat> seats = seatService.getAllSeats();
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
     * 获取可用座位列表
     * GET /api/seats/available
     */
    @GetMapping("/available")
    public Map<String, Object> getAvailableSeats() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Seat> seats = seatService.getAvailableSeats();
            result.put("success", true);
            result.put("seats", seats);
            result.put("count", seats.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取可用座位失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取座位详情
     * GET /api/seats/{id}
     */
    @GetMapping("/{id}")
    public Map<String, Object> getSeatById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Seat seat = seatService.getSeatById(id);
            if (seat != null) {
                result.put("success", true);
                result.put("seat", seat);
            } else {
                result.put("success", false);
                result.put("message", "座位不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取座位详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据座位编号获取座位
     * GET /api/seats/number/{seatNumber}
     */
    @GetMapping("/number/{seatNumber}")
    public Map<String, Object> getSeatByNumber(@PathVariable String seatNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            Seat seat = seatService.getSeatByNumber(seatNumber);
            if (seat != null) {
                result.put("success", true);
                result.put("seat", seat);
            } else {
                result.put("success", false);
                result.put("message", "座位不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取座位详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 控制座位电源
     * POST /api/seats/{id}/power
     * Body: { "powerOn": true }
     */
    @PostMapping("/{id}/power")
    public Map<String, Object> controlPower(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 验证登录（管理员权限可以在这里添加）
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

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
            result.put("message", "电源控制失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新座位状态
     * PUT /api/seats/{id}/status
     * Body: { "status": "available" | "occupied" | "reserved" | "maintenance" }
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateSeatStatus(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            String status = (String) body.get("status");
            if (status == null || status.isEmpty()) {
                result.put("success", false);
                result.put("message", "请指定座位状态");
                return result;
            }

            // 验证状态值
            if (!status.matches("available|occupied|reserved|maintenance")) {
                result.put("success", false);
                result.put("message", "无效的座位状态");
                return result;
            }

            Seat seat = seatService.updateSeatStatus(id, status);
            result.put("success", true);
            result.put("seat", seat);
            result.put("message", "座位状态已更新");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新座位状态失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取座位统计信息
     * GET /api/seats/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getSeatStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            SeatService.SeatStats stats = seatService.getSeatStats();
            result.put("success", true);
            result.put("total", stats.total);
            result.put("available", stats.available);
            result.put("occupied", stats.occupied);
            result.put("reserved", stats.reserved);
            result.put("maintenance", stats.maintenance);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        return result;
    }
}
