package com.worktracker;

import com.worktracker.core.WorkSession;
import com.worktracker.core.AutoTracker;
import com.worktracker.data.DataManager;
import com.worktracker.data.SessionState;
import com.worktracker.tray.SystemTrayManager;
import com.worktracker.ui.HistoryViewer;
import com.worktracker.ui.StatisticsViewer;
import com.worktracker.ui.SummaryDialog;
import com.worktracker.ui.MainStatusPanel;
import com.worktracker.ui.MainControlPanel;
import com.worktracker.utils.TimeUtils;
import com.worktracker.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WorkHourTracker extends JFrame {
    private WorkSession session;
    private DataManager dataManager;
    private SystemTrayManager trayManager;
    private HistoryViewer historyViewer;
    private StatisticsViewer statisticsViewer;
    
    private MainStatusPanel statusPanel;
    private MainControlPanel controlPanel;

    private AutoTracker autoTracker;
    private Timer uiTimer;
    private boolean isManualBreak = false;

    
    public WorkHourTracker() {
        session = new WorkSession();
        dataManager = new DataManager();
        trayManager = new SystemTrayManager();
        historyViewer = new HistoryViewer(dataManager);
        statisticsViewer = new StatisticsViewer(dataManager);
        
        autoTracker = new AutoTracker(5 * 60 * 1000, () -> {
            // Active callback
            if (session.getCurrentState() == WorkSession.State.IDLE) {
                startWork();
            } else if (session.getCurrentState() == WorkSession.State.ON_BREAK && !isManualBreak) {
                resumeWork(false);
            }
        }, () -> {
            // Idle callback
            if (session.getCurrentState() == WorkSession.State.WORKING) {
                startBreak(false);
            }
        });
        
        initializeUI();
        setupSystemTray();
        startUITimer();
        
        // Setup application shutdown handling
        SessionState.registerShutdownHook(session, dataManager);
        
        // Restore previous session state if exists
        if (SessionState.restoreState(session)) {
            System.out.println("Previous session restored");
        }
        
        updateUI();
    }
    
    private void initializeUI() {
        setTitle("Work Hour Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(IconUtils.createAppIcon());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SessionState.saveState(session);
                if (trayManager.isSupported()) {
                    setVisible(false);
                } else {
                    System.exit(0);
                }
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                SessionState.saveState(session);
                if (trayManager.isSupported()) {
                    setVisible(false);
                }
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(248, 249, 250)); // Light Gray Theme
        
        // Status panel
        statusPanel = new MainStatusPanel();
        
        // Button panel
        controlPanel = new MainControlPanel(
            this::startWork,
            () -> startBreak(true),
            () -> resumeWork(true),
            this::stopWork,
            () -> historyViewer.showHistory(this),
            () -> statisticsViewer.showStatistics(this, session)
        );
        
        mainPanel.add(statusPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        add(mainPanel);
        
        updateDailyWeeklyStats();
    }
    
    private void setupSystemTray() {
        trayManager.setupSystemTray(
            e -> showWindow(),
            () -> {
                if (session.getCurrentState() != WorkSession.State.IDLE) {
                    showWindow();
                    SwingUtilities.invokeLater(() -> {
                        int result = JOptionPane.showConfirmDialog(this,
                            "Work session is active. Stop and exit?", "Confirm Exit",
                            JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            stopWork();
                            SessionState.saveState(session);
                            System.exit(0);
                        }
                    });
                } else {
                    SessionState.saveState(session);
                    System.exit(0);
                }
            }
        );
    }

    private void showWindow() {
        SwingUtilities.invokeLater(() -> {
            setExtendedState(JFrame.NORMAL);
            setVisible(true);
            toFront();
            requestFocus();
        });
    }
    
    private void startUITimer() {
        uiTimer = new Timer(1000, e -> {
            if (controlPanel != null && controlPanel.isAutoTrackEnabled()) {
                autoTracker.check();
            }
            updateTimeDisplay();
        });
        uiTimer.start();
    }
    
    private void startWork() {
        session.startWork();
        updateUI();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void startBreak(boolean manual) {
        isManualBreak = manual;
        session.startBreak();
        updateUI();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void resumeWork(boolean manual) {
        isManualBreak = false;
        session.resumeWork();
        updateUI();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void stopWork() {
        if (controlPanel != null) {
            controlPanel.setAutoTrackEnabled(false);
        }
        isManualBreak = false;
        session.stopWork();
        SummaryDialog.show(this, session);
        try {
            dataManager.logSession(session);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateUI();
        updateDailyWeeklyStats();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void updateUI() {
        WorkSession.State state = session.getCurrentState();
        
        statusPanel.updateState(state);
        controlPanel.updateState(state);
    }
    
    private void updateTimeDisplay() {
        if (session.getCurrentState() == WorkSession.State.IDLE) {
            statusPanel.updateTimeText("00:00:00");
            return;
        }
        
        long currentElapsed = session.getCurrentElapsedSeconds();
        
        long totalElapsed = (session.getCurrentState() == WorkSession.State.WORKING) 
            ? session.getTotalWorkSeconds() + currentElapsed 
            : session.getTotalBreakSeconds() + currentElapsed;
            
        statusPanel.updateTimeText(TimeUtils.formatDuration(totalElapsed));
        
        updateDailyWeeklyStats();
    }
    
    private void updateDailyWeeklyStats() {
        long activeSeconds = dataManager.getSessionActiveWorkSeconds(session);
        long dailyWork = dataManager.calculateDailyWorkHours() + activeSeconds;
        long weeklyWork = dataManager.calculateWeeklyWorkHours() + activeSeconds;
        
        statusPanel.updateStats(dailyWork, weeklyWork);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                    // Use Nimbus on Linux to avoid GTK glibc native crashes
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } catch (Exception e) {
                System.err.println("Failed to set native look and feel: " + e.getMessage());
            }
            new WorkHourTracker().setVisible(true);
        });
    }
}