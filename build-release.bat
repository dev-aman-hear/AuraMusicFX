@echo off

call mvnw.cmd clean package

rmdir /s /q dist 2>nul
rmdir /s /q custom-jre 2>nul

echo Building custom minimal JRE...
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.logging,jdk.jfr,jdk.unsupported,java.management,java.naming --output custom-jre

jpackage --type app-image --name AuraMusicFX --input target --main-jar AuraMusicFX-1.0.0.jar --main-class dev.aman.auramusicfx.Launcher --icon src\main\resources\icons\AuraMusicFX_Ultimate.ico --runtime-image custom-jre --dest dist

xcopy vlc dist\AuraMusicFX\vlc\ /E /I /Y

echo.
echo Aura Music build complete.
pause