/**
 * 云端自习室 - 宠物行为 AI
 * 实现宠物的自主行为逻辑
 */

// 宠物对话配置
const PET_DIALOGUES = {
    // 专注开始
    focus_start: [
        "加油！我陪着你！",
        "一起努力吧~",
        "今天也要好好学习哦！",
        "我会安静陪伴你的~",
        "专注模式启动！"
    ],
    // 专注里程碑
    focus_milestone: {
        15: ["15分钟了！继续保持~", "你真棒！", "加油加油！"],
        30: ["半小时了！太厉害了！", "休息一下？", "你是最棒的！"],
        45: ["45分钟！学霸本霸！", "要不要喝点水？"],
        60: ["一小时！佩服佩服~", "你太强了！", "我都困了你还在学！"],
        90: ["一个半小时！神仙吧！", "休息一下吧~"],
        120: ["两小时！！！", "你是机器人吗？", "太厉害了吧！"]
    },
    // 待机状态
    idle: [
        "...",
        "~♪",
        "(*^▽^*)",
        "٩(◕‿◕｡)۶",
        "( ´ ▽ ` )",
        "..."
    ],
    // 互动反馈
    interact: [
        "嘿嘿~",
        "摸摸头~",
        "喵~",
        "汪！",
        "开心！",
        "嗯？",
        "你好呀~",
        "(*´∀`*)"
    ],
    // 被拖动
    drag: [
        "哇！",
        "放我下来~",
        "好高！",
        "嘻嘻~",
        "飞起来了！"
    ],
    // 睡觉
    sleep: [
        "zzZ...",
        "好困...",
        "晚安~",
        "做个好梦..."
    ],
    // 心情低落
    sad: [
        "好久没理我了...",
        "有点寂寞...",
        "想你了~",
        "陪我玩嘛..."
    ],
    // 鼓励语
    encourage: [
        "你可以的！",
        "相信自己！",
        "努力会有回报的！",
        "我永远支持你！",
        "加油鸭！",
        "冲冲冲！",
        "今天也是元气满满的一天！"
    ]
};

// 宠物类型对应的 Emoji
const PET_EMOJIS = {
    cat: {
        egg: '🥚',
        baby: '🐱',
        teen: '😺',
        adult: '😸',
        professional: '🎓😸'
    },
    dog: {
        egg: '🥚',
        baby: '🐶',
        teen: '🐕',
        adult: '🦮',
        professional: '🎓🐶'
    },
    rabbit: {
        egg: '🥚',
        baby: '🐰',
        teen: '🐇',
        adult: '🐇',
        professional: '🎓🐰'
    }
};

// 心情对应的 Emoji
const MOOD_EMOJIS = {
    high: '😊',    // >= 70
    medium: '😐',  // 40-69
    low: '😢'      // < 40
};

/**
 * 宠物行为 AI 类
 */
class PetBehaviorAI {
    constructor() {
        this.currentState = 'idle';
        this.stateTimer = null;
        this.lastMilestone = 0;
    }
    
    /**
     * 根据上下文决定下一个行为
     * @param {object} context - 上下文信息
     * @returns {string} 下一个行为状态
     */
    decide(context) {
        const { isFocusing, focusDuration, mood, isDragging } = context;
        
        // 被拖动时
        if (isDragging) {
            return 'interact';
        }
        
        // 专注模式中
        if (isFocusing) {
            // 长时间专注后可能打盹
            if (focusDuration > 30 * 60 && Math.random() < 0.2) {
                return 'sleeping';
            }
            // 专注时大部分时间安静
            return Math.random() < 0.85 ? 'idle' : 'walking';
        }
        
        // 非专注模式 - 根据心情决定
        const actions = ['idle', 'walking', 'playing'];
        let weights;
        
        if (mood >= 70) {
            // 心情好：更活跃
            weights = [0.3, 0.3, 0.4];
        } else if (mood >= 40) {
            // 心情一般
            weights = [0.5, 0.3, 0.2];
        } else {
            // 心情低落：更安静
            weights = [0.7, 0.2, 0.1];
        }
        
        return this.weightedRandom(actions, weights);
    }
    
    /**
     * 加权随机选择
     * @param {array} items - 选项数组
     * @param {array} weights - 权重数组
     * @returns {any} 选中的项
     */
    weightedRandom(items, weights) {
        const totalWeight = weights.reduce((a, b) => a + b, 0);
        let random = Math.random() * totalWeight;
        
        for (let i = 0; i < items.length; i++) {
            random -= weights[i];
            if (random <= 0) {
                return items[i];
            }
        }
        
        return items[items.length - 1];
    }
    
    /**
     * 获取随机对话
     * @param {string} type - 对话类型
     * @returns {string} 对话内容
     */
    getRandomDialogue(type) {
        const dialogues = PET_DIALOGUES[type];
        if (!dialogues || dialogues.length === 0) {
            return '';
        }
        return dialogues[Math.floor(Math.random() * dialogues.length)];
    }
    
    /**
     * 检查专注里程碑
     * @param {number} focusDuration - 专注时长（秒）
     * @returns {string|null} 里程碑对话
     */
    checkMilestone(focusDuration) {
        const minutes = Math.floor(focusDuration / 60);
        const milestones = [15, 30, 45, 60, 90, 120];
        
        for (const milestone of milestones) {
            if (minutes >= milestone && this.lastMilestone < milestone) {
                this.lastMilestone = milestone;
                const dialogues = PET_DIALOGUES.focus_milestone[milestone];
                if (dialogues) {
                    return dialogues[Math.floor(Math.random() * dialogues.length)];
                }
            }
        }
        
        return null;
    }
    
    /**
     * 重置里程碑
     */
    resetMilestone() {
        this.lastMilestone = 0;
    }
    
    /**
     * 获取宠物 Emoji
     * @param {string} petType - 宠物类型
     * @param {string} stage - 进化阶段
     * @returns {string} Emoji
     */
    getPetEmoji(petType, stage) {
        const emojis = PET_EMOJIS[petType] || PET_EMOJIS.cat;
        return emojis[stage] || emojis.egg;
    }
    
    /**
     * 获取心情 Emoji
     * @param {number} mood - 心情值 (0-100)
     * @returns {string} Emoji
     */
    getMoodEmoji(mood) {
        if (mood >= 70) return MOOD_EMOJIS.high;
        if (mood >= 40) return MOOD_EMOJIS.medium;
        return MOOD_EMOJIS.low;
    }
    
    /**
     * 计算下次行为切换的延迟时间
     * @param {string} state - 当前状态
     * @returns {number} 延迟时间（毫秒）
     */
    getNextDelay(state) {
        switch (state) {
            case 'idle':
                return 3000 + Math.random() * 4000;  // 3-7秒
            case 'walking':
                return 2000 + Math.random() * 3000;  // 2-5秒
            case 'playing':
                return 1500 + Math.random() * 2000;  // 1.5-3.5秒
            case 'sleeping':
                return 5000 + Math.random() * 5000;  // 5-10秒
            default:
                return 3000;
        }
    }
}

/**
 * 宠物拖动处理类
 */
class PetDragHandler {
    constructor(element, options = {}) {
        this.element = element;
        this.options = {
            boundaryPadding: 20,
            onDragStart: null,
            onDragMove: null,
            onDragEnd: null,
            ...options
        };
        
        this.isDragging = false;
        this.startX = 0;
        this.startY = 0;
        this.currentX = options.initialX || 100;
        this.currentY = options.initialY || 300;
        
        this.init();
    }
    
    init() {
        // 设置初始位置
        this.updatePosition();
        
        // 绑定事件
        this.element.addEventListener('mousedown', this.onStart.bind(this));
        this.element.addEventListener('touchstart', this.onStart.bind(this), { passive: false });
        
        document.addEventListener('mousemove', this.onMove.bind(this));
        document.addEventListener('touchmove', this.onMove.bind(this), { passive: false });
        
        document.addEventListener('mouseup', this.onEnd.bind(this));
        document.addEventListener('touchend', this.onEnd.bind(this));
    }
    
    onStart(e) {
        e.preventDefault();
        this.isDragging = true;
        
        const point = e.touches ? e.touches[0] : e;
        this.startX = point.clientX - this.currentX;
        this.startY = point.clientY - this.currentY;
        
        this.element.classList.add('dragging');
        
        if (this.options.onDragStart) {
            this.options.onDragStart();
        }
    }
    
    onMove(e) {
        if (!this.isDragging) return;
        e.preventDefault();
        
        const point = e.touches ? e.touches[0] : e;
        let newX = point.clientX - this.startX;
        let newY = point.clientY - this.startY;
        
        // 边界检测
        const padding = this.options.boundaryPadding;
        const maxX = window.innerWidth - this.element.offsetWidth - padding;
        const maxY = window.innerHeight - this.element.offsetHeight - padding;
        
        newX = Math.max(padding, Math.min(maxX, newX));
        newY = Math.max(padding + 60, Math.min(maxY - 100, newY));  // 考虑顶部和底部导航
        
        this.currentX = newX;
        this.currentY = newY;
        this.updatePosition();
        
        if (this.options.onDragMove) {
            this.options.onDragMove(newX, newY);
        }
    }
    
    onEnd() {
        if (!this.isDragging) return;
        this.isDragging = false;
        
        this.element.classList.remove('dragging');
        
        if (this.options.onDragEnd) {
            this.options.onDragEnd(this.currentX, this.currentY);
        }
    }
    
    updatePosition() {
        this.element.style.left = `${this.currentX}px`;
        this.element.style.top = `${this.currentY}px`;
    }
    
    /**
     * 移动到指定位置（带动画）
     * @param {number} x - 目标 X
     * @param {number} y - 目标 Y
     * @param {number} duration - 动画时长（毫秒）
     */
    moveTo(x, y, duration = 2000) {
        const startX = this.currentX;
        const startY = this.currentY;
        const startTime = performance.now();
        
        const animate = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            // 缓动函数
            const easeProgress = 1 - Math.pow(1 - progress, 3);
            
            this.currentX = startX + (x - startX) * easeProgress;
            this.currentY = startY + (y - startY) * easeProgress;
            this.updatePosition();
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    }
    
    /**
     * 随机移动
     */
    randomMove() {
        const padding = this.options.boundaryPadding;
        const maxX = window.innerWidth - 80 - padding;
        const maxY = window.innerHeight - 200;
        
        const newX = padding + Math.random() * (maxX - padding);
        const newY = 150 + Math.random() * (maxY - 150);
        
        // 返回移动方向
        const direction = newX > this.currentX ? 'right' : 'left';
        
        this.moveTo(newX, newY, 2000 + Math.random() * 1000);
        
        return direction;
    }
    
    /**
     * 获取当前位置
     */
    getPosition() {
        return { x: this.currentX, y: this.currentY };
    }
    
    /**
     * 设置位置（不带动画）
     */
    setPosition(x, y) {
        this.currentX = x;
        this.currentY = y;
        this.updatePosition();
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        PetBehaviorAI,
        PetDragHandler,
        PET_DIALOGUES,
        PET_EMOJIS,
        MOOD_EMOJIS
    };
}
