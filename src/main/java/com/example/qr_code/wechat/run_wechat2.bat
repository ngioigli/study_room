@echo off
chcp 65001 >nul
echo ========================================
echo  Token请求脚本 - wechat2.py
echo  功能：直接获取校园码二维码
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
echo 请确保已在 wechat2.py 中配置有效的 Token 和 Cookie
echo.

".venv\Scripts\python.exe" wechat2.py

echo.
echo ========================================
echo  脚本执行完毕
echo ========================================
pause
