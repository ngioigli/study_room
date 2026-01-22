package com.example.qr_code.controller;

import com.example.qr_code.entity.MessageBoard;
import com.example.qr_code.entity.MessageReply;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageController 单元测试
 * 测试留言板功能（仅文本留言，不包含图片上传）
 */
public class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
    }

    // 测试场景：获取留言列表 - 成功
    // 前置条件：存在多条留言记录
    // 输入数据：page=1, size=10
    // 预期结果：返回留言列表和分页信息
    @Test
    void testGetMessages_Success() {
        // 准备测试数据
        List<MessageBoard> mockMessages = new ArrayList<>();
        MessageBoard msg = new MessageBoard();
        msg.setId(1L);
        msg.setContent("测试留言内容");
        mockMessages.add(msg);

        when(messageService.getMessages(1, 10)).thenReturn(mockMessages);
        when(messageService.getMessageCount()).thenReturn(15);

        // 执行测试
        Map<String, Object> response = messageController.getMessages(1, 10);

        // 验证结果
        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("messages"));
        assertEquals(15, response.get("total"));
        assertEquals(2, response.get("totalPages")); // 15条记录，每页10条，共2页
        verify(messageService, times(1)).getMessages(1, 10);
    }

    // 测试场景：获取留言列表 - 使用默认参数
    // 前置条件：未指定分页参数
    // 输入数据：无参数（使用默认值）
    // 预期结果：使用默认的page=1, size=10
    @Test
    void testGetMessages_DefaultParameters() {
        when(messageService.getMessages(1, 10)).thenReturn(new ArrayList<>());
        when(messageService.getMessageCount()).thenReturn(0);

        Map<String, Object> response = messageController.getMessages(1, 10);

        assertTrue((Boolean) response.get("success"));
        assertEquals(1, response.get("page"));
        assertEquals(10, response.get("size"));
    }

    // 测试场景：发布留言 - 成功（纯文本）
    // 前置条件：用户已登录
    // 输入数据：有效的文本内容
    // 预期结果：留言发布成功
    @Test
    void testCreateMessage_Success() {
        session.setAttribute("user", testUser);
        when(messageService.createMessage(anyLong(), anyString(), anyList())).thenReturn(true);

        Map<String, Object> response = messageController.createMessage(
                "这是一条测试留言", null, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("留言发布成功", response.get("message"));
        verify(messageService, times(1)).createMessage(
                eq(1L), eq("这是一条测试留言"), anyList());
    }

    // 测试场景：发布留言 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：有效的文本内容
    // 预期结果：返回"请先登录"错误
    @Test
    void testCreateMessage_NotLoggedIn() {
        Map<String, Object> response = messageController.createMessage(
                "测试留言", null, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(messageService, never()).createMessage(anyLong(), anyString(), anyList());
    }

    // 测试场景：发布留言 - 内容为空
    // 前置条件：用户已登录
    // 输入数据：空字符串或null
    // 预期结果：返回"留言内容不能为空"错误
    @Test
    void testCreateMessage_EmptyContent() {
        session.setAttribute("user", testUser);

        Map<String, Object> response = messageController.createMessage(
                "", null, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("留言内容不能为空", response.get("message"));
    }

    // 测试场景：发布留言 - 内容超长
    // 前置条件：用户已登录
    // 输入数据：超过500字的文本
    // 预期结果：返回"留言内容不能超过500字"错误
    @Test
    void testCreateMessage_ContentTooLong() {
        session.setAttribute("user", testUser);
        String longContent = "a".repeat(501);

        Map<String, Object> response = messageController.createMessage(
                longContent, null, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("留言内容不能超过500字", response.get("message"));
    }

    // 测试场景：发布留言 - 包含敏感词
    // 前置条件：用户已登录
    // 输入数据：包含敏感词的内容
    // 预期结果：Service返回false，提示包含敏感词
    @Test
    void testCreateMessage_ContainsSensitiveWords() {
        session.setAttribute("user", testUser);
        when(messageService.createMessage(anyLong(), anyString(), anyList())).thenReturn(false);

        Map<String, Object> response = messageController.createMessage(
                "包含敏感词的留言", null, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("留言包含敏感词，请修改后重试", response.get("message"));
    }

    // 测试场景：编辑留言 - 成功
    // 前置条件：用户已登录且是留言作者
    // 输入数据：留言ID和新内容
    // 预期结果：留言修改成功
    @Test
    void testUpdateMessage_Success() {
        session.setAttribute("user", testUser);
        when(messageService.updateMessage(1L, 1L, "修改后的内容")).thenReturn(true);

        Map<String, String> data = new HashMap<>();
        data.put("content", "修改后的内容");

        Map<String, Object> response = messageController.updateMessage(1L, data, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("留言修改成功", response.get("message"));
    }

    // 测试场景：编辑留言 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：留言ID和新内容
    // 预期结果：返回"请先登录"错误
    @Test
    void testUpdateMessage_NotLoggedIn() {
        Map<String, String> data = new HashMap<>();
        data.put("content", "修改后的内容");

        Map<String, Object> response = messageController.updateMessage(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
    }

    // 测试场景：编辑留言 - 内容为空
    // 前置条件：用户已登录
    // 输入数据：空内容
    // 预期结果：返回"留言内容不能为空"错误
    @Test
    void testUpdateMessage_EmptyContent() {
        session.setAttribute("user", testUser);
        Map<String, String> data = new HashMap<>();
        data.put("content", "   ");

        Map<String, Object> response = messageController.updateMessage(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("留言内容不能为空", response.get("message"));
    }

    // 测试场景：编辑留言 - 无权限或包含敏感词
    // 前置条件：用户已登录
    // 输入数据：有效内容但Service返回false
    // 预期结果：返回"修改失败"提示
    @Test
    void testUpdateMessage_Failed() {
        session.setAttribute("user", testUser);
        when(messageService.updateMessage(anyLong(), anyLong(), anyString())).thenReturn(false);

        Map<String, String> data = new HashMap<>();
        data.put("content", "修改内容");

        Map<String, Object> response = messageController.updateMessage(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("修改失败，可能包含敏感词或无权限", response.get("message"));
    }

    // 测试场景：删除留言 - 成功
    // 前置条件：用户已登录且是留言作者
    // 输入数据：留言ID
    // 预期结果：留言删除成功
    @Test
    void testDeleteMessage_Success() {
        session.setAttribute("user", testUser);
        when(messageService.deleteMessage(1L, 1L)).thenReturn(true);

        Map<String, Object> response = messageController.deleteMessage(1L, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("留言删除成功", response.get("message"));
    }

    // 测试场景：删除留言 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：留言ID
    // 预期结果：返回"请先登录"错误
    @Test
    void testDeleteMessage_NotLoggedIn() {
        Map<String, Object> response = messageController.deleteMessage(1L, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
    }

    // 测试场景：删除留言 - 无权限或留言不存在
    // 前置条件：用户已登录但不是留言作者
    // 输入数据：留言ID
    // 预期结果：返回"删除失败"提示
    @Test
    void testDeleteMessage_Failed() {
        session.setAttribute("user", testUser);
        when(messageService.deleteMessage(anyLong(), anyLong())).thenReturn(false);

        Map<String, Object> response = messageController.deleteMessage(1L, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("删除失败，无权限或留言不存在", response.get("message"));
    }

    // 测试场景：获取留言的回复列表 - 成功
    // 前置条件：留言存在且有回复
    // 输入数据：留言ID
    // 预期结果：返回回复列表
    @Test
    void testGetReplies_Success() {
        List<MessageReply> mockReplies = new ArrayList<>();
        MessageReply reply = new MessageReply();
        reply.setId(1L);
        reply.setContent("测试回复");
        mockReplies.add(reply);

        when(messageService.getReplies(1L)).thenReturn(mockReplies);

        Map<String, Object> response = messageController.getReplies(1L);

        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("replies"));
        assertEquals(1, ((List<?>) response.get("replies")).size());
    }

    // 测试场景：发布回复 - 成功
    // 前置条件：用户已登录，留言存在
    // 输入数据：留言ID和回复内容
    // 预期结果：回复成功
    @Test
    void testCreateReply_Success() {
        session.setAttribute("user", testUser);
        when(messageService.createReply(1L, 1L, "这是一条回复")).thenReturn(true);

        Map<String, String> data = new HashMap<>();
        data.put("content", "这是一条回复");

        Map<String, Object> response = messageController.createReply(1L, data, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("回复成功", response.get("message"));
    }

    // 测试场景：发布回复 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：留言ID和回复内容
    // 预期结果：返回"请先登录"错误
    @Test
    void testCreateReply_NotLoggedIn() {
        Map<String, String> data = new HashMap<>();
        data.put("content", "回复内容");

        Map<String, Object> response = messageController.createReply(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
    }

    // 测试场景：发布回复 - 内容为空
    // 前置条件：用户已登录
    // 输入数据：空回复内容
    // 预期结果：返回"回复内容不能为空"错误
    @Test
    void testCreateReply_EmptyContent() {
        session.setAttribute("user", testUser);
        Map<String, String> data = new HashMap<>();
        data.put("content", "  ");

        Map<String, Object> response = messageController.createReply(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("回复内容不能为空", response.get("message"));
    }

    // 测试场景：发布回复 - 内容超长
    // 前置条件：用户已登录
    // 输入数据：超过200字的回复
    // 预期结果：返回"回复内容不能超过200字"错误
    @Test
    void testCreateReply_ContentTooLong() {
        session.setAttribute("user", testUser);
        Map<String, String> data = new HashMap<>();
        data.put("content", "a".repeat(201));

        Map<String, Object> response = messageController.createReply(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("回复内容不能超过200字", response.get("message"));
    }

    // 测试场景：发布回复 - 包含敏感词
    // 前置条件：用户已登录
    // 输入数据：包含敏感词的回复
    // 预期结果：返回"回复失败，可能包含敏感词"提示
    @Test
    void testCreateReply_ContainsSensitiveWords() {
        session.setAttribute("user", testUser);
        when(messageService.createReply(anyLong(), anyLong(), anyString())).thenReturn(false);

        Map<String, String> data = new HashMap<>();
        data.put("content", "包含敏感词的回复");

        Map<String, Object> response = messageController.createReply(1L, data, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("回复失败，可能包含敏感词", response.get("message"));
    }

    // 测试场景：删除回复 - 成功
    // 前置条件：用户已登录且是回复作者
    // 输入数据：回复ID
    // 预期结果：回复删除成功
    @Test
    void testDeleteReply_Success() {
        session.setAttribute("user", testUser);
        when(messageService.deleteReply(1L, 1L)).thenReturn(true);

        Map<String, Object> response = messageController.deleteReply(1L, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("回复删除成功", response.get("message"));
    }

    // 测试场景：删除回复 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：回复ID
    // 预期结果：返回"请先登录"错误
    @Test
    void testDeleteReply_NotLoggedIn() {
        Map<String, Object> response = messageController.deleteReply(1L, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
    }

    // 测试场景：删除回复 - 无权限或回复不存在
    // 前置条件：用户已登录但不是回复作者
    // 输入数据：回复ID
    // 预期结果：返回"删除失败"提示
    @Test
    void testDeleteReply_Failed() {
        session.setAttribute("user", testUser);
        when(messageService.deleteReply(anyLong(), anyLong())).thenReturn(false);

        Map<String, Object> response = messageController.deleteReply(1L, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("删除失败，无权限或回复不存在", response.get("message"));
    }

    // 测试场景：获取文明提示语 - 成功
    // 前置条件：无
    // 输入数据：无
    // 预期结果：返回提示语和规则列表
    @Test
    void testGetTips_Success() {
        Map<String, Object> response = messageController.getTips();

        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("tips"));
        assertNotNull(response.get("rules"));
        assertTrue(((String[]) response.get("tips")).length > 0);
        assertTrue(((String[]) response.get("rules")).length > 0);
    }
}
