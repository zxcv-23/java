@echo off
setlocal EnableExtensions
title 停止前端 (5173)
cd /d "%~dp0"
set "FOUND="
for /f "tokens=5" %%A in ('netstat -ano ^| findstr ":5173 " ^| findstr LISTENING') do (
    echo [停止] 结束占用 5173 端口的进程 PID %%A
    taskkill /F /PID %%A >nul 2>&1
    set "FOUND=1"
)
if not defined FOUND echo [提示] 5173 端口上没有正在运行的前端进程
echo 完成。
pause
