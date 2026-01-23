---
name: documentation
description: >
  项目文档编写与维护。当用户提到文档、README、说明、记录时激活此技能。
---

# 文档编写 (Documentation)

编写/更新项目文档、记录功能变更、同步代码注释。

## 文档目录

```
src/md/
├── core/                  # 核心规范
│   ├── 03_CURRENT_STATUS_LOG.md   # 【重点】项目状态日志
│   └── 13_ERROR_LEARNING_LOG.md   # 错误学习日志
├── design/                # 设计文档
│   ├── 01_PRODUCT_REQUIREMENTS_DOC.md
│   └── 02_SYSTEM_DESIGN_DOC.md
├── guide/                 # 开发指南
├── deploy/                # 部署迁移
│   ├── 14_MIGRATION_DEPLOY_GUIDE.md
│   └── 15_DATABASE_CHANGELOG.md
└── academic/              # 学术相关
```

## 更新优先级

| 优先级 | 文档 | 更新时机 |
|-------|------|---------|
| **必须** | `core/03_CURRENT_STATUS_LOG.md` | 每次代码变更后 |
| **必须** | `core/13_ERROR_LEARNING_LOG.md` | 发现错误时 |
| **按需** | `design/02_SYSTEM_DESIGN_DOC.md` | 架构/API变更时 |
| **按需** | `deploy/15_DATABASE_CHANGELOG.md` | 数据库变更时 |

## 状态日志格式

```markdown
- **🔧 变更简述（YYYY-MM-DD）**:
  - **问题**：描述问题
  - **修复内容**：
    - 修改点1
    - 修改点2
```

## 代码注释规范

### Java 类

```java
/**
 * 用户服务类
 * @author xxx
 * @since 2026-01-22
 */
@Service
public class UserService { }
```

### Java 方法

```java
/**
 * 保存专注记录
 * @param userId 用户ID
 * @param duration 时长（秒）
 * @return 记录ID
 */
public Long saveFocusRecord(Long userId, int duration) { }
```
