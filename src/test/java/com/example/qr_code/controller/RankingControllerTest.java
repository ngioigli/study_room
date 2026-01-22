package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RankingController 单元测试
 * 测试排行榜相关功能
 */
public class RankingControllerTest {

    @InjectMocks
    private RankingController rankingController;

    @Mock
    private RankingService rankingService;

    @Mock
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试场景：成功获取今日排行榜（默认参数）
     * 前置条件：有用户学习数据
     * 输入数据：默认limit=20
     * 预期结果：返回排行榜列表，按今日学习时长降序排列
     */
    @Test
    public void testGetTodayRanking_DefaultLimit() {
        Map<String, Object> ranking1 = new HashMap<>();
        ranking1.put("userId", 1L);
        ranking1.put("username", "张三");
        ranking1.put("duration", 180);
        ranking1.put("rank", 1);

        Map<String, Object> ranking2 = new HashMap<>();
        ranking2.put("userId", 2L);
        ranking2.put("username", "李四");
        ranking2.put("duration", 120);
        ranking2.put("rank", 2);

        List<Map<String, Object>> mockRankings = Arrays.asList(ranking1, ranking2);

        when(rankingService.getTodayRanking(20)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getTodayRanking(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("today", response.get("type"));
        assertEquals("今日排行", response.get("title"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) response.get("ranking");
        assertNotNull(ranking);
        assertEquals(2, ranking.size());
        assertEquals("张三", ranking.get(0).get("username"));
        assertEquals(180, ranking.get(0).get("duration"));

        verify(rankingService, times(1)).getTodayRanking(20);
    }

    /**
     * 测试场景：成功获取今日排行榜（自定义limit）
     * 前置条件：有用户学习数据
     * 输入数据：limit=10
     * 预期结果：返回最多10条排行榜数据
     */
    @Test
    public void testGetTodayRanking_CustomLimit() {
        List<Map<String, Object>> mockRankings = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> ranking = new HashMap<>();
            ranking.put("userId", (long) i);
            ranking.put("username", "用户" + i);
            ranking.put("duration", 200 - i * 10);
            ranking.put("rank", i);
            mockRankings.add(ranking);
        }

        when(rankingService.getTodayRanking(10)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getTodayRanking(10);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) response.get("ranking");
        assertNotNull(ranking);
        assertEquals(10, ranking.size());

        verify(rankingService, times(1)).getTodayRanking(10);
    }

    /**
     * 测试场景：今日排行榜为空
     * 前置条件：没有用户学习数据
     * 输入数据：默认limit
     * 预期结果：返回空列表
     */
    @Test
    public void testGetTodayRanking_EmptyList() {
        when(rankingService.getTodayRanking(20)).thenReturn(new ArrayList<>());

        Map<String, Object> response = rankingController.getTodayRanking(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) response.get("ranking");
        assertNotNull(ranking);
        assertTrue(ranking.isEmpty());

        verify(rankingService, times(1)).getTodayRanking(20);
    }

    /**
     * 测试场景：成功获取本周排行榜（默认参数）
     * 前置条件：有用户学习数据
     * 输入数据：默认limit=20
     * 预期结果：返回排行榜列表，按本周学习时长降序排列
     */
    @Test
    public void testGetWeeklyRanking_Success() {
        Map<String, Object> ranking1 = new HashMap<>();
        ranking1.put("userId", 1L);
        ranking1.put("username", "张三");
        ranking1.put("duration", 600);
        ranking1.put("rank", 1);

        List<Map<String, Object>> mockRankings = Collections.singletonList(ranking1);

        when(rankingService.getWeeklyRanking(20)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getWeeklyRanking(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("weekly", response.get("type"));
        assertEquals("本周排行", response.get("title"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) response.get("ranking");
        assertNotNull(ranking);
        assertEquals(1, ranking.size());

        verify(rankingService, times(1)).getWeeklyRanking(20);
    }

    /**
     * 测试场景：成功获取本月排行榜
     * 前置条件：有用户学习数据
     * 输入数据：默认limit=20
     * 预期结果：返回排行榜列表，按本月学习时长降序排列
     */
    @Test
    public void testGetMonthlyRanking_Success() {
        Map<String, Object> ranking1 = new HashMap<>();
        ranking1.put("userId", 1L);
        ranking1.put("username", "张三");
        ranking1.put("duration", 2000);
        ranking1.put("rank", 1);

        List<Map<String, Object>> mockRankings = Collections.singletonList(ranking1);

        when(rankingService.getMonthlyRanking(20)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getMonthlyRanking(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("monthly", response.get("type"));
        assertEquals("本月排行", response.get("title"));

        verify(rankingService, times(1)).getMonthlyRanking(20);
    }

    /**
     * 测试场景：成功获取年度排行榜
     * 前置条件：有用户学习数据
     * 输入数据：默认limit=20
     * 预期结果：返回排行榜列表，按年度学习时长降序排列
     */
    @Test
    public void testGetYearlyRanking_Success() {
        Map<String, Object> ranking1 = new HashMap<>();
        ranking1.put("userId", 1L);
        ranking1.put("username", "张三");
        ranking1.put("duration", 10000);
        ranking1.put("rank", 1);

        List<Map<String, Object>> mockRankings = Collections.singletonList(ranking1);

        when(rankingService.getYearlyRanking(20)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getYearlyRanking(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("yearly", response.get("type"));
        assertEquals("年度排行", response.get("title"));

        verify(rankingService, times(1)).getYearlyRanking(20);
    }

    /**
     * 测试场景：成功获取总排行榜
     * 前置条件：有用户学习数据
     * 输入数据：默认limit=20
     * 预期结果：返回排行榜列表，按总学习时长降序排列
     */
    @Test
    public void testGetTotalRanking_Success() {
        Map<String, Object> ranking1 = new HashMap<>();
        ranking1.put("userId", 1L);
        ranking1.put("username", "张三");
        ranking1.put("duration", 50000);
        ranking1.put("rank", 1);

        List<Map<String, Object>> mockRankings = Collections.singletonList(ranking1);

        when(rankingService.getTotalRanking(20)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getTotalRanking(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("total", response.get("type"));
        assertEquals("总排行", response.get("title"));

        verify(rankingService, times(1)).getTotalRanking(20);
    }

    /**
     * 测试场景：成功获取用户各维度排名（已登录）
     * 前置条件：用户已登录
     * 输入数据：无
     * 预期结果：返回用户在各榜单的排名信息
     */
    @Test
    public void testGetMyRanking_Success() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("张三");

        when(session.getAttribute("user")).thenReturn(mockUser);

        Map<String, Object> mockRankings = new HashMap<>();
        mockRankings.put("todayRank", 5);
        mockRankings.put("weeklyRank", 3);
        mockRankings.put("monthlyRank", 8);
        mockRankings.put("yearlyRank", 12);
        mockRankings.put("totalRank", 10);

        when(rankingService.getUserRankings(1L)).thenReturn(mockRankings);

        Map<String, Object> response = rankingController.getMyRanking(session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> rankings = (Map<String, Object>) response.get("rankings");
        assertNotNull(rankings);
        assertEquals(5, rankings.get("todayRank"));
        assertEquals(3, rankings.get("weeklyRank"));
        assertEquals(8, rankings.get("monthlyRank"));

        verify(session, times(1)).getAttribute("user");
        verify(rankingService, times(1)).getUserRankings(1L);
    }

    /**
     * 测试场景：获取用户排名信息时未登录
     * 前置条件：用户未登录
     * 输入数据：无
     * 预期结果：返回错误信息提示需要登录
     */
    @Test
    public void testGetMyRanking_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = rankingController.getMyRanking(session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(rankingService, never()).getUserRankings(any());
    }
}
