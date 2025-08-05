#!/bin/bash

echo "Building Work Hour Tracker..."

# Clean previous builds
rm -rf build/
rm -f *.jar

# Create build directory
mkdir -p build

# Compile Java files
echo "Compiling Java files..."
javac -d build com/worktracker/*.java com/worktracker/*/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Create JAR file
echo "Creating JAR file..."
cd build
jar cfm ../WorkHourTracker.jar ../MANIFEST.MF com/

cd ..
echo "Build completed successfully!"
echo "JAR file: WorkHourTracker.jar"