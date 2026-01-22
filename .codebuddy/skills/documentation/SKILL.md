# 📝 Skill: 文档编写 (Documentation)

> **技能 ID**: `documentation`
> **触发关键词**: 文档、README、说明、记录

## 1. 技能职责

- 编写/更新项目文档
- 记录功能变更
- 同步代码注释
- 维护 API 文档

## 2. 文档目录

```
src/md/
├── 00_AI_DEVELOPER_RULES.md      # AI 开发规范
├── 01_PRODUCT_REQUIREMENTS_DOC.md # 产品需求文档
├── 02_SYSTEM_DESIGN_DOC.md       # 系统设计文档
├── 03_CURRENT_STATUS_LOG.md      # 【重点】项目状态日志
├── 04_THESIS_OUTLINE.md          # 论文大纲
├── 05_AI_HANDOVER_PROTOCOL.md    # AI 交接协议
├── 07_FINAL_TASK_BOOK.md         # 最终任务书
├── 08_ULTIMATE_DEV_GUIDE.md      # 终极开发指南
├── 09_GAMIFICATION_MODULE_DESIGN.md # 游戏化模块设计
├── 10_UI_DESIGN_SPECIFICATION.md # UI 设计规范
└── 11_QUICK_DEPLOY_GUIDE.md      # 快速部署指南
```

## 3. 更新优先级

| 优先级 | 文档 | 更新时机 |
|-------|------|---------|
| **必须** | `03_CURRENT_STATUS_LOG.md` | 每次代码变更后立即更新 |
| **按需** | `02_SYSTEM_DESIGN_DOC.md` | 架构、API、数据库变更时更新 |
| **按需** | `01_PRODUCT_REQUIREMENTS_DOC.md` | 功能需求变更时更新 |
| **按需** | `11_QUICK_DEPLOY_GUIDE.md` | 部署流程变更时更新 |

## 4. 状态日志格式

更新 `03_CURRENT_STATUS_LOG.md` 时：

```markdown
- **🔧 变更简述（YYYY-MM-DD）**:
  - **问题**：描述问题
  - **修复内容**：
    - 修改点1
    - 修改点2
```

## 5. 代码注释规范

### Java 类注释

```java
/**
 * 用户服务类
 * 
 * @author xxx
 * @since 2026-01-22
 */
@Service
public class UserService { }
```

### Java 方法注释

```java
/**
 * 保存专注记录
 * 
 * @param userId 用户ID
 * @param duration 时长（秒）
 * @param type 类型（free/pomodoro）
 * @return 记录ID
 */
public Long saveFocusRecord(Long userId, int duration, String type) { }
```

## 6. 文档检查清单

- [ ] `03_CURRENT_STATUS_LOG.md` 已更新
- [ ] 如涉及架构/API 变更，已更新 `02_SYSTEM_DESIGN_DOC.md`
- [ ] 如涉及部署流程，已更新 `11_QUICK_DEPLOY_GUIDE.md`
- [ ] 代码关键方法有注释
