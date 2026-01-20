@echo off
chcp 65001 >nul
echo ========================================
echo    云端自习室 - 一键部署脚本
echo ========================================
echo.

REM 检查环境
echo [1/6] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java，请先安装 JDK 17+
    echo 下载地址: https://adoptium.net/zh-CN/
    pause
    exit /b 1
)

mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Maven，请先安装 Maven 3.9+
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

node -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js 18+
    echo 下载地址: https://nodejs.org/zh-cn
    pause
    exit /b 1
)

echo [√] 环境检查通过
echo.

REM 安装后端依赖
echo [2/6] 安装后端依赖...
call mvn dependency:resolve -q
if errorlevel 1 (
    echo [警告] Maven 依赖可能未完全安装，继续执行...
)
echo [√] 后端依赖处理完成
echo.

REM 检查 vue-frontend 目录
if not exist "vue-frontend" (
    echo [错误] 未找到 vue-frontend 目录
    echo 请确保项目结构完整
    pause
    exit /b 1
)

REM 安装前端依赖
echo [3/6] 安装前端依赖...
cd vue-frontend
call npm install
if errorlevel 1 (
    echo [错误] npm 依赖安装失败
    cd ..
    pause
    exit /b 1
)
cd ..
echo [√] 前端依赖安装完成
echo.

REM 构建前端
echo [4/6] 构建前端...
cd vue-frontend
call npm run build
if errorlevel 1 (
    echo [警告] 前端构建失败，可能需要手动处理
)
cd ..
echo [√] 前端构建处理完成
echo.

REM 数据库提示
echo [5/6] 数据库配置...
echo.
echo ========================================
echo    请手动完成以下数据库配置：
echo ========================================
echo.
echo 1. 确保 MySQL 服务已启动
echo.
echo 2. 创建数据库（如果不存在）：
echo    CREATE DATABASE study_room DEFAULT CHARACTER SET utf8mb4;
echo.
echo 3. 导入表结构：
echo    mysql -u root -p study_room ^< src/main/resources/sql/study_room_init.sql
echo.
echo 4. 修改数据库配置文件：
echo    src/main/resources/application.properties
echo    - spring.datasource.password=你的密码
echo.
echo ========================================
echo.

REM 完成
echo [6/6] 部署完成！
echo.
echo ========================================
echo    启动说明
echo ========================================
echo.
echo 启动后端：
echo   run.bat 或 mvn spring-boot:run
echo.
echo 启动前端（开发模式）：
echo   cd vue-frontend ^&^& npm run dev
echo.
echo 访问地址：
echo   - 原版 HTML: http://localhost:9090/index.html
echo   - Vue 3 版本: http://localhost:5173
echo.
echo 测试账号：admin / 123456
echo.
echo ========================================
pause
