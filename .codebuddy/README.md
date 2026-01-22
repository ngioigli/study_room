# .codebuddy 项目配置目录

本目录包含 CodeBuddy AI 助手的项目级配置，包括 Agent、Skills、Rules 等。

## 目录结构

```
.codebuddy/
├── agents/                    # 智能体定义
│   └── 超级团队模式.md        # 超级团队 Agent
├── skills/                    # 技能定义
│   ├── README.md
│   ├── troubleshooting/       # 问题诊断技能
│   ├── frontend-dev/          # 前端开发技能
│   ├── backend-dev/           # 后端开发技能
│   ├── ui-design/             # UI 设计技能
│   ├── database/              # 数据库技能
│   ├── testing/               # 测试技能
│   ├── documentation/         # 文档技能
│   ├── security/              # 安全技能
│   └── code-review/           # 代码审查技能
├── rules/                     # 规则定义（始终应用或按需应用）
│   ├── assistant-identity.mdc # AI 身份与行为规范
│   ├── project-facts.mdc      # 项目事实快照
│   ├── project-docs-sync.mdc  # 文档同步规范
│   ├── git-commit-rules.mdc   # Git 提交规范
│   ├── java开发指南.mdc       # Java 开发规范
│   ├── _Java单测生成-Junit5.mdc
│   ├── java_单元测试AI生成实践指南.mdc
│   └── anydev/                # AnyDev 集成规则
├── plans/                     # 任务计划存档
└── config-templates/          # 配置模板
    ├── mcp-config-template.json  # MCP 配置模板
    └── SETUP_GUIDE.md            # MCP 配置指南
```

## 使用说明

### 启动超级团队模式

说以下任意口令激活超级团队 Agent：

```
进入超级团队模式
启动超级团队模式
超级团队
```

激活后，AI 会自动根据问题类型调用对应的技能来解决问题。

### Rules vs Skills 的区别

| 类型 | 用途 | 特点 |
|------|------|------|
| **Rules** | 始终遵守的规范/约束 | 被动应用，定义"必须做什么" |
| **Skills** | 可调用的专业能力 | 主动调用，定义"如何做" |

### 主要规则

| 规则文件 | 说明 | 应用方式 |
|---------|------|---------|
| `assistant-identity.mdc` | AI 身份与行为规范 | 始终应用 |
| `project-facts.mdc` | 项目技术栈/API/表结构 | 始终应用 |
| `project-docs-sync.mdc` | 文档同步要求 | 始终应用 |
| `git-commit-rules.mdc` | Git 提交规范 | 始终应用 |

### 主要技能

| 技能 | 触发关键词 |
|------|-----------|
| `troubleshooting` | 报错、bug、不工作 |
| `frontend-dev` | 页面、HTML、CSS、JS |
| `backend-dev` | 接口、API、Controller |
| `ui-design` | 界面、设计、布局 |
| `database` | 表、字段、SQL |
| `testing` | 测试、单测、验证 |
| `documentation` | 文档、README |
| `security` | 安全、XSS、权限 |
| `code-review` | 审查、优化 |

## 注意事项

1. **不要删除此目录**：`.codebuddy` 存储项目配置数据
2. **config-templates 是模板**：实际的 MCP 配置在本地私有位置，不提交仓库
3. **plans 是存档**：记录历史任务计划，可以清理过旧的
