@echo off
REM ============================================================
REM Nexora 数据库一键迁移脚本（给团队成员使用）
REM 用法:   migrate.bat [mysql用户名] [mysql密码] [模式]
REM   模式:  full     = 全新安装【默认】：先清除目标数据库，再导入
REM                     nexora_base.sql（从开发者电脑提取的最新库结构，业务数据已清除）
REM          upgrade  = 仅执行增量脚本（针对旧库升级，保留数据）
REM 示例:   migrate.bat root 123456 full
REM         migrate.bat root 123456 upgrade
REM 前置:   MySQL 已启动，mysql 命令在 PATH 中
REM ============================================================
setlocal

set MYSQL=mysql
set DB_USER=%1
set DB_PASS=%2
set MODE=%3
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PASS%"=="" set DB_PASS=123456
if "%MODE%"=="" set MODE=full

set DIR=%~dp0
set BASE=%~dp0nexora_base.sql
set DB=nexora

echo ============================================
echo  Nexora 数据库迁移  模式: %MODE%
echo  数据库: %DB%   用户: %DB_USER%
echo ============================================

if /i "%MODE%"=="full" (
    echo.
    echo [1/3] 清除旧数据库并重建 %DB% ...
    "%MYSQL%" -u%DB_USER% -p%DB_PASS% -e "DROP DATABASE IF EXISTS %DB%; CREATE DATABASE %DB% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    if errorlevel 1 goto :fail

    echo [2/3] 导入最新库结构（nexora_base.sql，业务数据已清除）...
    REM Windows 下用 cmd 执行输入重定向，避免中文编码问题
    cmd /c ""%MYSQL%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB% < "%BASE%""
    if errorlevel 1 goto :fail

    echo [3/3] 完成。全新结构已就绪（35 张表，含三层目录字段与邮箱账号体系）。
) else (
    echo.
    echo [1/2] 增量1: 学生个人知识库三层化（如早已执行过，出现 Duplicate column/Table 属预期，可忽略）...
    cmd /c ""%MYSQL%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB% < "%DIR%20260822_student_wiki.sql""

    echo [2/2] 增量2: 账号体系以邮箱为唯一登录标识...
    cmd /c ""%MYSQL%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB% < "%DIR%20260822_v2_email_account.sql""
    if errorlevel 1 goto :fail
)

echo.
echo [OK] 数据库 %DB% 迁移完成！
echo 环境变量与启动步骤见 docs\联调验收.md
pause
exit /b 0

:fail
echo.
echo [ERROR] 迁移失败！
echo   1) MySQL 是否已启动？（mysql 命令可用: %MYSQL%）
echo   2) 账号密码是否正确？（默认 root/123456，可传参覆盖）
echo   3) full 模式需要能访问 %BASE%
pause
exit /b 1