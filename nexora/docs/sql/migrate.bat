@echo off
REM ============================================================
REM Nexora database one-shot migration (for team members)
REM Usage:   migrate.bat [mysql_user] [mysql_password]
REM Example: migrate.bat root 123456
REM Flow:    drop and recreate the database -> import nexora.sql
REM          (full baseline) -> run 2026*.sql incremental scripts
REM          in filename order (they only add tables/columns and are
REM          re-runnable). Baseline + incrementals = latest schema.
REM NOTE:    keep this file ASCII-only. cmd.exe parses .bat with the
REM          system ANSI codepage (GBK on Chinese Windows); UTF-8
REM          Chinese bytes can swallow line breaks and corrupt parsing.
REM Requires: MySQL running, mysql client on PATH
REM ============================================================
setlocal

set MYSQL=mysql
set DB_USER=%1
set DB_PASS=%2
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PASS%"=="" set DB_PASS=123456

set DIR=%~dp0
set BASE=%~dp0nexora.sql
set DB=nexora

echo ============================================
echo  Nexora database migration (full rebuild)
echo  Database: %DB%   User: %DB_USER%
echo ============================================

echo.
echo [1/3] Drop and recreate database %DB% ...
"%MYSQL%" -u%DB_USER% -p%DB_PASS% -e "DROP DATABASE IF EXISTS %DB%; CREATE DATABASE %DB% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if errorlevel 1 goto :fail

echo [2/3] Import baseline schema (nexora.sql, business data cleared) ...
REM Run via cmd so the input redirection works with Chinese-safe quoting
cmd /c ""%MYSQL%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB% < "%BASE%""
if errorlevel 1 goto :fail

echo [3/3] Apply incremental scripts (2026*.sql, re-runnable) ...
for %%f in ("%DIR%2026*.sql") do (
    echo   - %%~nxf
    cmd /c ""%MYSQL%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB% < "%%f""
    if errorlevel 1 goto :fail
)

echo.
echo [OK] Database %DB% migrated (baseline + incrementals, includes user
echo      audit columns and ai_usage_record)!
echo See docs/ for env vars and startup steps
pause
exit /b 0

:fail
echo.
echo [ERROR] Migration failed!
echo   1) Is MySQL running? (mysql client available as: %MYSQL%)
echo   2) Are the credentials correct? (default root/123456, pass args to override)
echo   3) Can you access %BASE% ?
pause
exit /b 1
