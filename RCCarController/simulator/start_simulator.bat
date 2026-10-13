@echo off
REM ─────────────────────────────────────────────────────────────────────────────
REM  RC Car Simulator — Quick Start (Windows)
REM  Launches the Python simulator and sets up ADB port forwarding in one step.
REM ─────────────────────────────────────────────────────────────────────────────

echo.
echo  [1/2] Setting up ADB reverse port forwarding...
adb reverse tcp:9035 tcp:9035
if %ERRORLEVEL% NEQ 0 (
    echo  ERROR: ADB failed. Make sure:
    echo    - Android Studio emulator is running
    echo    - ADB is in your PATH  ^(usually: C:\Users\YOU\AppData\Local\Android\Sdk\platform-tools^)
    pause
    exit /b 1
)
echo  Port 9035 forwarded successfully.
echo.
echo  [2/2] Starting RC Car Simulator...
echo  Press Ctrl+C to stop.
echo.
python "%~dp0car_simulator.py"
pause
