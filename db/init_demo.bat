@echo off
setlocal EnableExtensions
title NestStay Demo DB Init

set "ROOT=%~dp0"
set "MYSQL=mysql"
set "DB=neststay_demo"
set "USER=root"
set "PASS=123456"
set "NESTSTAY_DB=%ROOT%..\..\NestStay\db"
set "MYSQL_OPTS=--default-character-set=utf8mb4"

where mysql >nul 2>&1
if errorlevel 1 (
  echo [ERROR] mysql client not found. Add MySQL bin to PATH.
  exit /b 1
)

echo ============================================================
echo   Initializing MySQL database: %DB%
echo ============================================================
echo.

echo [1/3] Recreate database...
"%MYSQL%" %MYSQL_OPTS% -u%USER% -p%PASS% -e "DROP DATABASE IF EXISTS `%DB%`; CREATE DATABASE `%DB%` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 goto :failed

echo [2/3] Load base schema from NestStay neststay.sql ...
if not exist "%NESTSTAY_DB%\neststay.sql" (
  echo [ERROR] Missing %NESTSTAY_DB%\neststay.sql
  exit /b 1
)
powershell -NoProfile -Command "$c = Get-Content -Raw -Encoding UTF8 '%NESTSTAY_DB%\neststay.sql'; $c = $c -replace '`neststay`','`neststay_demo`'; $c | & mysql %MYSQL_OPTS% -u%USER% -p%PASS% %DB%"
if errorlevel 1 goto :failed

echo [3/3] Apply migrations in order ...
call :run_sql migrate_round3.sql
call :run_sql migrate_round4.sql
call :run_sql migrate_round5.sql
call :run_sql migrate_round6.sql
call :run_sql migrate_round7.sql
call :run_sql migrate_round8.sql
call :run_sql migrate_round9.sql
call :run_sql migrate_round10.sql
call :run_sql migrate_round11.sql
call :run_sql migrate_round12.sql
call :run_sql migrate_round13.sql
call :run_sql migrate_round14.sql
call :run_sql migrate_round15.sql
call :run_sql migrate_round16.sql
call :run_sql migrate_round17.sql
call :run_sql migrate_round18.sql
call :run_sql migrate_round20.sql
call :run_sql migrate_admin_permissions.sql
call :run_sql migrate_core_workflows.sql
call :run_sql migrate_forum_hierarchy.sql
call :run_sql migrate_home_display.sql
call :run_sql migrate_news_comment_menu.sql
call :run_sql migrate_news_features.sql
call :run_sql migrate_order_status.sql

if exist "%ROOT%neststay_demo_seed.sql" (
  echo [+] Apply demo seed ...
  "%MYSQL%" %MYSQL_OPTS% -u%USER% -p%PASS% %DB% < "%ROOT%neststay_demo_seed.sql"
)

echo.
echo Done. Database `%DB%` is ready.
echo Admin login: admin / admin
exit /b 0

:run_sql
if not exist "%NESTSTAY_DB%\%~1" exit /b 0
echo       Running %~1
"%MYSQL%" %MYSQL_OPTS% -u%USER% -p%PASS% %DB% < "%NESTSTAY_DB%\%~1"
if errorlevel 1 echo [WARN] %~1 returned non-zero. Continuing...
exit /b 0

:failed
echo [ERROR] Database init failed.
exit /b 1
