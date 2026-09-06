@echo off
setlocal EnableExtensions EnableDelayedExpansion
title NestStay Shell launcher

rem Preview shell: backend 8080 + admin Vite 8081 + consumer Vite 8082
rem Production bundle (backend only):
rem   initiate_service.bat build

set "ROOT=%~dp0"
set "FRONT_DIR=%ROOT%src\main\resources\front\front"
set "ADMIN_DIR=%ROOT%src\main\resources\admin\admin"
set "MODE=debug"
if /i "%~1"=="build" set "MODE=build"
if /i "%~1"=="--build" set "MODE=build"

cd /d "%ROOT%" || goto :root_error

echo ============================================================
if /i "%MODE%"=="build" (
    echo   NestStay Shell - production build + backend only
) else (
    echo   NestStay Shell - debug launcher
    echo   backend 8080 / admin 8081 / consumer 8082
)
echo ============================================================
echo.
echo First-time setup: run db\init_demo.bat to create neststay_demo
echo.

where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not found.
    goto :failed
)

where npm >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npm not found.
    goto :failed
)

if not exist "%ROOT%gradlew.bat" (
    echo [ERROR] gradlew.bat not found.
    goto :failed
)

if /i "%MODE%"=="build" goto :prod_build

if not exist "%FRONT_DIR%\dist\index.html" (
    echo [WARN] Consumer dist missing. Run scripts\build-shell-frontends.ps1 first.
)
if not exist "%ADMIN_DIR%\dist\index.html" (
    echo [WARN] Admin dist missing. Run scripts\build-shell-frontends.ps1 first.
)

echo [1/4] Freeing ports 8080 / 8081 / 8082...
call :free_port 8080
call :free_port 8081
call :free_port 8082

echo [2/4] Compiling backend...
call gradlew.bat classes -x test
if errorlevel 1 goto :failed

echo [3/4] Starting backend...
start "NestStay-Shell-Backend" /D "%ROOT%" cmd /k gradlew.bat bootRun

echo [4/4] Starting Vite dev servers (optional preview of dist)...
if exist "%ADMIN_DIR%\package.json" start "NestStay-Shell-Admin" /D "%ADMIN_DIR%" cmd /k npm run dev
if exist "%FRONT_DIR%\package.json" start "NestStay-Shell-Front" /D "%FRONT_DIR%" cmd /k npm run dev

timeout /t 8 /nobreak >nul
start "" "http://localhost:8080/neststay/front/index.html#/index/home"
start "" "http://localhost:8080/neststay/admin/admin/dist/index.html#/login"
call :print_links
pause
exit /b 0

:prod_build
echo [1/4] Build frontends with shell guards...
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%scripts\build-shell-frontends.ps1"
if errorlevel 1 goto :failed

echo [2/4] Compile backend...
call :free_port 8080
call gradlew.bat classes -x test
if errorlevel 1 goto :failed

echo [3/4] Starting backend on port 8080...
echo [4/4] Open URLs below after bootRun starts.
call :print_links
call gradlew.bat bootRun
exit /b %ERRORLEVEL%

:print_links
echo.
echo ============================================================
echo   NestStay Shell URLs
echo ------------------------------------------------------------
echo   API            http://localhost:8080/neststay/
echo   Consumer home  http://localhost:8080/neststay/front/index.html#/index/home
echo   Admin login    http://localhost:8080/neststay/admin/admin/dist/index.html#/login
echo   Contact URL    see application.yml shell.contact-url
echo ============================================================
goto :eof

:free_port
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /C:":%~1 " ^| findstr /I "LISTENING"') do taskkill /F /PID %%P >nul 2>&1
goto :eof

:root_error
echo [ERROR] Cannot enter %ROOT%
:failed
pause
exit /b 1
