-- =====================================================
-- 无人自习室智能门禁与助学平台 - Spring Boot 自动初始化脚本
-- 使用 CREATE TABLE IF NOT EXISTS，已存在的表不会被覆盖
-- =====================================================

-- 1. 用户表 (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT '👤' COMMENT '头像(emoji或URL)',
    signature VARCHAR(100) DEFAULT NULL COMMENT '个性签名',
    today_status VARCHAR(50) DEFAULT '努力学习中 📚' COMMENT '今日状态',
    study_days INT DEFAULT 0 COMMENT '学习天数',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role ENUM('user', 'admin') DEFAULT 'user' COMMENT '角色',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 为已存在的表添加新字段（忽略错误）
ALTER TABLE users ADD COLUMN IF NOT EXISTS signature VARCHAR(100) DEFAULT NULL COMMENT '个性签名';
ALTER TABLE users ADD COLUMN IF NOT EXISTS today_status VARCHAR(50) DEFAULT '努力学习中 📚' COMMENT '今日状态';
ALTER TABLE users ADD COLUMN IF NOT EXISTS study_days INT DEFAULT 0 COMMENT '学习天数';

-- 2. 座位表 (seats)
CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    seat_number VARCHAR(10) NOT NULL UNIQUE COMMENT '座位编号',
    status ENUM('available', 'occupied', 'reserved', 'maintenance') DEFAULT 'available' COMMENT '座位状态',
    power_on TINYINT(1) DEFAULT 0 COMMENT '电源状态：0-断电，1-通电',
    description VARCHAR(100) DEFAULT NULL COMMENT '座位描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- 3. 订单表 (orders)
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

-- 4. 专注记录表 (focus_records)
CREATE TABLE IF NOT EXISTS focus_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
    duration INT NOT NULL COMMENT '专注时长(秒)',
    type VARCHAR(20) DEFAULT 'free' COMMENT '专注类型: free(自由专注), pomodoro(番茄钟)',
    focus_date DATE NOT NULL COMMENT '专注日期',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_focus_date (focus_date),
    INDEX idx_user_date (user_id, focus_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专注记录表';

-- 为已存在的focus_records表添加type字段
ALTER TABLE focus_records ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'free' COMMENT '专注类型: free(自由专注), pomodoro(番茄钟)';

-- 5. 用户宠物表 (user_pets)
CREATE TABLE IF NOT EXISTS user_pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '宠物ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    pet_name VARCHAR(50) DEFAULT '小蛋蛋' COMMENT '宠物名称',
    pet_type VARCHAR(20) DEFAULT 'cat' COMMENT '宠物类型',
    exp INT DEFAULT 0 COMMENT '经验值',
    level INT DEFAULT 1 COMMENT '等级',
    stage ENUM('egg', 'baby', 'teen', 'adult', 'professional') DEFAULT 'egg' COMMENT '进化阶段',
    mood INT DEFAULT 100 COMMENT '心情值(0-100)',
    last_interact_time DATETIME DEFAULT NULL COMMENT '上次互动时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户宠物表';

-- 6. 学习统计表 (learning_stats)
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

-- 为已存在的learning_stats表添加tomato_count字段
ALTER TABLE learning_stats ADD COLUMN IF NOT EXISTS tomato_count INT DEFAULT 0 COMMENT '当日番茄钟完成数';

-- 7. 门禁通行记录表 (access_logs)
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

-- 8. 座位预约表 (seat_reservations)
CREATE TABLE IF NOT EXISTS seat_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    reservation_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已取消，1-待使用，2-已使用，3-已过期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_date (reservation_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位预约表';

-- 9. 留言板表 (message_board)
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

-- 为已存在的message_board表添加images字段
ALTER TABLE message_board ADD COLUMN IF NOT EXISTS images TEXT DEFAULT NULL COMMENT '图片URL列表，多个用逗号分隔';

-- 10. 留言回复表 (message_replies)
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

-- 11. 鼓励卡片表 (encouragement_cards)
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

-- 12. 系统配置表 (system_config)
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始座位数据（INSERT IGNORE：已存在则跳过）
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

-- 初始管理员账号（INSERT IGNORE：已存在则跳过）
INSERT IGNORE INTO users (id, username, password, nickname, role) VALUES
(1, 'admin', '123456', '管理员', 'admin');

-- 测试用户账号
INSERT IGNORE INTO users (id, username, password, nickname, role) VALUES
(2, 'test', '123456', '测试用户', 'user');

-- 更多测试用户账号（用于排行榜测试）
INSERT IGNORE INTO users (id, username, password, nickname, avatar, today_status, study_days, role, status) VALUES
(3, 'zhangsan', '123456', '张三', '👨‍🎓', '冲刺考研中 📖', 45, 'user', 1),
(4, 'lisi', '123456', '李四', '👩‍💻', '准备期末复习 ✍️', 30, 'user', 1),
(5, 'wangwu', '123456', '王五', '🧑‍🔬', '学习新技术 💡', 60, 'user', 1),
(6, 'zhaoliu', '123456', '赵六', '👨‍🏫', '努力学习中 📚', 20, 'user', 1),
(7, 'sunqi', '123456', '孙七', '👩‍🎨', '备考CPA 📊', 55, 'user', 1);

-- 测试用户的学习统计数据（用于排行榜展示）
INSERT IGNORE INTO learning_stats (id, user_id, stat_date, total_duration, focus_count, avg_duration, max_duration, exp_earned, tomato_count) VALUES
-- zhangsan的数据
(1, 3, CURDATE(), 14400, 6, 2400, 3600, 240, 4),
(2, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 10800, 4, 2700, 4200, 180, 3),
(3, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 7200, 3, 2400, 3000, 120, 2),
-- lisi的数据
(4, 4, CURDATE(), 10800, 5, 2160, 3000, 180, 3),
(5, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 9000, 4, 2250, 3600, 150, 2),
-- wangwu的数据
(6, 5, CURDATE(), 18000, 8, 2250, 4500, 300, 5),
(7, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 14400, 6, 2400, 3600, 240, 4),
(8, 5, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 12600, 5, 2520, 3900, 210, 3),
-- zhaoliu的数据
(9, 6, CURDATE(), 5400, 2, 2700, 3600, 90, 1),
(10, 6, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 7200, 3, 2400, 3000, 120, 2),
-- sunqi的数据
(11, 7, CURDATE(), 16200, 7, 2314, 4200, 270, 4),
(12, 7, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 12600, 5, 2520, 3600, 210, 3);

-- 测试用户的宠物数据
INSERT IGNORE INTO user_pets (id, user_id, pet_name, pet_type, exp, level, stage, mood) VALUES
(1, 3, '小学霸', 'cat', 850, 9, 'teen', 95),
(2, 4, '小勤奋', 'dog', 520, 6, 'baby', 88),
(3, 5, '大学霸', 'rabbit', 1500, 15, 'adult', 100),
(4, 6, '小努力', 'cat', 200, 3, 'baby', 75),
(5, 7, '冲冲冲', 'dog', 1100, 11, 'teen', 92);

-- 测试留言板数据
INSERT IGNORE INTO message_board (id, user_id, content, reply_count, status) VALUES
(1, 3, '今天又是努力学习的一天！大家加油！💪', 2, 1),
(2, 5, '考研倒计时100天，感觉时间好紧张啊...', 3, 1),
(3, 4, '自习室的环境真好，学习效率提升了很多~', 1, 1);

-- 测试留言回复数据
INSERT IGNORE INTO message_replies (id, message_id, user_id, content, status) VALUES
(1, 1, 4, '一起加油！我们都能行的！', 1),
(2, 1, 5, '今天也完成了5个番茄钟，开心~', 1),
(3, 2, 3, '加油！我去年也是这样过来的，相信自己！', 1),
(4, 2, 7, '一起冲！上岸后请吃饭😂', 1),
(5, 2, 4, '加油加油，你已经很棒了！', 1),
(6, 3, 6, '确实！这里学习氛围很好', 1);
