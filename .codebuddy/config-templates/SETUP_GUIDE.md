# CodeBuddy 配置快速恢复指南

本目录仅包含 **MCP 服务器配置模板**（需要填写敏感信息，不能直接使用）。

项目级规则已放在 `.codebuddy/rules/` 目录下，Clone 项目后自动生效。

## 目录结构说明

```
.codebuddy/
├── rules/                      # 【自动生效】项目级规则
│   ├── project-docs-sync.mdc   # 强制文档同步规范
│   ├── java开发指南.mdc        # Java 开发规范
│   └── anydev/rules/anydev.mdc # AnyDev 部署规则
├── plans/                      # AI 生成的开发计划（自动生成）
└── config-templates/           # 【需手动配置】MCP 模板
    ├── SETUP_GUIDE.md          # 本文件
    └── mcp-config-template.json# MCP 配置模板
```

## 配置 MCP 服务器

**目标路径**: 
- Windows: `C:\Users\<用户名>\.codebuddy\mcp.json`
- Mac/Linux: `~/.codebuddy/mcp.json`

**操作步骤**:
1. 复制 `mcp-config-template.json` 到目标路径
2. 重命名为 `mcp.json`
3. 替换占位符：

| 占位符 | 替换为 |
|-------|-------|
| `your_mysql_user` | MySQL 用户名（如 `root`） |
| `your_mysql_password` | MySQL 密码 |
| `your_github_token_here` | GitHub Personal Access Token |

**获取 GitHub Token**:
1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token (classic)"
3. 勾选 `repo` 权限
4. 生成并复制 Token

## 安装 MCP 依赖

```bash
# Node.js (用于 GitHub MCP) - 需要 v18+
node --version

# Python uv (用于 MySQL MCP)
pip install uv
```

## 安全提醒

⚠️ `mcp.json` 包含敏感信息，**绝对不要**提交到仓库！

## 项目仓库

- **GitHub**: https://github.com/ngioigli/study_room
- **Gitee**: https://gitee.com/abcdxiaocheng/cloud-based-self-study-room
