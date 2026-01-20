---
name: qr-code-migration-config
overview: 将自习室项目迁移到新电脑：配置 MySQL MCP 服务、修改 application.properties、创建数据库和表。
todos:
  - id: explore-project
    content: 使用 [subagent:code-explorer] 探索项目结构，定位配置文件位置
    status: completed
  - id: config-mcp
    content: 配置 MySQL MCP 服务到 mcp.json 文件
    status: completed
    dependencies:
      - explore-project
  - id: update-properties
    content: 修改 application.properties 数据库连接和上传路径配置
    status: completed
    dependencies:
      - explore-project
  - id: create-database
    content: 创建 study_room 数据库
    status: completed
    dependencies:
      - config-mcp
  - id: create-tables
    content: 根据项目需求创建数据库表结构
    status: completed
    dependencies:
      - create-database
  - id: create-upload-dir
    content: 创建图片上传目录 C:/qr_app/uploads/images
    status: completed
---

## Product Overview

将自习室项目从原服务器迁移到新电脑的配置任务，主要涉及数据库环境配置和应用程序连接设置。

## Core Features

- 配置 MySQL MCP 服务到 mcp.json，实现数据库管理能力
- 修改 application.properties 配置文件，更新数据库连接信息和图片上传路径
- 创建 study_room 数据库
- 创建项目所需的数据表结构

## Tech Stack

- 数据库：MySQL 8.x
- 后端框架：Spring Boot (Java)
- 配置管理：application.properties
- MCP 服务：MySQL MCP

## Tech Architecture

### System Architecture

本次任务为配置迁移，不涉及架构变更，仅需调整配置文件以适配新环境。

### 配置变更点

- **MySQL MCP 服务**：在 mcp.json 中添加 MySQL 连接配置
- **数据库连接**：更新 application.properties 中的数据库 URL、用户名、密码
- **文件上传路径**：将图片上传路径改为 `C:/qr_app/uploads/images`

### 数据流

应用启动 → 读取 application.properties → 连接 MySQL 数据库 → 服务就绪

## Implementation Details

### 核心配置文件

```
project-root/
├── src/main/resources/
│   └── application.properties  # 修改：数据库连接和上传路径配置
└── mcp.json                    # 新增/修改：MySQL MCP 服务配置
```

### 关键配置内容

**application.properties 数据库配置**：

```
spring.datasource.url=jdbc:mysql://localhost:3306/study_room?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=12345678a
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 图片上传路径
file.upload.path=C:/qr_app/uploads/images
```

**mcp.json MySQL 服务配置**：

```
{
  "mcpServers": {
    "mysql": {
      "command": "npx",
      "args": ["-y", "@benborber/mcp-server-mysql"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "12345678a",
        "MYSQL_DATABASE": "study_room"
      }
    }
  }
}
```

### 数据库表结构

需要根据项目原有表结构创建相应的数据表，包括用户表、预约表、座位表等核心业务表。

## Agent Extensions

### SubAgent

- **code-explorer**
- Purpose: 探索项目目录结构，查找 application.properties 配置文件位置和现有 mcp.json 配置
- Expected outcome: 确定配置文件的准确路径，了解现有配置内容以便正确修改