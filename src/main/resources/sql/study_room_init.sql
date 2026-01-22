-- =====================================================
-- 无人自习室智能门禁与助学平台 - 完整数据库初始化脚本
-- 项目：基于Spring Boot的无人自习室智能门禁协同与沉浸式助学空间
-- 作者：黎志鹏 (22软件3班 / 2210412115)
-- 版本：V1.1
-- 更新：2026-01-05
-- 
-- 特性：
-- - 使用 CREATE TABLE IF NOT EXISTS，已存在的表不会被覆盖
-- - 使用 INSERT IGNORE，已存在的数据不会重复插入
-- - 可安全在已有数据的数据库上执行
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
-- 注意：如果表已存在则跳过，不会影响已有用户数据
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
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
    INDEX idx_status (status),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =====================================================
-- 4. 专注记录表 (focus_records) - 沉浸式助学模块
-- =====================================================
CREATE TABLE IF NOT EXISTS focus_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT DEFAULT NULL COMMENT '关联订单ID（可选）',
    duration INT NOT NULL COMMENT '专注时长(秒)',
    type VARCHAR(20) DEFAULT 'free' COMMENT '专注类型: free/pomodoro',
    focus_date DATE NOT NULL COMMENT '专注日期',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_focus_date (focus_date),
    INDEX idx_user_date (user_id, focus_date),
    CONSTRAINT fk_focus_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_pet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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
    INDEX idx_stat_date (stat_date),
    CONSTRAINT fk_stats_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_access_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁通行记录表';

-- =====================================================
-- 初始数据（仅在数据不存在时插入）
-- INSERT IGNORE：如果主键/唯一键冲突则跳过，不报错
-- =====================================================

-- 座位数据（仅当 seats 表为空时插入）
-- 使用存储过程确保只在表为空时插入
DELIMITER //
CREATE PROCEDURE init_seats_if_empty()
BEGIN
    DECLARE seat_count INT;
    SELECT COUNT(*) INTO seat_count FROM seats;
    IF seat_count = 0 THEN
        INSERT INTO seats (seat_number, description) VALUES 
        ('A01', '靠窗位置'),
        ('A02', '靠窗位置'),
        ('A03', '靠窗位置'),
        ('B01', '中间区域'),
        ('B02', '中间区域'),
        ('B03', '中间区域'),
        ('C01', '角落安静区'),
        ('C02', '角落安静区');
    END IF;
END //
DELIMITER ;

-- 执行存储过程
CALL init_seats_if_empty();

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS init_seats_if_empty;

-- =====================================================
-- 建表完成！
-- 共 7 张表：users, seats, orders, focus_records, 
--           user_pets, learning_stats, access_logs
-- 
-- 安全特性：
-- - CREATE TABLE IF NOT EXISTS：表存在则跳过
-- - 座位数据：仅在表为空时插入
-- - 不会覆盖任何已有数据
-- =====================================================
