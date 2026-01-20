@echo off
chcp 65001 >nul
echo ========================================
echo  微信自动化脚本 - 依赖安装
echo ========================================
echo.

cd /d "%~dp0"

:: 检查虚拟环境是否存在
if not exist ".venv\Scripts\python.exe" (
    echo [1/3] 创建虚拟环境...
    uv venv .venv --seed
    if errorlevel 1 (
        echo 错误：创建虚拟环境失败
        pause
        exit /b 1
    )
) else (
    echo [1/3] 虚拟环境已存在，跳过创建
)

echo.
echo [2/3] 检查 Python 环境...
".venv\Scripts\python.exe" --version
if errorlevel 1 (
    echo 错误：虚拟环境损坏，请删除 .venv 目录后重试
    pause
    exit /b 1
)

echo.
echo [3/3] 安装依赖库...
echo - uiautomation (UI自动化)
echo - pyperclip (剪贴板)
echo - pywin32 (Windows API)
echo - pyautogui (鼠标键盘控制)
echo - requests (HTTP请求)
echo - qrcode (二维码生成)
echo - Pillow (图像处理)
echo.

".venv\Scripts\pip" install uiautomation pyperclip pywin32 pyautogui requests qrcode Pillow -i https://pypi.tuna.tsinghua.edu.cn/simple

echo.
echo ========================================
echo  安装完成！
echo ========================================
pause
