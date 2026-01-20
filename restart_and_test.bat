@echo off
echo 正在重启服务...

REM 停止现有的Java进程
taskkill /f /im java.exe 2>nul

REM 等待2秒
timeout /t 2 >nul

echo 启动新的服务...
REM 启动新的服务
call run.bat

echo 服务重启完成！
echo.
echo 请在浏览器中：
echo 1. 按 Ctrl+F5 强制刷新页面
echo 2. 或者按 F12 打开开发者工具，右键刷新按钮选择"清空缓存并硬性重新加载"
echo.
echo 主要功能测试：
echo - 专注页面：点击右上角⚙️设置按钮查看新功能
echo - 宠物页面：查看聊天气泡样式和经验值更新
echo - 情绪碎纸机：体验更大的纸张区域和音效
pause