# 云端自习室 - Agent Skills

本目录包含云端自习室项目的 Agent Skills，采用 Claude Skills 官方规范实现**渐进式披露（Progressive Disclosure）**机制。

## 渐进式披露原理

```
第1层: YAML frontmatter (name + description) → ~100 tokens，启动时加载
第2层: SKILL.md 正文 (完整指令)           → <5k tokens，匹配时加载
第3层: 引用脚本/资源                      → 按需加载
```

## 技能列表

| 技能 | 文件 | 触发场景 |
|------|------|----------|
| **code-review** | `code-review/SKILL.md` | 代码审查、优化、重构 |
| **database** | `database/SKILL.md` | 数据库表结构、SQL、迁移 |
| **frontend-dev** | `frontend-dev/SKILL.md` | HTML/CSS/JS 页面开发 |
| **backend-dev** | `backend-dev/SKILL.md` | Java/Spring Boot 后端开发 |
| **troubleshooting** | `troubleshooting/SKILL.md` | 错误诊断、Bug 修复 |
| **ui-design** | `ui-design/SKILL.md` | UI/UX 设计、布局、样式 |
| **testing** | `testing/SKILL.md` | 单元测试、验收测试 |
| **documentation** | `documentation/SKILL.md` | 文档编写、注释同步 |
| **security** | `security/SKILL.md` | 安全审查、XSS/SQL 注入防护 |
| **academic-assistant** | `academic-assistant/SKILL.md` | 毕设开题、论文、翻译、文献 |

## SKILL.md 规范

每个技能必须以 YAML frontmatter 开头：

```yaml
---
name: skill-name
description: >
  简洁描述技能功能和触发场景，帮助 Claude 判断何时使用该技能。
---

# 技能标题

[详细指令内容]
```

## 使用方式

技能由 Agent 自动扫描和加载，也可手动触发：

```
请使用 code-review 技能审查 UserController
请使用 troubleshooting 技能诊断登录失败问题
```

## 参考

- [Claude Skills 官方仓库](https://github.com/anthropics/skills)
- [Agent Skills 规范](https://github.com/anthropics/skills/tree/main/spec)
