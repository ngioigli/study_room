-- =====================================================
-- 无人自习室智能门禁与助学平台 - 完整数据库初始化脚本
-- 项目：基于Spring Boot的无人自习室智能门禁协同与沉浸式助学空间
-- 作者：黎志鹏 (22软件3班 / 2210412115)
-- 版本：V2.0
-- 更新：2026-01-22
-- 
-- 特性：
-- - 使用 CREATE TABLE IF NOT EXISTS，已存在的表不会被覆盖
-- - 使用 INSERT IGNORE，已存在的数据不会重复插入
-- - 与 schema.sql 保持完全同步
-- 
-- 使用方法：
-- 1. 创建数据库：CREATE DATABASE study_room DEFAULT CHARSET utf8mb4;
-- 2. 导入此文件：mysql -u root -p study_room < study_room_init.sql
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS study_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE study_room;

-- =====================================================
-- 1. 用户表 (users) - 核心表
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT '👤' COMMENT '头像(emoji或URL)',
    signature VARCHAR(100) DEFAULT NULL COMMENT '个性签名',
    today_status VARCHAR(50) DEFAULT '努力学习中 📚' COMMENT '今日状态',
    study_days INT DEFAULT 0 COMMENT '学习天数',
    hide_ranking TINYINT(1) DEFAULT 0 COMMENT '是否隐藏排行榜：0-显示，1-隐藏',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role ENUM('user', 'admin') DEFAULT 'user' COMMENT '角色',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 座位表 (seats) - 座位电源控制模块
-- =====================================================
CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    seat_number VARCHAR(10) NOT NULL UNIQUE COMMENT '座位编号，如 A01, B02',
    status ENUM('available', 'occupied', 'reserved', 'maintenance') DEFAULT 'available' COMMENT '座位状态',
    power_on TINYINT(1) DEFAULT 0 COMMENT '电源状态：0-断电，1-通电',
    description VARCHAR(100) DEFAULT NULL COMMENT '座位描述，如靠窗、角落',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- =====================================================
-- 3. 订单表 (orders) - 座位电源控制模块
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    planned_duration INT DEFAULT NULL COMMENT '计划时长(分钟)',
    actual_duration INT DEFAULT NULL COMMENT '实际时长(分钟)',
    status ENUM('active', 'completed', 'cancelled', 'timeout') DEFAULT 'active' COMMENT '订单状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =====================================================
-- 4. 专注记录表 (focus_records) - 沉浸式助学模块
-- =====================================================
CREATE TABLE IF NOT EXISTS focus_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT DEFAULT NULL COMMENT '关联订单ID（可选）',
    duration INT NOT NULL COMMENT '专注时长(秒)',
    type VARCHAR(20) DEFAULT 'free' COMMENT '专注类型: free(自由专注), pomodoro(番茄钟)',
    client_id VARCHAR(64) DEFAULT NULL COMMENT '客户端ID，用于幂等性校验',
    focus_date DATE NOT NULL COMMENT '专注日期',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_focus_date (focus_date),
    INDEX idx_user_date (user_id, focus_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专注记录表';

-- =====================================================
-- 5. 用户宠物表 (user_pets) - 用户行为激励模块
-- =====================================================
CREATE TABLE IF NOT EXISTS user_pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '宠物ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID（一对一）',
    pet_name VARCHAR(50) DEFAULT '小蛋蛋' COMMENT '宠物名称',
    pet_type VARCHAR(20) DEFAULT 'cat' COMMENT '宠物类型：cat, dog, rabbit',
    exp INT DEFAULT 0 COMMENT '经验值',
    level INT DEFAULT 1 COMMENT '等级',
    stage ENUM('egg', 'baby', 'teen', 'adult', 'professional') DEFAULT 'egg' COMMENT '进化阶段',
    mood INT DEFAULT 100 COMMENT '心情值(0-100)',
    last_interact_time DATETIME DEFAULT NULL COMMENT '上次互动时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户宠物表';

-- =====================================================
-- 6. 学习统计表 (learning_stats) - 数据分析模块
-- =====================================================
CREATE TABLE IF NOT EXISTS learning_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_duration INT DEFAULT 0 COMMENT '当日总时长(秒)',
    focus_count INT DEFAULT 0 COMMENT '专注次数',
    avg_duration INT DEFAULT 0 COMMENT '平均时长(秒)',
    max_duration INT DEFAULT 0 COMMENT '最长单次时长(秒)',
    exp_earned INT DEFAULT 0 COMMENT '当日获得经验值',
    tomato_count INT DEFAULT 0 COMMENT '当日番茄钟完成数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习统计表';

-- =====================================================
-- 7. 门禁通行记录表 (access_logs) - 智能门禁模块
-- =====================================================
CREATE TABLE IF NOT EXISTS access_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action_type ENUM('lock', 'unlock', 'enter', 'exit') NOT NULL COMMENT '操作类型',
    lock_start_time DATETIME DEFAULT NULL COMMENT '锁定开始时间',
    lock_end_time DATETIME DEFAULT NULL COMMENT '锁定结束时间',
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    user_agent VARCHAR(255) DEFAULT NULL COMMENT '用户代理',
    result ENUM('success', 'fail', 'timeout') DEFAULT 'success' COMMENT '操作结果',
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁通行记录表';

-- =====================================================
-- 8. 座位预约表 (seat_reservations)
-- =====================================================
CREATE TABLE IF NOT EXISTS seat_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    reservation_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已取消，1-待使用，2-已使用，3-已过期',
    check_in_time DATETIME DEFAULT NULL COMMENT '签到时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_date (reservation_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位预约表';

-- =====================================================
-- 9. 留言板表 (message_board)
-- =====================================================
CREATE TABLE IF NOT EXISTS message_board (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '留言ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '留言内容',
    images TEXT DEFAULT NULL COMMENT '图片URL列表，多个用逗号分隔',
    reply_count INT DEFAULT 0 COMMENT '回复数量',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言板表';

-- =====================================================
-- 10. 留言回复表 (message_replies)
-- =====================================================
CREATE TABLE IF NOT EXISTS message_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '回复ID',
    message_id BIGINT NOT NULL COMMENT '留言ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content VARCHAR(500) NOT NULL COMMENT '回复内容',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_message_id (message_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言回复表';

-- =====================================================
-- 11. 鼓励卡片表 (encouragement_cards)
-- =====================================================
CREATE TABLE IF NOT EXISTS encouragement_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '卡片ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID（匿名则为NULL）',
    emoji VARCHAR(10) DEFAULT '🌸' COMMENT '表情',
    message VARCHAR(200) NOT NULL COMMENT '鼓励内容',
    likes INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_likes (likes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鼓励卡片表';

-- =====================================================
-- 12. 系统配置表 (system_config)
-- =====================================================
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =====================================================
-- 初始数据（使用 INSERT IGNORE 防止重复插入）
-- =====================================================

-- 初始座位数据
INSERT IGNORE INTO seats (id, seat_number, description) VALUES 
(1, 'A01', '靠窗位置'),
(2, 'A02', '靠窗位置'),
(3, 'A03', '靠窗位置'),
(4, 'B01', '中间区域'),
(5, 'B02', '中间区域'),
(6, 'B03', '中间区域'),
(7, 'C01', '角落安静区'),
(8, 'C02', '角落安静区');

-- 初始鼓励卡片数据
INSERT IGNORE INTO encouragement_cards (id, emoji, message, likes) VALUES
(1, '🌸', '学习累了就休息一下吧，你已经很棒了！', 15),
(2, '⭐', '每一点努力都不会白费，加油！', 23),
(3, '🌈', '雨后总会有彩虹，坚持就是胜利！', 18),
(4, '💪', '相信自己，你比想象中更强大！', 31),
(5, '🎯', '专注当下，未来会感谢现在努力的你！', 27),
(6, '☀️', '新的一天，新的开始，你可以的！', 12),
(7, '🌻', '向阳而生，你的努力终会开花结果！', 20),
(8, '💖', '有人默默为你加油，你不是一个人在战斗！', 35);

-- 初始管理员账号
INSERT IGNORE INTO users (id, username, password, nickname, role) VALUES
(1, 'admin', '123456', '管理员', 'admin');

-- 测试用户账号
INSERT IGNORE INTO users (id, username, password, nickname, role) VALUES
(2, 'test', '123456', '测试用户', 'user');

-- =====================================================
-- 建表完成！
-- 共 12 张表：users, seats, orders, focus_records, 
--           user_pets, learning_stats, access_logs,
--           seat_reservations, message_board, message_replies,
--           encouragement_cards, system_config
-- 
-- 安全特性：
-- - CREATE TABLE IF NOT EXISTS：表存在则跳过
-- - INSERT IGNORE：数据存在则跳过
-- - 不会覆盖任何已有数据
-- =====================================================
