@echo off
REM ============================================================
REM Nexora 数据库一键迁移脚本（给团队成员使用）
REM 用法:   migrate.bat [mysql用户名] [mysql密码]
REM 示例:   migrate.bat root 123456
REM 流程:   清除旧数据库重建 → 导入 nexora_base.sql
REM         （nexora_base.sql 为从开发者机器导出的最新库结构，
REM           35 张表全量、业务数据已清除，结构即唯一基线）
REM 前置:   MySQL 已启动，mysql 命令在 PATH 中
REM ============================================================
setlocal

set MYSQL=mysql
set DB_USER=%1
set DB_PASS=%2
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PASS%"=="" set DB_PASS=123456

set DIR=%~dp0
set BASE=%~dp0nexora_base.sql
set DB=nexora

echo ============================================
echo  Nexora 数据库迁移（全量重建）
echo  数据库: %DB%   用户: %DB_USER%
echo ============================================

echo.
echo [1/2] 清除旧数据库并重建 %DB% ...
"%MYSQL%" -u%DB_USER% -p%DB_PASS% -e "DROP DATABASE IF EXISTS %DB%; CREATE DATABASE %DB% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if errorlevel 1 goto :fail

echo [2/2] 导入最新库结构（nexora_base.sql，业务数据已清除）...
REM Windows 下用 cmd 执行输入重定向，避免中文编码问题
cmd /c ""%MYSQL%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB% < "%BASE%""
if errorlevel 1 goto :fail

echo.
echo [OK] 数据库 %DB% 迁移完成（35 张表，含三层目录、学习档案与邮箱账号体系）！
echo 环境变量与启动步骤见 docs\联调验收.md
pause
exit /b 0

:fail
echo.
echo [ERROR] 迁移失败！
echo   1) MySQL 是否已启动？（mysql 命令可用: %MYSQL%）
echo   2) 账号密码是否正确？（默认 root/123456，可传参覆盖）
echo   3) 是否能访问 %BASE%
pause
exit /b 1