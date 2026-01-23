# 📊 数据库变更日志 (Database Changelog)

> **创建日期**：2026-01-22  
> **维护人**：AI + 开发团队  
> **说明**：记录所有数据库结构和数据变更，保证可追溯性

---

## 📋 变更记录

### [DB-002] 补充缺失字段（代码审查修复）
- **日期**：2026-01-22
- **类型**：新增字段
- **表名**：`users`, `focus_records`, `seat_reservations`
- **变更详情**：
  ```sql
  -- users 表新增 hide_ranking 字段
  ALTER TABLE users ADD COLUMN IF NOT EXISTS hide_ranking TINYINT(1) DEFAULT 0 COMMENT '是否隐藏排行榜：0-显示，1-隐藏';
  
  -- focus_records 表新增 client_id 字段（用于幂等性校验）
  ALTER TABLE focus_records ADD COLUMN IF NOT EXISTS client_id VARCHAR(64) DEFAULT NULL COMMENT '客户端ID，用于幂等性校验';
  
  -- seat_reservations 表新增 check_in_time 字段
  ALTER TABLE seat_reservations ADD COLUMN IF NOT EXISTS check_in_time DATETIME DEFAULT NULL COMMENT '签到时间';
  ```
- **兼容性**：向后兼容（所有新字段都有 DEFAULT 值）
- **关联修改**：
  - [x] schema.sql 已更新
  - [x] study_room_init.sql 已更新
  - [x] Java 实体类已存在对应字段

---

### [DB-001] 初始数据库结构
- **日期**：2026-01-22
- **类型**：初始化
- **表清单**（共12张表）：
  1. `users` - 用户表
  2. `seats` - 座位表
  3. `orders` - 订单表
  4. `focus_records` - 专注记录表
  5. `user_pets` - 用户宠物表
  6. `learning_stats` - 学习统计表
  7. `access_logs` - 门禁通行记录表
  8. `seat_reservations` - 座位预约表
  9. `message_board` - 留言板表
  10. `message_replies` - 留言回复表
  11. `encouragement_cards` - 鼓励卡片表
  12. `system_config` - 系统配置表
- **兼容性**：初始版本
- **关联修改**：
  - [x] schema.sql 已创建
  - [x] study_room_init.sql 已创建
  - [x] 迁移文档已创建

---

## 📈 数据库版本摘要

| 版本 | 日期 | 表数量 | 主要变更 |
|------|------|--------|----------|
| v1.0.1 | 2026-01-22 | 12 | 补充缺失字段 (hide_ranking, client_id, check_in_time) |
| v1.0.0 | 2026-01-22 | 12 | 初始版本 |

---

## 📝 变更模板

复制以下模板记录新的变更：

```markdown
### [DB-XXX] 变更简述
- **日期**：YYYY-MM-DD
- **类型**：新增表/删除表/新增字段/修改字段/新增索引/数据变更
- **表名**：涉及的表
- **变更详情**：
  ```sql
  -- DDL 语句
  ALTER TABLE xxx ADD COLUMN xxx;
  ```
- **兼容性**：向后兼容/需迁移脚本
- **关联修改**：
  - [ ] schema.sql 已更新
  - [ ] study_room_init.sql 已更新
  - [ ] Java 实体类已更新
  - [ ] 迁移文档已更新（如需要）
```

---

## ⚠️ 注意事项

1. **每次数据库变更必须在此文件记录**
2. **编号格式**：`DB-001`, `DB-002`, ...（递增）
3. **变更必须同步更新 schema.sql 和 study_room_init.sql**
4. **破坏性变更（删除表/字段）必须提供迁移脚本**
