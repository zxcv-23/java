@echo off
setlocal EnableExtensions
title springBootTest 后端 (端口 8081)

REM ============================================================
REM  springBootTest 后端一键启动脚本 (GBK 编码, 双击运行)
REM  作用:
REM    1. 清除 SERVER__PORT / SERVER__HOST 环境变量 (防止覆盖 server.port)
REM    2. 结束占用 8081 端口的旧进程
REM    3. dd 库不存在时自动建库 + 建表 (幂等, 绝不 DROP 库/表, 不清空注册用户)
REM       (库已存在则检测 user 表缺列并幂等补齐)
REM    4. 每次都用 Maven 重新打包 (跳过测试), 避免启动旧 jar
REM    5. 在 8081 端口启动后端
REM  说明: 全部使用相对路径, 项目目录整体移动后依然可用。
REM  注意: 数据库名必须与 application-dev.properties 的 spring.datasource.url 一致。
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
"%MYSQL%" -uroot -p123456 -e "USE dd;" >nul 2>&1
if errorlevel 1 goto :INITDB

REM 库已存在: 检查 user 表字段是否与 entity/User.java 一致
REM 缺列时自动执行 sql\fix_user_table.sql 补齐(幂等, 不删除任何数据)
set "COLCNT="
for /f %%A in ('"%MYSQL%" -uroot -p123456 -N -e "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user' AND COLUMN_NAME IN ('email','phone','gender','bio','token_version');" 2^>nul') do set "COLCNT=%%A"
if not defined COLCNT goto :DBOK
if "%COLCNT%"=="5" goto :DBOK

echo [数据库] user 表缺少字段, 当前 %COLCNT%/5, 正在自动补齐 ...
if exist "sql\fix_user_table.sql" (
    "%MYSQL%" -uroot -p123456 < "sql\fix_user_table.sql" >nul 2>&1
    echo [数据库] 字段已补齐, 原有数据未改动
) else (
    echo [警告] 未找到 sql\fix_user_table.sql, 登录可能继续报错
)
goto :CHECKJAR

:INITDB
echo [数据库] 正在创建 dd 库并初始化表结构 (幂等, 不会删除任何已有数据) ...
"%MYSQL%" -uroot -p123456 < "sql\init.sql" >nul 2>&1
if errorlevel 1 goto :DBFAIL
"%MYSQL%" -uroot -p123456 -D dd < "src\main\resources\schema.sql" >nul 2>&1
"%MYSQL%" -uroot -p123456 -D dd < "src\main\resources\data.sql" >nul 2>&1
echo [数据库] dd 库初始化完成
goto :CHECKJAR

:DBOK
echo [数据库] dd 库已存在且结构正确, 跳过初始化

REM --- 5. 每次都重新打包, 避免启动旧 jar (旧 jar 里可能还是旧库名/旧代码)
:CHECKJAR
echo [构建] 重新打包 (确保使用最新代码与配置) ...
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
call "%MVN%" -DskipTests package
if errorlevel 1 goto :BUILDFAIL
if not exist "target\springBootTest-0.0.1-SNAPSHOT.jar" goto :BUILDFAIL

:RUN
echo [启动] 后端服务: http://192.168.119.42:8081  (关闭本窗口或按 Ctrl+C 即停止)
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
