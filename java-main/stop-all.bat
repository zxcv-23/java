@echo off
setlocal EnableExtensions
title 一键停止前后端
REM ============================================================
REM  停止后端 (8081) 和前端 (5173): 按端口找 PID 并结束进程
REM ============================================================
call :KILLPORT 8081 后端
call :KILLPORT 5173 前端
echo.
echo 完成。
pause
goto :eof

:KILLPORT
set "PORT=%1"
set "NAME=%2"
set "FOUND="
for /f "tokens=5" %%A in ('netstat -ano ^| findstr ":%PORT% " ^| findstr LISTENING') do (
    echo [停止] 结束%NAME%进程, 端口 %PORT%, PID %%A
    taskkill /F /PID %%A >nul 2>&1
    set "FOUND=1"
)
if not defined FOUND echo [提示] %NAME% (端口 %PORT%) 没有在运行
goto :eof
