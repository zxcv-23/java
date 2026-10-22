@echo off
setlocal EnableExtensions
title guiniang-huajia-vue 前端 (端口 5173)

REM ============================================================
REM  guiniang-huajia-vue 前端一键启动脚本 (GBK 编码, 双击运行)
REM  作用:
REM    1. node_modules 不存在时自动 npm install (失败自动换国内镜像重试)
REM    2. 启动 Vite 开发服务器 (热更新)
REM  注意: 需要先启动后端 (springBootTest\start-backend.bat)
REM        每次开机 node_modules 会被删除, 脚本会自动重装。
REM ============================================================

cd /d "%~dp0"

where npm >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 npm, 请先安装 Node.js, 建议 18 以上
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo [依赖] node_modules 不存在, 正在安装 — 仅首次需要联网
    call npm install
    if errorlevel 1 (
        echo [依赖] 默认源安装失败, 改用国内镜像 npmmirror 重试 ...
        call npm install --registry=https://registry.npmmirror.com
        if errorlevel 1 goto :NPMFAIL
    )
    echo [依赖] 安装完成。
)

echo [启动] 前端开发服务器: http://127.0.0.1:5173  (关闭本窗口或按 Ctrl+C 即停止)
call npm run dev
echo [退出] 前端已停止。
pause
exit /b 0

:NPMFAIL
echo [错误] npm install 失败, 请检查网络或 Node.js 安装。
pause
exit /b 1
