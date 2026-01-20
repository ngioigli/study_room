package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.entity.UserPet;
import com.example.qr_code.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetService petService;

    /**
     * 获取当前用户的宠物信息
     * GET /api/pet
     */
    @GetMapping
    public Map<String, Object> getPet(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        UserPet pet = petService.getPet(user.getId());
        
        result.put("success", true);
        if (pet != null) {
            result.put("hasPet", true);
            result.put("pet", buildPetVO(pet));
        } else {
            result.put("hasPet", false);
            result.put("message", "还没有宠物，快去孵化一只吧！");
        }

        return result;
    }

    /**
     * 创建宠物
     * POST /api/pet/create
     * Body: { "petType": "cat", "petName": "小咪" }
     */
    @PostMapping("/create")
    public Map<String, Object> createPet(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        String petType = body.get("petType");
        String petName = body.get("petName");

        try {
            UserPet pet = petService.createPet(user.getId(), petType, petName);
            result.put("success", true);
            result.put("message", "宠物孵化成功！");
            result.put("pet", buildPetVO(pet));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 与宠物互动
     * POST /api/pet/interact
     * Body: { "interactType": "feed" } // feed, pet, play, talk
     */
    @PostMapping("/interact")
    public Map<String, Object> interact(@RequestBody(required = false) Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            String interactType = "talk"; // 默认为聊天
            if (body != null && body.containsKey("interactType")) {
                interactType = body.get("interactType");
            }

            String message = petService.interact(user.getId(), interactType);
            UserPet pet = petService.getPet(user.getId());
            
            result.put("success", true);
            result.put("message", message);
            if (pet != null) {
                result.put("mood", pet.getMood());
                result.put("exp", pet.getExp());
                result.put("level", pet.getLevel());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "互动失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取随机鼓励语
     * GET /api/pet/message
     */
    @GetMapping("/message")
    public Map<String, Object> getRandomMessage() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", petService.getRandomMessage());
        return result;
    }

    /**
     * 获取所有鼓励语列表
     * GET /api/pet/messages
     */
    @GetMapping("/messages")
    public Map<String, Object> getAllMessages() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("messages", petService.getAllMessages());
        return result;
    }

    /**
     * 一键孵化（测试用）
     * POST /api/pet/instant-hatch
     */
    @PostMapping("/instant-hatch")
    public Map<String, Object> instantHatch(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            UserPet pet = petService.instantHatch(user.getId());
            if (pet != null) {
                result.put("success", true);
                result.put("message", "孵化成功！");
                result.put("pet", buildPetVO(pet));
            } else {
                result.put("success", false);
                result.put("message", "宠物不存在或已经孵化");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "孵化失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 构建宠物视图对象
     */
    private Map<String, Object> buildPetVO(UserPet pet) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", pet.getId());
        vo.put("name", pet.getPetName());
        vo.put("type", pet.getPetType());
        vo.put("exp", pet.getExp());
        vo.put("level", pet.getLevel());
        vo.put("stage", pet.getStage());
        vo.put("stageName", petService.getStageName(pet.getStage()));
        vo.put("mood", pet.getMood());
        vo.put("expToNextLevel", petService.getExpToNextLevel(pet.getExp()));
        vo.put("createdAt", pet.getCreatedAt());
        return vo;
    }
}
