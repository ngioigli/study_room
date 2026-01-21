package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RankingController 排行榜控制器
 * 处理各维度学习时长排行
 */
@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /**
     * 获取今日排行榜
     * GET /api/ranking/today
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayRanking(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> ranking = rankingService.getTodayRanking(limit);
        
        response.put("success", true);
        response.put("ranking", ranking);
        response.put("type", "today");
        response.put("title", "今日排行");
        return response;
    }

    /**
     * 获取本周排行榜
     * GET /api/ranking/weekly
     */
    @GetMapping("/weekly")
    public Map<String, Object> getWeeklyRanking(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> ranking = rankingService.getWeeklyRanking(limit);
        
        response.put("success", true);
        response.put("ranking", ranking);
        response.put("type", "weekly");
        response.put("title", "本周排行");
        return response;
    }

    /**
     * 获取本月排行榜
     * GET /api/ranking/monthly
     */
    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyRanking(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> ranking = rankingService.getMonthlyRanking(limit);
        
        response.put("success", true);
        response.put("ranking", ranking);
        response.put("type", "monthly");
        response.put("title", "本月排行");
        return response;
    }

    /**
     * 获取本年排行榜
     * GET /api/ranking/yearly
     */
    @GetMapping("/yearly")
    public Map<String, Object> getYearlyRanking(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> ranking = rankingService.getYearlyRanking(limit);
        
        response.put("success", true);
        response.put("ranking", ranking);
        response.put("type", "yearly");
        response.put("title", "年度排行");
        return response;
    }

    /**
     * 获取总排行榜
     * GET /api/ranking/total
     */
    @GetMapping("/total")
    public Map<String, Object> getTotalRanking(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> ranking = rankingService.getTotalRanking(limit);
        
        response.put("success", true);
        response.put("ranking", ranking);
        response.put("type", "total");
        response.put("title", "总排行");
        return response;
    }

    /**
     * 获取当前用户的各维度排名
     * GET /api/ranking/me
     */
    @GetMapping("/me")
    public Map<String, Object> getMyRanking(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        Map<String, Object> rankings = rankingService.getUserRankings(user.getId());
        
        response.put("success", true);
        response.put("rankings", rankings);
        return response;
    }
}
