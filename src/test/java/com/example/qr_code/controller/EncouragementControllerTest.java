package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.EncouragementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EncouragementController 单元测试
 * 测试鼓励卡片功能
 */
public class EncouragementControllerTest {

    @InjectMocks
    private EncouragementController encouragementController;

    @Mock
    private EncouragementService encouragementService;

    @Mock
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试场景：成功获取随机鼓励卡片列表（默认limit）
     * 前置条件：系统中存在鼓励卡片
     * 输入数据：默认limit=20
     * 预期结果：返回20张随机卡片
     */
    @Test
    public void testGetCards_DefaultLimit() {
        List<Map<String, Object>> mockCards = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> card = new HashMap<>();
            card.put("id", (long) i);
            card.put("emoji", "💪");
            card.put("message", "加油！");
            card.put("likes", 10);
            mockCards.add(card);
        }

        when(encouragementService.getRandomCards(20)).thenReturn(mockCards);

        Map<String, Object> response = encouragementController.getCards(20);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cards = (List<Map<String, Object>>) response.get("cards");
        assertNotNull(cards);
        assertEquals(20, cards.size());

        verify(encouragementService, times(1)).getRandomCards(20);
    }

    /**
     * 测试场景：成功获取随机鼓励卡片列表（自定义limit）
     * 前置条件：系统中存在鼓励卡片
     * 输入数据：limit=10
     * 预期结果：返回10张随机卡片
     */
    @Test
    public void testGetCards_CustomLimit() {
        List<Map<String, Object>> mockCards = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> card = new HashMap<>();
            card.put("id", (long) i);
            mockCards.add(card);
        }

        when(encouragementService.getRandomCards(10)).thenReturn(mockCards);

        Map<String, Object> response = encouragementController.getCards(10);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cards = (List<Map<String, Object>>) response.get("cards");
        assertEquals(10, cards.size());

        verify(encouragementService, times(1)).getRandomCards(10);
    }

    /**
     * 测试场景：获取卡片时limit超过最大值
     * 前置条件：无
     * 输入数据：limit=100（超过最大值50）
     * 预期结果：自动限制为50张卡片
     */
    @Test
    public void testGetCards_LimitExceedsMax() {
        List<Map<String, Object>> mockCards = new ArrayList<>();
        
        when(encouragementService.getRandomCards(50)).thenReturn(mockCards);

        Map<String, Object> response = encouragementController.getCards(100);

        assertNotNull(response);
        verify(encouragementService, times(1)).getRandomCards(50);
    }

    /**
     * 测试场景：获取卡片列表时服务层异常
     * 前置条件：服务层抛出异常
     * 输入数据：默认limit
     * 预期结果：返回错误信息
     */
    @Test
    public void testGetCards_ServiceException() {
        when(encouragementService.getRandomCards(anyInt())).thenThrow(new RuntimeException("数据库连接失败"));

        Map<String, Object> response = encouragementController.getCards(20);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertTrue(response.get("message").toString().contains("获取失败"));

        verify(encouragementService, times(1)).getRandomCards(20);
    }

    /**
     * 测试场景：成功创建鼓励卡片（已登录用户）
     * 前置条件：用户已登录
     * 输入数据：emoji和message
     * 预期结果：成功创建卡片
     */
    @Test
    public void testCreateCard_Success() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("张三");

        when(session.getAttribute("user")).thenReturn(mockUser);

        Map<String, String> params = new HashMap<>();
        params.put("emoji", "🎉");
        params.put("message", "今天也要加油哦！");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("id", 123L);

        when(encouragementService.createCard(1L, "🎉", "今天也要加油哦！")).thenReturn(mockResult);

        Map<String, Object> response = encouragementController.createCard(params, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals(123L, response.get("id"));

        verify(session, times(1)).getAttribute("user");
        verify(encouragementService, times(1)).createCard(1L, "🎉", "今天也要加油哦！");
    }

    /**
     * 测试场景：创建鼓励卡片（未登录用户）
     * 前置条件：用户未登录
     * 输入数据：emoji和message
     * 预期结果：以匿名用户身份创建（userId为null）
     */
    @Test
    public void testCreateCard_AnonymousUser() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, String> params = new HashMap<>();
        params.put("emoji", "🌟");
        params.put("message", "匿名留言");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);

        when(encouragementService.createCard(null, "🌟", "匿名留言")).thenReturn(mockResult);

        Map<String, Object> response = encouragementController.createCard(params, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));

        verify(encouragementService, times(1)).createCard(null, "🌟", "匿名留言");
    }

    /**
     * 测试场景：成功点赞卡片
     * 前置条件：卡片存在
     * 输入数据：卡片ID
     * 预期结果：点赞成功
     */
    @Test
    public void testLikeCard_Success() {
        when(encouragementService.likeCard(123L)).thenReturn(true);

        Map<String, Object> response = encouragementController.likeCard(123L);

        assertNotNull(response);
        assertEquals(true, response.get("success"));

        verify(encouragementService, times(1)).likeCard(123L);
    }

    /**
     * 测试场景：点赞不存在的卡片
     * 前置条件：卡片不存在
     * 输入数据：不存在的卡片ID
     * 预期结果：点赞失败
     */
    @Test
    public void testLikeCard_CardNotFound() {
        when(encouragementService.likeCard(999L)).thenReturn(false);

        Map<String, Object> response = encouragementController.likeCard(999L);

        assertNotNull(response);
        assertEquals(false, response.get("success"));

        verify(encouragementService, times(1)).likeCard(999L);
    }

    /**
     * 测试场景：管理员成功隐藏卡片
     * 前置条件：用户是管理员
     * 输入数据：卡片ID
     * 预期结果：隐藏成功
     */
    @Test
    public void testHideCard_AdminSuccess() {
        User mockAdmin = new User();
        mockAdmin.setId(1L);
        mockAdmin.setRole("admin");

        when(session.getAttribute("user")).thenReturn(mockAdmin);
        when(encouragementService.hideCard(123L)).thenReturn(true);

        Map<String, Object> response = encouragementController.hideCard(123L, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));

        verify(session, times(1)).getAttribute("user");
        verify(encouragementService, times(1)).hideCard(123L);
    }

    /**
     * 测试场景：非管理员用户尝试隐藏卡片
     * 前置条件：用户不是管理员
     * 输入数据：卡片ID
     * 预期结果：返回权限不足错误
     */
    @Test
    public void testHideCard_NonAdminForbidden() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setRole("user");

        when(session.getAttribute("user")).thenReturn(mockUser);

        Map<String, Object> response = encouragementController.hideCard(123L, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("权限不足", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(encouragementService, never()).hideCard(anyLong());
    }

    /**
     * 测试场景：未登录用户尝试隐藏卡片
     * 前置条件：用户未登录
     * 输入数据：卡片ID
     * 预期结果：返回权限不足错误
     */
    @Test
    public void testHideCard_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = encouragementController.hideCard(123L, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("权限不足", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(encouragementService, never()).hideCard(anyLong());
    }
}
