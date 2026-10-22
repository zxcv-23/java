@echo off
setlocal EnableExtensions
title springBootTest 后端 (端口 8081)

REM ============================================================
REM  springBootTest 后端一键启动脚本 (GBK 编码, 双击运行)
REM  作用:
REM    1. 清除 SERVER__PORT / SERVER__HOST 环境变量 (防止覆盖 server.port)
REM    2. 结束占用 8081 端口的旧进程
REM    3. guiniang 库不存在时自动执行 sql\init.sql 建库建表
REM       (并检测旧版表结构, 发现时提示重建)
REM    4. target 下没有 jar 时自动用 Maven 构建 (跳过测试)
REM    5. 在 8081 端口启动后端
REM  说明: 全部使用相对路径, 项目目录整体移动后依然可用。
REM  注意: 每次开机 target 文件夹会被删除, 脚本会自动重新构建。
REM ============================================================

cd /d "%~dp0"

REM --- 1. 清除环境变量覆盖 (Spring 宽松绑定会把 SERVER__PORT 当成 server.port)
set "SERVER__PORT="
set "SERVER__HOST="

REM --- 2. 结束占用 8081 的旧进程
for /f "tokens=5" %%A in ('netstat -ano ^| findstr ":8081 " ^| findstr LISTENING') do (
    echo [清理] 结束占用 8081 端口的旧进程 PID %%A
    taskkill /F /PID %%A >nul 2>&1
)

REM --- 3. 定位 Java (优先 Zulu 21)
set "JAVACMD=java"
if exist "C:\Program Files\Zulu\zulu-21\bin\java.exe" set "JAVACMD=C:\Program Files\Zulu\zulu-21\bin\java.exe"

REM --- 4. 数据库检查 / 初始化
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist "%MYSQL%" (
    echo [警告] 未找到 mysql.exe, 跳过数据库检查
    goto :CHECKJAR
)
"%MYSQL%" -uroot -p123456 -e "USE guiniang;" >nul 2>&1
if errorlevel 1 goto :INITDB

REM 库已存在, 检查是否为旧版表结构 (旧表含 email 列, 与当前代码不匹配, 注册会报 500)
set "OLDCNT="
for /f %%A in ('"%MYSQL%" -uroot -p123456 -N -e "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='guiniang' AND TABLE_NAME='user' AND COLUMN_NAME='email';" 2^>nul') do set "OLDCNT=%%A"
if not defined OLDCNT goto :DBOK
if "%OLDCNT%"=="0" goto :DBOK

echo [数据库] 检测到旧版表结构(含 email 列), 与当前代码不匹配, 注册会失败。
set "CONFIRM="
set /p "CONFIRM=是否删除并重建 guiniang 库? (将清空全部用户数据, 输入 Y 确认): "
if /i not "%CONFIRM%"=="Y" (
    echo [跳过] 未重建数据库, 注册接口可能报错。
    goto :CHECKJAR
)

:INITDB
echo [数据库] 正在执行 sql\init.sql 建库建表 ...
"%MYSQL%" -uroot -p123456 -e "DROP DATABASE IF EXISTS guiniang;" >nul 2>&1
"%MYSQL%" -uroot -p123456 < "sql\init.sql" >nul 2>&1
if errorlevel 1 goto :DBFAIL
echo [数据库] guiniang 库初始化完成
goto :CHECKJAR

:DBOK
echo [数据库] guiniang 库已存在且结构正确, 跳过初始化

REM --- 5. jar 不存在时自动构建
:CHECKJAR
if exist "target\springBootTest-0.0.1-SNAPSHOT.jar" goto :RUN

echo [构建] 未发现 jar, 开始 Maven 构建 (跳过测试) ...
set "MVN="
REM 优先使用本地缓存的 Maven 发行包
for /d %%D in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-*") do (
    for /d %%E in ("%%D\*") do (
        for /d %%F in ("%%E\apache-maven-*") do (
            if exist "%%F\bin\mvn.cmd" set "MVN=%%F\bin\mvn.cmd"
        )
    )
)
if not defined MVN (
    where mvn >nul 2>&1
    if not errorlevel 1 set "MVN=mvn"
)
if not defined MVN (
    if exist "mvnw.cmd" set "MVN=mvnw.cmd"
)
if not defined MVN (
    echo [错误] 未找到 Maven, 请安装 Maven 并加入 PATH。
    pause
    exit /b 1
)
echo [构建] 使用 Maven: %MVN%
call "%MVN%" -DskipTests clean package
if errorlevel 1 goto :BUILDFAIL
if not exist "target\springBootTest-0.0.1-SNAPSHOT.jar" goto :BUILDFAIL

:RUN
echo [启动] 后端服务: http://localhost:8081  (关闭本窗口或按 Ctrl+C 即停止)
"%JAVACMD%" -jar "target\springBootTest-0.0.1-SNAPSHOT.jar" --server.port=8081
echo [退出] 后端已停止。
pause
exit /b 0

:DBFAIL
echo [错误] 数据库初始化失败, 请确认 MySQL 已在本机 3306 端口运行。
pause
exit /b 1

:BUILDFAIL
echo [错误] 构建失败, 请查看上方日志。
pause
exit /b 1
