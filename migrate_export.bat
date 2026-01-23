@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ============================================================
echo    云端自习室 - 数据库导出工具 (Windows)
echo    导出完整数据库结构和数据
echo ============================================================
echo.

:: 配置参数（根据实际情况修改）
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "MYSQL_DB=study_room"

:: 获取当前日期时间作为文件名后缀
for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set "DATE_STR=%%a%%b%%c"
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set "TIME_STR=%%a%%b"
set "BACKUP_FILE=study_room_backup_%DATE_STR%_%TIME_STR%.sql"
set "FULL_BACKUP_FILE=study_room_full_backup.sql"

echo [1/4] 请输入 MySQL root 密码:
set /p MYSQL_PWD=

echo.
echo [2/4] 正在导出数据库结构和数据...
echo.

:: 创建备份目录
if not exist "backup" mkdir backup

:: 导出完整数据库（结构+数据+触发器+存储过程）
mysqldump -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PWD% --databases %MYSQL_DB% --routines --triggers --events --single-transaction --quick --lock-tables=false > "backup\%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] 数据库导出成功！
    echo.
    
    :: 复制一份为固定名称文件，方便导入脚本使用
    copy "backup\%BACKUP_FILE%" "%FULL_BACKUP_FILE%" >nul
    
    echo [3/4] 导出文件信息:
    echo    - 带时间戳备份: backup\%BACKUP_FILE%
    echo    - 迁移用文件:   %FULL_BACKUP_FILE%
    echo.
    
    :: 显示文件大小
    for %%I in ("backup\%BACKUP_FILE%") do echo    - 文件大小: %%~zI 字节
    echo.
    
    echo [4/4] 导出完成！
    echo.
    echo ============================================================
    echo    下一步操作:
    echo    1. 将 %FULL_BACKUP_FILE% 复制到目标电脑
    echo    2. 在目标电脑运行 migrate_import.bat
    echo ============================================================
) else (
    echo [ERROR] 数据库导出失败！
    echo    请检查:
    echo    1. MySQL 服务是否启动
    echo    2. 密码是否正确
    echo    3. 数据库 %MYSQL_DB% 是否存在
)

echo.
pause
