# Work Hour Tracker

A cross-platform Java desktop application for tracking daily working hours and break sessions.

## Features

- ⏱️ Track work sessions and breaks
- 📊 View daily and weekly statistics
- 📈 Historical data with detailed reports
- 🔔 System tray integration
- 🚀 Auto-start on system boot
- 💾 CSV data export

## Installation

### Quick Install (Linux/macOS)

1. **Build the application:**
   ```bash
   chmod +x build.sh
   ./build.sh
   ```

2. **Install the application:**
   ```bash
   chmod +x install.sh
   ./install.sh
   ```

3. **Run the application:**
   ```bash
   worktracker
   ```

### Manual Installation

1. **Build JAR file:**
   ```bash
   javac -d build com/worktracker/*.java com/worktracker/*/*.java
   cd build
   jar cfm ../WorkHourTracker.jar ../MANIFEST.MF com/
   cd ..
   ```

2. **Run directly:**
   ```bash
   java -jar WorkHourTracker.jar
   ```

## Usage

### Main Controls
- **Start Work**: Begin tracking work time
- **Start Break**: Pause work tracking and start break timer
- **Resume Work**: End break and resume work tracking
- **Stop Work**: End session and show summary

### Features
- **View History**: See all past work sessions
- **Statistics**: Detailed weekly and monthly reports
- **System Tray**: Minimize to system tray for background operation

### Data Storage
- Work sessions are automatically saved to `work-log.csv`
- Data includes date, times, work duration, break duration, and break sessions

## System Integration

### Auto-Start
The installer automatically configures the application to start on system boot.

### System Tray
- Application runs in background when minimized
- Right-click tray icon for quick access
- Shows current status (Ready/Working/On Break)

## Uninstallation

To remove the application:
```bash
~/.local/share/WorkHourTracker/uninstall.sh
```

This will remove all application files but preserve your work log data.

## Requirements

- Java 8 or higher
- Linux, macOS, or Windows
- System tray support (for background operation)

## File Structure

```
WorkHourTracker/
├── com/worktracker/
│   ├── core/WorkSession.java
│   ├── data/DataManager.java
│   ├── tray/SystemTrayManager.java
│   ├── ui/HistoryViewer.java
│   ├── ui/StatisticsViewer.java
│   ├── utils/TimeUtils.java
│   └── WorkHourTracker.java
├── build.sh
├── install.sh
├── MANIFEST.MF
└── README.md
```

## License

This project is open source and available under the MIT License.
