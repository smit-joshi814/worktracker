#!/bin/bash

# Create a simple clock icon using ImageMagick (if available) or use existing clock.png
if command -v convert >/dev/null 2>&1; then
    # Create 48x48 icon using ImageMagick
    convert -size 48x48 xc:transparent \
        -fill "#4682B4" -draw "circle 24,24 24,4" \
        -fill "white" -stroke "#191970" -strokewidth 2 \
        -draw "line 24,24 24,12" -draw "line 24,24 36,24" \
        -fill "white" -draw "circle 24,24 24,22" \
        debian/usr/share/pixmaps/worktracker.png
    echo "Created icon using ImageMagick"
elif [ -f "clock.png" ]; then
    # Use existing clock.png if available
    cp clock.png debian/usr/share/pixmaps/worktracker.png
    echo "Used existing clock.png"
else
    # Create a simple text-based icon as fallback
    convert -size 48x48 xc:"#4682B4" \
        -fill white -gravity center -pointsize 20 -annotate +0+0 "⏰" \
        debian/usr/share/pixmaps/worktracker.png 2>/dev/null || \
    echo "No icon created - ImageMagick not available and no clock.png found"
fi