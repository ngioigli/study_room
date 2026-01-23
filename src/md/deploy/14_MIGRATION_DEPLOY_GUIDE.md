# 🚀 云端自习室 - Windows 一键迁移部署指南

> **版本**：v1.0.0  
> **更新日期**：2026-01-22  
> **适用环境**：Windows 10/11 + MySQL 8.x + JDK 8/11/17

---

## 📋 部署前检查清单

| 检查项 | 要求 | 验证命令 |
|--------|------|----------|
| JDK | 8/11/17 任一版本 | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| MySQL | 8.x | `mysql --version` |
| Git | 2.x+ | `git --version` |
| 端口 9090 | 未被占用 | `netstat -ano \| findstr 9090` |

---

## 🔧 第一步：环境安装

### 1.1 安装 JDK

1. 下载 JDK：https://adoptium.net/temurin/releases/
2. 选择 **Windows x64** + **JDK 17 LTS**（推荐）
3. 安装完成后配置环境变量：
   ```
   JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot
   Path 追加 = %JAVA_HOME%\bin
   ```
4. 验证：`java -version`

### 1.2 安装 Maven

1. 下载：https://maven.apache.org/download.cgi
2. 解压到 `C:\Program Files\Apache\maven`
3. 配置环境变量：
   ```
   M2_HOME = C:\Program Files\Apache\maven
   Path 追加 = %M2_HOME%\bin
   ```
4. 验证：`mvn -version`

### 1.3 安装 MySQL 8.x

1. 下载：https://dev.mysql.com/downloads/installer/
2. 选择 **MySQL Installer for Windows**
3. 安装时设置 root 密码（**记住密码，后续配置需要**）
4. 默认端口：3306
5. 验证：`mysql -u root -p`

---

## 📦 第二步：获取项目代码

### 方式一：Git 克隆（推荐）

```bash
# 从 GitHub 克隆
git clone https://github.com/ngioigli/study_room.git

# 或从 Gitee 克隆（国内更快）
git clone https://gitee.com/abcdxiaocheng/cloud-based-self-study-room.git
```

### 方式二：复制迁移包

将源电脑的整个项目文件夹复制到目标电脑。

---

## 🗄️ 第三步：数据库迁移

### 3.1 导出源数据库（在源电脑执行）

**方式一：运行导出脚本**
```bash
# 在项目根目录执行
migrate_export.bat
```

**方式二：手动导出**
```bash
# 导出数据库结构和数据
mysqldump -u root -p --databases study_room --routines --triggers > study_room_full_backup.sql
```

### 3.2 导入到目标数据库（在目标电脑执行）

**方式一：运行导入脚本**
```bash
# 在项目根目录执行
migrate_import.bat
```

**方式二：手动导入**
```bash
# 登录 MySQL
mysql -u root -p

# 执行导入
SOURCE C:/path/to/study_room_full_backup.sql;

# 或者命令行直接导入
mysql -u root -p < study_room_full_backup.sql
```

### 3.3 验证数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 切换数据库
USE study_room;

-- 检查表数量（应为12张）
SHOW TABLES;

-- 检查用户数据
SELECT COUNT(*) FROM users;

-- 检查学习统计数据
SELECT COUNT(*) FROM learning_stats;
```

---

## ⚙️ 第四步：配置项目

### 4.1 修改数据库密码

编辑 `src/main/resources/application.properties`：

```properties
# 修改为目标电脑的 MySQL 密码
spring.datasource.password=你的MySQL密码
```

### 4.2 修改上传路径（可选）

```properties
# 修改为实际的上传目录
file.upload-dir=C:/study_room/uploads/images
```

---

## 🚀 第五步：启动项目

### 方式一：使用启动脚本

```bash
# 在项目根目录双击运行
run.bat
```

### 方式二：Maven 命令

```bash
# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/qr_code-0.0.1-SNAPSHOT.jar
```

### 方式三：IDE 启动

1. 用 IntelliJ IDEA 打开项目
2. 找到 `QrCodeApplication.java`
3. 右键 → Run

---

## ✅ 第六步：验证部署

### 6.1 访问测试

| 页面 | 地址 | 预期 |
|------|------|------|
| 登录页 | http://localhost:9090/login.html | 显示登录表单 |
| 首页 | http://localhost:9090/index.html | 需登录后访问 |
| 管理后台 | http://localhost:9090/admin.html | 需管理员账号 |

### 6.2 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 测试用户 | test | 123456 |

### 6.3 功能验证清单

- [ ] 登录/退出正常
- [ ] 座位列表显示正常
- [ ] 专注计时功能正常
- [ ] 宠物页面加载正常
- [ ] 留言板显示正常
- [ ] 排行榜数据正常

---

## 🔄 第七步：配置自动启动（可选）

### Windows 服务方式

1. 下载 WinSW：https://github.com/winsw/winsw/releases
2. 创建 `study-room-service.xml` 配置
3. 安装服务：`winsw install`

### 计划任务方式

1. 打开任务计划程序
2. 创建基本任务
3. 触发器：系统启动时
4. 操作：启动程序 → `run.bat`

---

## 📁 项目目录结构

```
qr_code/
├── src/
│   ├── main/
│   │   ├── java/                    # Java 源码
│   │   └── resources/
│   │       ├── static/              # 前端静态资源
│   │       ├── application.properties  # 配置文件（需修改）
│   │       ├── schema.sql           # 数据库DDL
│   │       └── sql/
│   │           └── study_room_init.sql  # 完整初始化SQL
│   └── md/                          # 项目文档
├── uploads/                         # 上传文件目录
├── target/                          # 编译输出
├── pom.xml                          # Maven 配置
├── run.bat                          # 启动脚本
├── migrate_export.bat               # 数据导出脚本
└── migrate_import.bat               # 数据导入脚本
```

---

## ❓ 常见问题

### Q1: 端口 9090 被占用

```bash
# 查找占用进程
netstat -ano | findstr 9090

# 结束进程
taskkill /PID <进程ID> /F

# 或修改端口
# 编辑 application.properties: server.port=9091
```

### Q2: MySQL 连接失败

1. 检查 MySQL 服务是否启动
2. 检查密码是否正确
3. 检查 3306 端口是否开放

### Q3: Maven 编译失败

```bash
# 清理并重新下载依赖
mvn clean install -U
```

### Q4: 中文乱码

确保 MySQL 配置 `character_set_server=utf8mb4`

---

## 📞 技术支持

- GitHub Issues: https://github.com/ngioigli/study_room/issues
- Gitee Issues: https://gitee.com/abcdxiaocheng/cloud-based-self-study-room/issues

---

## 📝 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| v1.0.0 | 2026-01-22 | 初始版本，完整迁移文档 |

