package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.UserPet;
import com.example.qr_code.mapper.UserPetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class PetService {

    @Autowired
    private UserPetMapper userPetMapper;

    // 进化阶段经验阈值
    private static final int EXP_BABY = 100;      // 幼年体
    private static final int EXP_TEEN = 500;      // 少年体
    private static final int EXP_ADULT = 2000;    // 成年体
    private static final int EXP_PROFESSIONAL = 10000; // 职业形态

    // 鼓励语列表（扩展版）
    private static final List<String> ENCOURAGE_MESSAGES = Arrays.asList(
        // 学习鼓励类
        "今天也要加油哦！",
        "你是最棒的！继续保持！",
        "学习使我快乐~",
        "我相信你一定可以的！",
        "每一分钟的努力都有意义",
        "坚持就是胜利！",
        "你已经很努力了，真棒！",
        "知识就是力量！",
        "今天的你比昨天更优秀",
        "一步一个脚印，稳扎稳打",
        "加油！胜利就在前方",
        "学习的时光最美好",
        
        // 生活化内容
        "今天吃得好饱呀~",
        "阳光真舒服，让人想打盹~",
        "主人陪我玩真开心！",
        "我今天学会了新技能呢！",
        "感觉自己变聪明了一点点~",
        "主人真是我最好的朋友！",
        "今天的心情特别好~",
        "我想和主人一起看星星",
        "主人的笑容最治愈了",
        "和主人在一起的每一天都很幸福",
        "我会一直陪着主人的！",
        "主人辛苦了，要记得休息哦",
        
        // 休息提醒类
        "休息一下也很重要呢",
        "累了就休息，我陪着你",
        "你的努力我都看在眼里",
        "适当放松才能走得更远",
        "劳逸结合最重要啦！",
        "喝口水，休息一下吧~",
        "眼睛累了就看看远方",
        "深呼吸，放松一下心情",
        
        // 时间相关（通用）
        "时间过得真快呀~",
        "每一刻都很珍贵呢",
        "和主人度过的时光最美好",
        "今天又是充实的一天！",
        "时光荏苒，我们一起成长",
        
        // 情感支持类
        "主人最棒了！我相信你！",
        "遇到困难不要怕，我在这里",
        "你已经做得很好了",
        "每个人都有低谷期，会过去的",
        "相信自己，你比想象中更强大",
        "失败是成功之母，不要气馁",
        "我会永远支持你的！",
        
        // 互动回应类
        "谢谢主人陪我玩~",
        "和主人聊天最开心了",
        "主人今天过得怎么样？",
        "我想听主人讲故事",
        "主人有什么烦恼吗？",
        "我们一起努力吧！",
        "主人的温柔让我很安心"
    );

    /**
     * 获取用户的宠物
     */
    public UserPet getPet(Long userId) {
        QueryWrapper<UserPet> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return userPetMapper.selectOne(wrapper);
    }

    /**
     * 创建宠物
     */
    @Transactional
    public UserPet createPet(Long userId, String petType, String petName) {
        // 检查是否已有宠物
        UserPet existing = getPet(userId);
        if (existing != null) {
            return existing;
        }

        UserPet pet = new UserPet();
        pet.setUserId(userId);
        pet.setPetType(petType != null ? petType : "cat");
        pet.setPetName(petName != null && !petName.isEmpty() ? petName : "小蛋蛋");
        pet.setExp(0);
        pet.setLevel(1);
        pet.setStage("egg");
        pet.setMood(100);
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());

        userPetMapper.insert(pet);
        return pet;
    }

    /**
     * 增加经验值并检查进化
     */
    @Transactional
    public UserPet addExp(Long userId, int expToAdd) {
        UserPet pet = getPet(userId);
        if (pet == null) {
            // 自动创建宠物
            pet = createPet(userId, "cat", null);
        }

        int newExp = pet.getExp() + expToAdd;
        pet.setExp(newExp);

        // 计算新等级（每100经验升1级）
        int newLevel = calculateLevel(newExp);
        pet.setLevel(newLevel);

        // 检查进化
        String newStage = calculateStage(newExp);
        pet.setStage(newStage);

        pet.setUpdatedAt(LocalDateTime.now());
        userPetMapper.updateById(pet);

        return pet;
    }

    /**
     * 与宠物互动（支持不同类型）
     */
    @Transactional
    public String interact(Long userId, String interactType) {
        UserPet pet = getPet(userId);
        if (pet == null) {
            return "你还没有宠物呢，快去孵化一只吧！";
        }

        int moodIncrease = 5; // 默认心情增加值
        String message;

        // 根据互动类型设置不同的心情增加值和消息
        switch (interactType) {
            case "feed":
                moodIncrease = 20;
                message = "好吃好吃~谢谢主人的投喂！";
                break;
            case "pet":
                moodIncrease = 10;
                message = "好舒服~主人的手好温暖呢~";
                break;
            case "play":
                moodIncrease = 15;
                message = "太好玩了！和主人一起玩最开心了~";
                break;
            case "talk":
                moodIncrease = 5;
                message = getTimeBasedMessage();
                break;
            default:
                message = getRandomMessage();
        }

        // 更新心情值
        int newMood = Math.min(100, pet.getMood() + moodIncrease);
        pet.setMood(newMood);
        pet.setLastInteractTime(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
        userPetMapper.updateById(pet);

        return message;
    }

    /**
     * 与宠物互动（兼容旧版本）
     */
    @Transactional
    public String interact(Long userId) {
        return interact(userId, "talk");
    }

    /**
     * 根据时间获取对话消息
     */
    private String getTimeBasedMessage() {
        int hour = LocalDateTime.now().getHour();
        
        if (hour < 12) {
            // 早上
            List<String> morningMessages = Arrays.asList(
                "早上好啊主人！", "新的一天开始啦~", "今天也要加油哦！",
                "早起的鸟儿有虫吃~", "美好的一天从现在开始！"
            );
            return morningMessages.get(new Random().nextInt(morningMessages.size()));
        } else if (hour < 18) {
            // 下午
            List<String> afternoonMessages = Arrays.asList(
                "下午好~", "学习累了吗？", "要不要休息一下？",
                "下午的阳光真舒服~", "继续加油哦！"
            );
            return afternoonMessages.get(new Random().nextInt(afternoonMessages.size()));
        } else {
            // 晚上
            List<String> eveningMessages = Arrays.asList(
                "晚上好！", "今天辛苦了~", "晚安，好梦~",
                "夜深了，注意休息哦~", "今天学得怎么样？"
            );
            return eveningMessages.get(new Random().nextInt(eveningMessages.size()));
        }
    }

    /**
     * 计算带心情加成的经验值
     */
    public int calculateExpWithMoodBonus(int baseExp, int mood) {
        if (mood >= 100) {
            // 满心情额外获得30%经验
            return (int) (baseExp * 1.3);
        } else if (mood >= 80) {
            // 高心情额外获得15%经验
            return (int) (baseExp * 1.15);
        } else if (mood >= 60) {
            // 中等心情额外获得5%经验
            return (int) (baseExp * 1.05);
        }
        return baseExp; // 低心情无加成
    }

    /**
     * 获取随机鼓励语
     */
    public String getRandomMessage() {
        Random random = new Random();
        return ENCOURAGE_MESSAGES.get(random.nextInt(ENCOURAGE_MESSAGES.size()));
    }

    /**
     * 获取所有鼓励语
     */
    public List<String> getAllMessages() {
        return ENCOURAGE_MESSAGES;
    }

    /**
     * 计算等级
     */
    public int calculateLevel(int totalExp) {
        // 每100经验升1级
        return (totalExp / 100) + 1;
    }

    /**
     * 计算升到下一级所需经验
     */
    public int getExpToNextLevel(int currentExp) {
        int currentLevel = calculateLevel(currentExp);
        int nextLevelExp = currentLevel * 100;
        return nextLevelExp - currentExp;
    }

    /**
     * 根据经验值计算进化阶段
     */
    private String calculateStage(int exp) {
        if (exp >= EXP_PROFESSIONAL) {
            return "professional";
        } else if (exp >= EXP_ADULT) {
            return "adult";
        } else if (exp >= EXP_TEEN) {
            return "teen";
        } else if (exp >= EXP_BABY) {
            return "baby";
        } else {
            return "egg";
        }
    }

    /**
     * 获取进化阶段的中文名称
     */
    public String getStageName(String stage) {
        switch (stage) {
            case "egg": return "蛋";
            case "baby": return "幼年体";
            case "teen": return "少年体";
            case "adult": return "成年体";
            case "professional": return "职业形态";
            default: return "未知";
        }
    }

    /**
     * 更新心情值（每天自动衰减）
     */
    @Transactional
    public void updateMoodDecay(Long userId) {
        UserPet pet = getPet(userId);
        if (pet == null) return;

        // 如果超过24小时没互动，心情值衰减
        if (pet.getLastInteractTime() != null) {
            LocalDateTime lastInteract = pet.getLastInteractTime();
            if (lastInteract.plusHours(24).isBefore(LocalDateTime.now())) {
                int newMood = Math.max(0, pet.getMood() - 10);
                pet.setMood(newMood);
                pet.setUpdatedAt(LocalDateTime.now());
                userPetMapper.updateById(pet);
            }
        }
    }

    /**
     * 一键孵化（测试用）- 直接将蛋孵化为幼年体
     */
    @Transactional
    public UserPet instantHatch(Long userId) {
        UserPet pet = getPet(userId);
        if (pet == null) {
            return null;
        }

        // 只有蛋状态才能孵化
        if (!"egg".equals(pet.getStage())) {
            return pet; // 已经孵化过了，直接返回
        }

        // 设置经验值为100（刚好达到幼年体）
        pet.setExp(100);
        pet.setLevel(2);
        pet.setStage("baby");
        pet.setMood(100);
        pet.setUpdatedAt(LocalDateTime.now());
        userPetMapper.updateById(pet);

        return pet;
    }
}
