---
name: database
description: >
  数据库表结构设计与SQL迁移脚本编写。当用户提到表、字段、SQL、数据库、
  迁移、索引、schema、ALTER TABLE时激活此技能。
---

# 数据库操作 (Database)

设计表结构、编写迁移脚本、优化查询性能。

## 技术约束

| 项目 | 说明 |
|------|------|
| 数据库 | MySQL 8.x |
| 数据库名 | study_room |
| 初始化脚本 | `src/main/resources/schema.sql` |
| 完整脚本 | `src/main/resources/sql/study_room_init.sql` |

## 现有表结构

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
| `access_logs` | 访问日志 |

## 命名规范

| 项目 | 规范 | 示例 |
|------|------|------|
| 表名 | 小写+下划线+复数 | `focus_records` |
| 字段名 | 小写+下划线 | `user_id` |
| 主键 | `id` | `BIGINT AUTO_INCREMENT` |
| 外键 | `表名单数_id` | `user_id` |
| 时间字段 | `xxx_at` | `created_at` |

## 必须字段模板

```sql
CREATE TABLE IF NOT EXISTS xxx (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 业务字段
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 新增字段模板

```sql
ALTER TABLE xxx ADD COLUMN IF NOT EXISTS new_field VARCHAR(100) DEFAULT NULL COMMENT '说明';
```

## 强制同步要求

修改数据库结构时必须同步更新：
1. `src/main/resources/schema.sql`
2. `src/main/resources/sql/study_room_init.sql`
3. `src/md/deploy/15_DATABASE_CHANGELOG.md`
4. 对应的 Entity 类
