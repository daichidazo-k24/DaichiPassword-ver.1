@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo === 起動チェック ===
echo [現在地] %CD%
echo.
echo [targetフォルダの中にあるJARファイル]
dir target\*.jar
echo.
echo ====================
echo ツールを起動します...

java -jar target\rsa-password-1.0-SNAPSHOT-jar-with-dependencies.jar

echo.
pause