@echo off
setlocal
title 一键启动前后端
REM ============================================================
REM  同时启动后端 (8081) 与前端 (5173), 各自独立窗口
REM  停止方式: 关闭两个窗口, 或运行 stop-all.bat
REM ============================================================
start "backend - 8081" cmd /k "%~dp0springBootTest\start-backend.bat"
timeout /t 3 /nobreak >nul
start "frontend - 5173" cmd /k "%~dp0guiniang-huajia-vue\start-frontend.bat"
echo.
echo 后端: http://localhost:8081
echo 前端: http://127.0.0.1:5173
echo.
echo 已打开两个窗口, 关闭窗口或运行 stop-all.bat 即可停止。
pause
