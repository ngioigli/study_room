# 📚 云端自习室 - 文档索引

> **更新日期**：2026-01-22  
> **说明**：项目文档已按功能分类整理到子文件夹

---

## 📁 目录结构

```
src/md/
├── README.md              # 本索引文件
├── core/                  # 核心规范与状态
│   ├── 00_AI_DEVELOPER_RULES.md      # AI 开发规范
│   ├── 03_CURRENT_STATUS_LOG.md      # 项目状态日志【最常更新】
│   ├── 05_AI_HANDOVER_PROTOCOL.md    # AI 交接协议
│   └── 13_ERROR_LEARNING_LOG.md      # 错误学习日志【强制必读】
│
├── design/                # 设计文档
│   ├── 01_PRODUCT_REQUIREMENTS_DOC.md   # 产品需求文档
│   ├── 02_SYSTEM_DESIGN_DOC.md          # 系统设计文档
│   ├── 09_GAMIFICATION_MODULE_DESIGN.md # 游戏化模块设计
│   └── 10_UI_DESIGN_SPECIFICATION.md    # UI 设计规范
│
├── guide/                 # 开发与部署指南
│   ├── 07_FINAL_TASK_BOOK.md            # 最终任务书
│   ├── 08_ULTIMATE_DEV_GUIDE.md         # 终极开发指南
│   ├── 11_QUICK_DEPLOY_GUIDE.md         # 快速部署指南
│   └── 12_AI_SUPER_TEAM_PROMPT_PACK.md  # 超级团队 Prompt 包
│
├── deploy/                # 迁移与部署
│   ├── 14_MIGRATION_DEPLOY_GUIDE.md     # 一键迁移部署指南【重点】
│   └── 15_DATABASE_CHANGELOG.md         # 数据库变更日志【强制维护】
│
└── academic/              # 学术相关
    └── 04_THESIS_OUTLINE.md             # 论文大纲
```

---

## 🔥 高频访问文档

| 场景 | 文档 | 路径 |
|------|------|------|
| **开发前必读** | 错误学习日志 | `core/13_ERROR_LEARNING_LOG.md` |
| **每次变更** | 项目状态日志 | `core/03_CURRENT_STATUS_LOG.md` |
| **数据库变更** | 数据库变更日志 | `deploy/15_DATABASE_CHANGELOG.md` |
| **新环境部署** | 迁移部署指南 | `deploy/14_MIGRATION_DEPLOY_GUIDE.md` |
| **功能开发** | 产品需求文档 | `design/01_PRODUCT_REQUIREMENTS_DOC.md` |
| **API/架构** | 系统设计文档 | `design/02_SYSTEM_DESIGN_DOC.md` |

---

## 📋 文档分类说明

### 📂 core/ - 核心规范
AI 和开发者必须遵循的规范和状态记录。

### 📂 design/ - 设计文档
产品需求、系统架构、UI/UX 设计相关文档。

### 📂 guide/ - 指南手册
开发指南、任务书、Prompt 包等操作性文档。

### 📂 deploy/ - 部署迁移
环境部署、数据迁移、数据库变更日志。

### 📂 academic/ - 学术相关
论文大纲、学术材料。

---

## ⚠️ AI 必读规则

1. **每次开发前**：阅读 `core/13_ERROR_LEARNING_LOG.md`
2. **每次修改后**：更新 `core/03_CURRENT_STATUS_LOG.md`
3. **数据库变更**：同步更新 `deploy/15_DATABASE_CHANGELOG.md`
4. **用户报错**：记录到 `core/13_ERROR_LEARNING_LOG.md`
