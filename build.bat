@echo off

echo Building Work Hour Tracker...

REM Clean previous builds
if exist build rmdir /s /q build
if exist *.jar del *.jar

REM Create build directory
mkdir build

REM Compile Java files
echo Compiling Java files...
javac -d build com\worktracker\*.java com\worktracker\core\*.java com\worktracker\data\*.java com\worktracker\tray\*.java com\worktracker\ui\*.java com\worktracker\utils\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Create JAR file
echo Creating JAR file...
cd build
jar cfm ..\WorkHourTracker.jar ..\MANIFEST.MF com\

cd ..
echo Build completed successfully!
echo JAR file: WorkHourTracker.jar
pause