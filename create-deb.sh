#!/bin/bash

echo "Creating Debian package for Work Hour Tracker..."

# Build the JAR if it doesn't exist
if [ ! -f "WorkHourTracker.jar" ]; then
    echo "Building JAR file..."
    bash build.sh
fi

# Copy JAR to package directory
cp WorkHourTracker.jar debian/usr/share/worktracker/

# Create icon
chmod +x create-icon.sh
bash create-icon.sh

# Set proper permissions
chmod 755 debian/DEBIAN/postinst
chmod 755 debian/DEBIAN/prerm
chmod 755 debian/usr/bin/worktracker

# Create the package
dpkg-deb --build debian worktracker_1.0.0_all.deb

echo "Debian package created: worktracker_1.0.0_all.deb"
echo ""
echo "To install:"
echo "  sudo dpkg -i worktracker_1.0.0_all.deb"
echo "  sudo apt-get install -f  # if dependencies are missing"
echo ""
echo "To remove:"
echo "  sudo apt-get remove worktracker"