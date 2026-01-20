package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 获取周统计数据
     * GET /api/stats/weekly
     */
    @GetMapping("/weekly")
    public Map<String, Object> getWeeklyStats(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Map<String, Object> stats = statsService.getWeeklyStats(user.getId());
            result.put("success", true);
            result.putAll(stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取月统计数据
     * GET /api/stats/monthly
     */
    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyStats(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Map<String, Object> stats = statsService.getMonthlyStats(user.getId());
            result.put("success", true);
            result.putAll(stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取学习时段分布
     * GET /api/stats/hourly
     */
    @GetMapping("/hourly")
    public Map<String, Object> getHourlyDistribution(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Map<String, Object> stats = statsService.getHourlyDistribution(user.getId());
            result.put("success", true);
            result.putAll(stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取学习画像
     * GET /api/stats/profile
     */
    @GetMapping("/profile")
    public Map<String, Object> getLearningProfile(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Map<String, Object> profile = statsService.getLearningProfile(user.getId());
            result.put("success", true);
            result.putAll(profile);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取画像失败: " + e.getMessage());
        }

        return result;
    }
}
