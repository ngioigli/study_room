---
name: delete-vue-keep-html
overview: 删除所有 Vue/前端项目目录，只保留 src/main/resources/static 下的 HTML 版本，并更新 md 文档。
todos:
  - id: explore-structure
    content: 使用 [subagent:code-explorer] 探索项目结构，识别所有 Vue 相关目录和 md 文档
    status: completed
  - id: delete-vue-dirs
    content: 删除 vue-frontend 及其他 Vue/前端相关目录
    status: completed
    dependencies:
      - explore-structure
  - id: verify-html-static
    content: 确认 src/main/resources/static 下的 HTML 版本完整保留
    status: completed
    dependencies:
      - delete-vue-dirs
  - id: update-md-docs
    content: 更新 src/md 目录下的文档，移除 Vue 相关内容，更新为 HTML 版本说明
    status: completed
    dependencies:
      - verify-html-static
---

## 产品概述

清理项目中的 Vue/前端相关目录，仅保留纯 HTML 版本的静态资源，并同步更新项目文档以反映此变更。

## 核心功能

- 删除 vue-frontend 目录及其他 Vue 相关前端目录
- 保留 src/main/resources/static 目录下的 HTML 静态版本
- 更新 src/md 目录下的文档，移除 Vue 相关说明，更新为 HTML 版本的描述

## Agent Extensions

### SubAgent

- **code-explorer**
- 用途：探索项目目录结构，识别所有需要删除的 Vue/前端相关目录，以及需要更新的文档文件
- 预期结果：获取完整的目录结构信息，明确需要删除的目录列表和需要修改的文档文件列表