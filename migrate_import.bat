@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ============================================================
echo    云端自习室 - 数据库导入工具 (Windows)
echo    清空并重建数据库，导入完整数据
echo ============================================================
echo.

:: 配置参数（根据实际情况修改）
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "MYSQL_DB=study_room"
set "BACKUP_FILE=study_room_full_backup.sql"

:: 检查备份文件是否存在
if not exist "%BACKUP_FILE%" (
    echo [ERROR] 找不到备份文件: %BACKUP_FILE%
    echo    请确保以下文件之一存在于项目根目录:
    echo    1. %BACKUP_FILE% （推荐）
    echo    2. 或运行 migrate_export.bat 生成备份文件
    echo.
    pause
    exit /b 1
)

echo [1/5] 检测到备份文件: %BACKUP_FILE%
for %%I in ("%BACKUP_FILE%") do echo      文件大小: %%~zI 字节
echo.

echo [2/5] 请输入 MySQL root 密码:
set /p MYSQL_PWD=

echo.
echo [WARNING] 此操作将清空现有数据库 '%MYSQL_DB%' 的所有数据！
echo.
set /p CONFIRM=确认继续? (输入 yes 确认): 

if /i not "%CONFIRM%"=="yes" (
    echo 操作已取消。
    pause
    exit /b 0
)

echo.
echo [3/5] 正在删除旧数据库（如存在）...

:: 删除旧数据库
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PWD% -e "DROP DATABASE IF EXISTS %MYSQL_DB%;" 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] 删除旧数据库时出现警告，继续执行...
)

echo [4/5] 正在导入数据库...
echo    这可能需要几分钟，请耐心等待...
echo.

:: 导入备份文件
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PWD% < "%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] 数据库导入成功！
    echo.
    
    echo [5/5] 验证导入结果...
    echo.
    
    :: 验证表数量
    echo --- 数据库表列表 ---
    mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PWD% -e "USE %MYSQL_DB%; SHOW TABLES;"
    
    echo.
    echo --- 数据统计 ---
    mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PWD% -e "USE %MYSQL_DB%; SELECT 'users' AS 表名, COUNT(*) AS 记录数 FROM users UNION ALL SELECT 'seats', COUNT(*) FROM seats UNION ALL SELECT 'orders', COUNT(*) FROM orders UNION ALL SELECT 'focus_records', COUNT(*) FROM focus_records UNION ALL SELECT 'learning_stats', COUNT(*) FROM learning_stats UNION ALL SELECT 'user_pets', COUNT(*) FROM user_pets UNION ALL SELECT 'message_board', COUNT(*) FROM message_board UNION ALL SELECT 'encouragement_cards', COUNT(*) FROM encouragement_cards;"
    
    echo.
    echo ============================================================
    echo    导入完成！下一步操作:
    echo    1. 编辑 src/main/resources/application.properties
    echo    2. 修改 spring.datasource.password 为当前电脑的MySQL密码
    echo    3. 运行 run.bat 启动项目
    echo    4. 访问 http://localhost:9090/login.html
    echo ============================================================
) else (
    echo.
    echo [ERROR] 数据库导入失败！
    echo    请检查:
    echo    1. MySQL 服务是否启动
    echo    2. 密码是否正确
    echo    3. 备份文件是否完整
)

echo.
pause
