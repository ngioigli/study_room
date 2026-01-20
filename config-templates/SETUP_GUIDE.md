# CodeBuddy 配置快速恢复指南

本目录包含 CodeBuddy 的 MCP 服务器配置和用户规则模板，方便在新环境中快速恢复开发配置。

## 目录结构

```
config-templates/
├── SETUP_GUIDE.md              # 本文件 - 配置指南
├── mcp-config-template.json    # MCP 服务器配置模板
└── rules/                      # 用户规则模板
    ├── java开发指南.mdc
    └── 代码修改规范.mdc
```

## 快速配置步骤

### 1. 配置 MCP 服务器

**目标路径**: `C:\Users\<你的用户名>\.codebuddy\mcp.json`

1. 复制 `mcp-config-template.json` 到目标路径并重命名为 `mcp.json`
2. 编辑文件，替换以下占位符：
   - `your_mysql_user` → 你的 MySQL 用户名（如 `root`）
   - `your_mysql_password` → 你的 MySQL 密码
   - `your_github_token_here` → 你的 GitHub Personal Access Token

**获取 GitHub Token**:
1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token (classic)"
3. 勾选 `repo` 权限
4. 生成并复制 Token

### 2. 配置用户规则

**目标路径**: `C:\Users\<你的用户名>\.codebuddy\rules\`

1. 将 `rules/` 目录下的所有 `.mdc` 文件复制到目标路径
2. 重启 CodeBuddy 使规则生效

### 3. 安装 MCP 依赖

确保已安装以下工具：

```bash
# Node.js (用于 GitHub MCP)
node --version  # 需要 v18+

# Python uv (用于 MySQL MCP)
pip install uv
```

### 4. 验证配置

重启 CodeBuddy 后，可以通过以下方式验证：

1. **验证 MySQL MCP**: 让 AI 执行 `SELECT * FROM seats;`
2. **验证 GitHub MCP**: 让 AI 在你的仓库创建一个测试 Issue

## 注意事项

⚠️ **安全提醒**:
- `mcp.json` 包含敏感信息（数据库密码、GitHub Token），请勿上传到公开仓库
- 定期更换 GitHub Token
- 使用最小权限原则配置 Token

## 项目仓库

- GitHub: https://github.com/ngioigli/study_room
- Gitee: https://gitee.com/abcdxiaocheng/cloud-based-self-study-room
