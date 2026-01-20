# CodeBuddy 配置快速恢复指南

本目录包含 CodeBuddy 的 MCP 服务器配置和用户规则模板，方便在新环境中快速恢复开发配置。

## 目录结构

```
.codebuddy/
├── plans/                      # AI 生成的开发计划（自动生成）
├── rules/                      # 项目级规则（跟随项目）
│   └── anydev/                 # AnyDev 云研发规则
└── config-templates/           # 用户级配置模板（需手动复制）
    ├── SETUP_GUIDE.md          # 本文件 - 配置指南
    ├── mcp-config-template.json# MCP 服务器配置模板
    └── rules/                  # 用户规则模板
        ├── java开发指南.mdc
        └── 代码修改规范.mdc
```

## 配置类型说明

| 类型 | 存储位置 | 是否跟随项目 | 说明 |
|-----|---------|-------------|------|
| **项目级规则** | `项目/.codebuddy/rules/` | ✅ 是 | Clone 后自动生效 |
| **用户级规则** | `用户目录/.codebuddy/rules/` | ❌ 否 | 需手动复制 |
| **MCP 配置** | `用户目录/.codebuddy/mcp.json` | ❌ 否 | 需手动复制并填写 Token |

## 快速配置步骤

### 1. 配置 MCP 服务器

**目标路径**: 
- Windows: `C:\Users\<用户名>\.codebuddy\mcp.json`
- Mac/Linux: `~/.codebuddy/mcp.json`

**操作步骤**:
1. 复制 `config-templates/mcp-config-template.json` 到目标路径
2. 重命名为 `mcp.json`
3. 编辑文件，替换以下占位符：

| 占位符 | 替换为 |
|-------|-------|
| `your_mysql_user` | 你的 MySQL 用户名（如 `root`） |
| `your_mysql_password` | 你的 MySQL 密码 |
| `your_github_token_here` | 你的 GitHub Personal Access Token |

**获取 GitHub Token**:
1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token (classic)"
3. 勾选 `repo` 权限（如需操作 Issue/PR，还需勾选相应权限）
4. 生成并复制 Token

### 2. 配置用户规则（可选）

**目标路径**: 
- Windows: `C:\Users\<用户名>\.codebuddy\rules\`
- Mac/Linux: `~/.codebuddy/rules/`

**操作步骤**:
1. 将 `config-templates/rules/` 目录下的所有 `.mdc` 文件复制到目标路径
2. 重启 CodeBuddy 使规则生效

### 3. 安装 MCP 依赖

```bash
# Node.js (用于 GitHub MCP) - 需要 v18+
node --version

# Python uv (用于 MySQL MCP)
pip install uv
```

### 4. 验证配置

重启 CodeBuddy 后，验证方式：

1. **验证 MySQL MCP**: 让 AI 执行 `SELECT * FROM seats;`
2. **验证 GitHub MCP**: 让 AI 在仓库创建一个测试 Issue

## 注意事项

⚠️ **安全提醒**:
- `mcp.json` 包含敏感信息（数据库密码、GitHub Token）
- **绝对不要**将填写了真实 Token 的 `mcp.json` 提交到仓库
- 定期更换 GitHub Token
- 使用最小权限原则配置 Token

## 项目仓库

- **GitHub**: https://github.com/ngioigli/study_room
- **Gitee**: https://gitee.com/abcdxiaocheng/cloud-based-self-study-room
