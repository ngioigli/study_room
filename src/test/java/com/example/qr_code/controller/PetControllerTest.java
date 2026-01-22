package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.entity.UserPet;
import com.example.qr_code.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PetController 单元测试类
 * 测试宠物管理相关功能：
 * 1. 获取宠物信息
 * 2. 创建宠物
 * 3. 宠物互动
 * 4. 获取鼓励语
 * 5. 一键孵化
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PetController 单元测试")
class PetControllerTest {

    @InjectMocks
    private PetController petController;

    @Mock
    private PetService petService;

    private MockHttpSession session;
    private User testUser;
    private UserPet testPet;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        
        // 创建测试宠物
        testPet = new UserPet();
        testPet.setId(1L);
        testPet.setUserId(1L);
        testPet.setPetName("小咪");
        testPet.setPetType("cat");
        testPet.setExp(100);
        testPet.setLevel(2);
        testPet.setStage("baby");
        testPet.setMood(80);
        testPet.setCreatedAt(LocalDateTime.now());
    }

    /**
     * 测试获取宠物信息 - 成功（有宠物）
     * 前置条件：用户已登录且有宠物
     * 输入数据：有效的用户会话
     * 预期结果：返回宠物信息
     */
    @Test
    @DisplayName("获取宠物信息 - 成功（有宠物）")
    void testGetPet_Success_HasPet() {
        // Arrange
        session.setAttribute("user", testUser);
        when(petService.getPet(testUser.getId())).thenReturn(testPet);
        when(petService.getStageName(testPet.getStage())).thenReturn("幼体期");
        when(petService.getExpToNextLevel(testPet.getExp())).thenReturn(50);

        // Act
        Map<String, Object> response = petController.getPet(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertTrue((Boolean) response.get("hasPet"));
        assertNotNull(response.get("pet"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pet = (Map<String, Object>) response.get("pet");
        assertEquals(testPet.getId(), pet.get("id"));
        assertEquals(testPet.getPetName(), pet.get("name"));
        assertEquals(testPet.getPetType(), pet.get("type"));
        assertEquals(testPet.getExp(), pet.get("exp"));
        assertEquals(testPet.getLevel(), pet.get("level"));
        assertEquals(testPet.getMood(), pet.get("mood"));
        
        verify(petService, times(1)).getPet(testUser.getId());
        verify(petService, times(1)).getStageName(testPet.getStage());
        verify(petService, times(1)).getExpToNextLevel(testPet.getExp());
    }

    /**
     * 测试获取宠物信息 - 成功（无宠物）
     * 前置条件：用户已登录但没有宠物
     * 输入数据：有效的用户会话
     * 预期结果：返回无宠物提示
     */
    @Test
    @DisplayName("获取宠物信息 - 成功（无宠物）")
    void testGetPet_Success_NoPet() {
        // Arrange
        session.setAttribute("user", testUser);
        when(petService.getPet(testUser.getId())).thenReturn(null);

        // Act
        Map<String, Object> response = petController.getPet(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertFalse((Boolean) response.get("hasPet"));
        assertEquals("还没有宠物，快去孵化一只吧！", response.get("message"));
        assertNull(response.get("pet"));
        
        verify(petService, times(1)).getPet(testUser.getId());
    }

    /**
     * 测试获取宠物信息 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("获取宠物信息 - 用户未登录")
    void testGetPet_UserNotLoggedIn() {
        // Arrange - 不设置用户会话

        // Act
        Map<String, Object> response = petController.getPet(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(petService, never()).getPet(any());
    }

    /**
     * 测试创建宠物 - 成功
     * 前置条件：用户已登录且没有宠物
     * 输入数据：有效的宠物类型和名称
     * 预期结果：成功创建宠物
     */
    @Test
    @DisplayName("创建宠物 - 成功")
    void testCreatePet_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, String> body = new HashMap<>();
        body.put("petType", "cat");
        body.put("petName", "小咪");
        
        when(petService.createPet(testUser.getId(), "cat", "小咪")).thenReturn(testPet);
        when(petService.getStageName(testPet.getStage())).thenReturn("幼体期");
        when(petService.getExpToNextLevel(testPet.getExp())).thenReturn(50);

        // Act
        Map<String, Object> response = petController.createPet(body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("宠物孵化成功！", response.get("message"));
        assertNotNull(response.get("pet"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pet = (Map<String, Object>) response.get("pet");
        assertEquals(testPet.getPetName(), pet.get("name"));
        assertEquals(testPet.getPetType(), pet.get("type"));
        
        verify(petService, times(1)).createPet(testUser.getId(), "cat", "小咪");
    }

    /**
     * 测试创建宠物 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：有效的宠物信息
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("创建宠物 - 用户未登录")
    void testCreatePet_UserNotLoggedIn() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("petType", "cat");
        body.put("petName", "小咪");

        // Act
        Map<String, Object> response = petController.createPet(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(petService, never()).createPet(any(), any(), any());
    }

    /**
     * 测试创建宠物 - 服务异常
     * 前置条件：用户已登录
     * 输入数据：有效的宠物信息
     * 预期结果：返回创建失败提示
     */
    @Test
    @DisplayName("创建宠物 - 服务异常")
    void testCreatePet_ServiceException() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, String> body = new HashMap<>();
        body.put("petType", "cat");
        body.put("petName", "小咪");
        
        when(petService.createPet(testUser.getId(), "cat", "小咪"))
                .thenThrow(new RuntimeException("宠物已存在"));

        // Act
        Map<String, Object> response = petController.createPet(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("创建失败: 宠物已存在", response.get("message"));
        
        verify(petService, times(1)).createPet(testUser.getId(), "cat", "小咪");
    }

    /**
     * 测试宠物互动 - 成功（有请求体）
     * 前置条件：用户已登录且有宠物
     * 输入数据：有效的互动类型
     * 预期结果：返回互动结果
     */
    @Test
    @DisplayName("宠物互动 - 成功（有请求体）")
    void testInteract_Success_WithBody() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, String> body = new HashMap<>();
        body.put("interactType", "feed");
        
        when(petService.interact(testUser.getId(), "feed")).thenReturn("小咪吃得很开心！");
        when(petService.getPet(testUser.getId())).thenReturn(testPet);

        // Act
        Map<String, Object> response = petController.interact(body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("小咪吃得很开心！", response.get("message"));
        assertEquals(testPet.getMood(), response.get("mood"));
        assertEquals(testPet.getExp(), response.get("exp"));
        assertEquals(testPet.getLevel(), response.get("level"));
        
        verify(petService, times(1)).interact(testUser.getId(), "feed");
        verify(petService, times(1)).getPet(testUser.getId());
    }

    /**
     * 测试宠物互动 - 成功（无请求体，默认聊天）
     * 前置条件：用户已登录且有宠物
     * 输入数据：空的请求体
     * 预期结果：默认执行聊天互动
     */
    @Test
    @DisplayName("宠物互动 - 成功（无请求体，默认聊天）")
    void testInteract_Success_NoBody() {
        // Arrange
        session.setAttribute("user", testUser);
        
        when(petService.interact(testUser.getId(), "talk")).thenReturn("小咪和你聊得很开心！");
        when(petService.getPet(testUser.getId())).thenReturn(testPet);

        // Act
        Map<String, Object> response = petController.interact(null, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("小咪和你聊得很开心！", response.get("message"));
        assertEquals(testPet.getMood(), response.get("mood"));
        assertEquals(testPet.getExp(), response.get("exp"));
        assertEquals(testPet.getLevel(), response.get("level"));
        
        verify(petService, times(1)).interact(testUser.getId(), "talk");
        verify(petService, times(1)).getPet(testUser.getId());
    }

    /**
     * 测试宠物互动 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：有效的互动类型
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("宠物互动 - 用户未登录")
    void testInteract_UserNotLoggedIn() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("interactType", "feed");

        // Act
        Map<String, Object> response = petController.interact(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(petService, never()).interact(any(), any());
    }

    /**
     * 测试宠物互动 - 服务异常
     * 前置条件：用户已登录
     * 输入数据：有效的互动类型
     * 预期结果：返回互动失败提示
     */
    @Test
    @DisplayName("宠物互动 - 服务异常")
    void testInteract_ServiceException() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, String> body = new HashMap<>();
        body.put("interactType", "feed");
        
        when(petService.interact(testUser.getId(), "feed"))
                .thenThrow(new RuntimeException("宠物不存在"));

        // Act
        Map<String, Object> response = petController.interact(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("互动失败: 宠物不存在", response.get("message"));
        
        verify(petService, times(1)).interact(testUser.getId(), "feed");
    }

    /**
     * 测试获取随机鼓励语 - 成功
     * 前置条件：无特殊前置条件
     * 输入数据：无
     * 预期结果：返回随机鼓励语
     */
    @Test
    @DisplayName("获取随机鼓励语 - 成功")
    void testGetRandomMessage_Success() {
        // Arrange
        String expectedMessage = "加油，你是最棒的！";
        when(petService.getRandomMessage()).thenReturn(expectedMessage);

        // Act
        Map<String, Object> response = petController.getRandomMessage();

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(expectedMessage, response.get("message"));
        
        verify(petService, times(1)).getRandomMessage();
    }

    /**
     * 测试获取所有鼓励语 - 成功
     * 前置条件：无特殊前置条件
     * 输入数据：无
     * 预期结果：返回所有鼓励语列表
     */
    @Test
    @DisplayName("获取所有鼓励语 - 成功")
    void testGetAllMessages_Success() {
        // Arrange
        List<String> expectedMessages = Arrays.asList(
                "加油，你是最棒的！",
                "坚持就是胜利！",
                "相信自己，你能行！"
        );
        when(petService.getAllMessages()).thenReturn(expectedMessages);

        // Act
        Map<String, Object> response = petController.getAllMessages();

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(expectedMessages, response.get("messages"));
        
        verify(petService, times(1)).getAllMessages();
    }

    /**
     * 测试一键孵化 - 成功
     * 前置条件：用户已登录且有未孵化的宠物
     * 输入数据：有效的用户会话
     * 预期结果：成功孵化宠物
     */
    @Test
    @DisplayName("一键孵化 - 成功")
    void testInstantHatch_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        when(petService.instantHatch(testUser.getId())).thenReturn(testPet);
        when(petService.getStageName(testPet.getStage())).thenReturn("幼体期");
        when(petService.getExpToNextLevel(testPet.getExp())).thenReturn(50);

        // Act
        Map<String, Object> response = petController.instantHatch(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("孵化成功！", response.get("message"));
        assertNotNull(response.get("pet"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pet = (Map<String, Object>) response.get("pet");
        assertEquals(testPet.getPetName(), pet.get("name"));
        assertEquals(testPet.getPetType(), pet.get("type"));
        
        verify(petService, times(1)).instantHatch(testUser.getId());
    }

    /**
     * 测试一键孵化 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("一键孵化 - 用户未登录")
    void testInstantHatch_UserNotLoggedIn() {
        // Arrange - 不设置用户会话

        // Act
        Map<String, Object> response = petController.instantHatch(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(petService, never()).instantHatch(any());
    }

    /**
     * 测试一键孵化 - 宠物不存在或已孵化
     * 前置条件：用户已登录
     * 输入数据：有效的用户会话
     * 预期结果：返回孵化失败提示
     */
    @Test
    @DisplayName("一键孵化 - 宠物不存在或已孵化")
    void testInstantHatch_PetNotExistOrAlreadyHatched() {
        // Arrange
        session.setAttribute("user", testUser);
        when(petService.instantHatch(testUser.getId())).thenReturn(null);

        // Act
        Map<String, Object> response = petController.instantHatch(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("宠物不存在或已经孵化", response.get("message"));
        
        verify(petService, times(1)).instantHatch(testUser.getId());
    }

    /**
     * 测试一键孵化 - 服务异常
     * 前置条件：用户已登录
     * 输入数据：有效的用户会话
     * 预期结果：返回孵化失败提示
     */
    @Test
    @DisplayName("一键孵化 - 服务异常")
    void testInstantHatch_ServiceException() {
        // Arrange
        session.setAttribute("user", testUser);
        when(petService.instantHatch(testUser.getId()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        Map<String, Object> response = petController.instantHatch(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("孵化失败: 数据库连接失败", response.get("message"));
        
        verify(petService, times(1)).instantHatch(testUser.getId());
    }
}