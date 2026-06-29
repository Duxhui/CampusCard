@echo off
title 校园一卡通启动器

:: 启动后端（在新的 cmd 窗口中运行）
echo 正在启动后端...
start "Backend" cmd /k "cd /d D:\Projects\Java\CampusCard\backend\campus-card-backend && mvn spring-boot:run"

:: 等待几秒让后端先初始化（可选，防止前端启动太快报错）
timeout /t 5 /nobreak >nul

:: 启动前端（在新的 cmd 窗口中运行）
echo 正在启动前端...
start "Frontend" cmd /k "cd /d D:\Projects\Java\CampusCard\frontend\campus-card-frontend && npm run dev"

echo 启动完成！请稍候，待两个窗口均显示就绪后，访问 http://localhost:5173
pause