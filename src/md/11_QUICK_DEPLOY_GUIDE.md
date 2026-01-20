# 一键部署指南 (Quick Deploy Guide)

**更新日期**: 2026-01-13
**适用系统**: Windows 10/11
**前端技术**: 纯 HTML/CSS/JavaScript（无需 Node.js）

本文档帮助小白用户快速部署云端自习室项目。

---

## 目录

1. [环境准备](#1-环境准备)
2. [手动安装步骤](#2-手动安装步骤)
3. [启动项目](#3-启动项目)
4. [常见问题](#4-常见问题)

---

## 1. 环境准备

### 1.1 必需软件

| 软件 | 版本要求 | 下载地址 |
|------|---------|---------|
| JDK | 17+ | https://adoptium.net/zh-CN/ |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| MySQL | 8.x | https://dev.mysql.com/downloads/mysql/ |

**注意**：本项目前端使用纯 HTML，无需安装 Node.js。

### 1.2 检查环境

打开命令提示符（Win+R 输入 `cmd`），执行以下命令检查：

```bash
# 检查 Java
java -version
# 应显示：openjdk version "17.x.x" 或更高

# 检查 Maven
mvn -version
# 应显示：Apache Maven 3.9.x 或更高
```

---

## 2. 手动安装步骤

### 2.1 下载项目

```bash
# 如果使用 Git
git clone https://github.com/ngioigli/study_room.git
cd qr_code
```

### 2.2 配置数据库

1. 启动 MySQL 服务
2. 创建数据库：

```sql
CREATE DATABASE study_room DEFAULT CHARACTER SET utf8mb4;
```

3. 导入表结构：

```bash
mysql -u root -p study_room < src/main/resources/sql/study_room_init.sql
```

4. 修改数据库配置 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/study_room?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 2.3 安装后端依赖

```bash
mvn clean install -DskipTests
```

---

## 3. 启动项目

### 3.1 启动方式

**方式一：使用启动脚本**

```bash
# Windows
run.bat
```

**方式二：使用 Maven**

```bash
mvn spring-boot:run
```

### 3.2 访问地址

| 页面 | 地址 |
|------|------|
| 登录页 | http://localhost:9090/login.html |
| 自习室首页 | http://localhost:9090/index.html |
| 学习小屋 | http://localhost:9090/study.html |
| 专注模式 | http://localhost:9090/focus.html |
| 宠物详情 | http://localhost:9090/pet.html |
| 学习统计 | http://localhost:9090/stats.html |
| 个人中心 | http://localhost:9090/profile.html |

### 3.3 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| test | 123456 | 普通用户 |

---

## 4. 常见问题

### Q1: Maven 下载依赖很慢怎么办？

配置阿里云镜像。编辑 `~/.m2/settings.xml`：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

### Q2: 端口被占用怎么办？

修改 `application.properties`：

```properties
server.port=8080  # 改成其他端口
```

### Q3: 数据库连接失败？

1. 检查 MySQL 服务是否启动
2. 检查用户名密码是否正确
3. 检查数据库 `study_room` 是否存在
4. 检查防火墙是否阻止了 3306 端口

### Q4: 页面打开是空白？

1. 检查后端是否启动成功（控制台无报错）
2. 检查浏览器控制台是否有错误
3. 清除浏览器缓存后重试

---

## 附录：项目结构

```
qr_code/
├── src/
│   ├── main/
│   │   ├── java/com/example/qr_code/   # Spring Boot 后端
│   │   │   ├── controller/             # REST API 控制器
│   │   │   ├── service/                # 业务逻辑层
│   │   │   ├── entity/                 # 数据实体
│   │   │   └── mapper/                 # MyBatis Mapper
│   │   └── resources/
│   │       ├── static/                 # 前端静态资源
│   │       │   ├── *.html              # 页面文件
│   │       │   ├── css/                # 样式文件
│   │       │   ├── js/                 # JavaScript 文件
│   │       │   ├── audio/              # 白噪音音频
│   │       │   └── images/             # 图片资源
│   │       ├── sql/                    # 数据库脚本
│   │       └── application.properties  # 配置文件
│   └── md/                             # 项目文档
├── pom.xml                             # Maven 配置
└── run.bat                             # 启动脚本
```

---

*祝你部署顺利！如有问题，请查阅其他文档或联系开发者。*
