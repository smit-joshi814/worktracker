#!/bin/bash

echo "Building Work Hour Tracker Package..."

# Clean previous builds
rm -rf build/ *.jar *.deb

# Build JAR
echo "Compiling Java files..."
mkdir -p build
javac -d build com/worktracker/*.java com/worktracker/*/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Creating JAR..."
cd build
jar cfm ../WorkHourTracker.jar ../MANIFEST.MF com/
cd ..

# Copy JAR to Debian package
cp WorkHourTracker.jar debian/usr/share/worktracker/

# Create icon if ImageMagick available
if command -v convert >/dev/null 2>&1; then
    convert -size 48x48 xc:"#4682B4" \
        -fill white -gravity center -pointsize 20 -annotate +0+0 "⏰" \
        debian/usr/share/pixmaps/worktracker.png 2>/dev/null
elif [ -f "clock.png" ]; then
    cp clock.png debian/usr/share/pixmaps/worktracker.png
fi

# Set permissions
chmod 755 debian/DEBIAN/postinst debian/DEBIAN/prerm debian/usr/bin/worktracker

# Build Debian package
dpkg-deb --build debian worktracker_1.0.1_all.deb

echo ""
echo "Package created successfully!"
echo "JAR: WorkHourTracker.jar"
echo "DEB: worktracker_1.0.1_all.deb"
echo ""
echo "Install: sudo dpkg -i worktracker_1.0.1_all.deb"