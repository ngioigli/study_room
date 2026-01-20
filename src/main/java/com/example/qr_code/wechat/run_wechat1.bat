@echo off
chcp 65001 >nul
echo ========================================
echo  微信UI自动化脚本 - wechat1.py
echo  功能：自动搜索公众号并点击菜单
echo ========================================
echo.

cd /d "%~dp0"

:: 检查虚拟环境
if not exist ".venv\Scripts\python.exe" (
    echo 错误：未找到虚拟环境，请先运行 install_deps.bat
    pause
    exit /b 1
)

echo 启动脚本...
echo 请确保微信已登录并在后台运行
echo.

".venv\Scripts\python.exe" wechat1.py

echo.
echo ========================================
echo  脚本执行完毕
echo ========================================
pause
