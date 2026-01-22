# 🗄️ Skill: 数据库操作 (Database)

> **技能 ID**: `database`
> **触发关键词**: 表、字段、SQL、数据库、迁移、索引、schema

## 1. 技能职责

- 设计数据库表结构
- 编写 SQL 迁移脚本
- 优化查询性能
- 处理数据一致性

## 2. 技术约束

| 项目 | 说明 |
|------|------|
| **数据库** | MySQL 8.x |
| **数据库名** | study_room |
| **初始化脚本** | `src/main/resources/schema.sql` |
| **初始化模式** | `spring.sql.init.mode=always` |

## 3. 现有表结构

| 表名 | 说明 |
|------|------|
| `users` | 用户表 |
| `seats` | 座位表 |
| `orders` | 入座订单 |
| `focus_records` | 专注记录 |
| `learning_stats` | 学习统计 |
| `user_pets` | 宠物 |
| `seat_reservations` | 座位预约 |
| `message_board` | 留言板 |
| `message_replies` | 留言回复 |
| `encouragement_cards` | 鼓励卡片 |
| `system_config` | 系统配置 |

## 4. 表设计规范

### 命名规范

| 项目 | 规范 | 示例 |
|------|------|------|
| 表名 | 小写 + 下划线 + 复数 | `focus_records` |
| 字段名 | 小写 + 下划线 | `user_id`, `created_at` |
| 主键 | `id` (AUTO_INCREMENT) | `id BIGINT PRIMARY KEY` |
| 外键字段 | `表名单数_id` | `user_id`, `seat_id` |
| 时间字段 | `xxx_at` | `created_at`, `updated_at` |

### 必须字段

```sql
CREATE TABLE xxx (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 业务字段
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 5. 迁移脚本规范

### 新增表

```sql
CREATE TABLE IF NOT EXISTS xxx (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 新增字段

```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS hide_ranking TINYINT(1) DEFAULT 0;
```

## 6. 脚本同步要求

修改数据库结构时必须同步更新：

1. `src/main/resources/schema.sql`
2. `src/md/03_CURRENT_STATUS_LOG.md`
3. 对应的 Entity 类

## 7. 数据迁移检查清单

- [ ] `schema.sql` 已更新
- [ ] 脚本可重复执行（IF NOT EXISTS）
- [ ] Entity 类已同步
- [ ] Mapper 查询已适配
- [ ] 文档已更新
