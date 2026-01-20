package com.example.qr_code.controller;

import com.example.qr_code.entity.FocusRecord;
import com.example.qr_code.entity.LearningStats;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.FocusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/focus")
public class FocusController {

    @Autowired
    private FocusService focusService;

    /**
     * 保存专注记录
     * POST /api/focus/save
     * Body: { "duration": 1800, "startTime": "2026-01-05T10:00:00", "endTime": "2026-01-05T10:30:00" }
     */
    @PostMapping("/save")
    public Map<String, Object> saveFocusRecord(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前登录用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Integer duration = (Integer) body.get("duration");
            if (duration == null || duration <= 0) {
                result.put("success", false);
                result.put("message", "无效的专注时长");
                return result;
            }

            // 解析时间（前端可能不传，后端自动计算）
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusSeconds(duration);

            // 保存记录
            FocusRecord record = focusService.saveFocusRecord(user.getId(), duration, startTime, endTime);

            // 获取今日统计
            LearningStats todayStats = focusService.getTodayStats(user.getId());

            result.put("success", true);
            result.put("message", "专注记录已保存");
            result.put("recordId", record.getId());
            result.put("expEarned", calculateExp(duration));
            
            if (todayStats != null) {
                result.put("todayTotal", todayStats.getTotalDuration());
                result.put("todayCount", todayStats.getFocusCount());
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取今日学习统计
     * GET /api/focus/today
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayStats(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        LearningStats stats = focusService.getTodayStats(user.getId());
        
        result.put("success", true);
        if (stats != null) {
            result.put("totalDuration", stats.getTotalDuration());
            result.put("focusCount", stats.getFocusCount());
            result.put("avgDuration", stats.getAvgDuration());
            result.put("maxDuration", stats.getMaxDuration());
            result.put("expEarned", stats.getExpEarned());
        } else {
            result.put("totalDuration", 0);
            result.put("focusCount", 0);
            result.put("avgDuration", 0);
            result.put("maxDuration", 0);
            result.put("expEarned", 0);
        }

        return result;
    }

    private int calculateExp(int durationSeconds) {
        int minutes = durationSeconds / 60;
        if (minutes >= 30) {
            return (int) (minutes * 1.5);
        }
        return minutes;
    }
}
