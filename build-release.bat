@echo off

call mvnw.cmd clean package

rmdir /s /q dist 2>nul

jpackage --type app-image --name AuraMusicFX --input target --main-jar AuraMusicFX-1.0.0.jar --main-class dev.aman.auramusicfx.Launcher --icon src\main\resources\icons\AuraMusicFX_Ultimate.ico --dest dist

xcopy vlc dist\AuraMusicFX\vlc\ /E /I /Y

echo.
echo Aura Music build complete.
pause