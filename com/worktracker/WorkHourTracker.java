package com.worktracker;

import com.worktracker.core.WorkSession;
import com.worktracker.data.DataManager;
import com.worktracker.tray.SystemTrayManager;
import com.worktracker.ui.HistoryViewer;
import com.worktracker.ui.StatisticsViewer;
import com.worktracker.utils.TimeUtils;
import com.worktracker.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WorkHourTracker extends JFrame {
    private WorkSession session;
    private DataManager dataManager;
    private SystemTrayManager trayManager;
    private HistoryViewer historyViewer;
    private StatisticsViewer statisticsViewer;
    
    private JButton startWorkBtn, startBreakBtn, resumeWorkBtn, stopWorkBtn, historyBtn, statsBtn;
    private JLabel statusLabel, timeLabel, dailyLabel, weeklyLabel, dateLabel;
    private Timer uiTimer;
    
    public WorkHourTracker() {
        session = new WorkSession();
        dataManager = new DataManager();
        trayManager = new SystemTrayManager();
        historyViewer = new HistoryViewer(dataManager);
        statisticsViewer = new StatisticsViewer(dataManager);
        
        initializeUI();
        setupSystemTray();
        startUITimer();
        updateUI();
    }
    
    private void initializeUI() {
        setTitle("Work Hour Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(IconUtils.createAppIcon());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (trayManager.isSupported()) {
                    setVisible(false);
                    // trayManager.showMessage("Work Hour Tracker", "Application minimized to system tray");
                } else {
                    System.exit(0);
                }
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                if (trayManager.isSupported()) {
                    setVisible(false);
                }
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(statusPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        
        updateDailyWeeklyStats();
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        statusPanel.setBackground(new Color(245, 245, 245));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Current Session"));
        
        statusLabel = new JLabel("Status: Ready to work", SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        statusLabel.setForeground(new Color(34, 139, 34));
        
        timeLabel = new JLabel("Elapsed: 00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        timeLabel.setForeground(new Color(25, 25, 112));
        
        LocalDate today = LocalDate.now();
        dateLabel = new JLabel(TimeUtils.formatDateWithDay(today), SwingConstants.CENTER);
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dateLabel.setForeground(new Color(105, 105, 105));
        
        dailyLabel = new JLabel("Today: 00:00:00", SwingConstants.CENTER);
        dailyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        weeklyLabel = new JLabel("This Week: 00:00:00", SwingConstants.CENTER);
        weeklyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        statusPanel.add(statusLabel);
        statusPanel.add(timeLabel);
        statusPanel.add(dateLabel);
        statusPanel.add(dailyLabel);
        statusPanel.add(weeklyLabel);
        
        return statusPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        startWorkBtn = createStyledButton("Start Work", new Color(34, 139, 34));
        startBreakBtn = createStyledButton("Start Break", new Color(255, 140, 0));
        resumeWorkBtn = createStyledButton("Resume Work", new Color(34, 139, 34));
        stopWorkBtn = createStyledButton("Stop Work", new Color(220, 20, 60));
        historyBtn = createStyledButton("View History", new Color(70, 130, 180));
        statsBtn = createStyledButton("Statistics", new Color(138, 43, 226));
        
        buttonPanel.add(startWorkBtn);
        buttonPanel.add(startBreakBtn);
        buttonPanel.add(resumeWorkBtn);
        buttonPanel.add(stopWorkBtn);
        buttonPanel.add(historyBtn);
        buttonPanel.add(statsBtn);
        
        // Button listeners
        startWorkBtn.addActionListener(e -> startWork());
        startBreakBtn.addActionListener(e -> startBreak());
        resumeWorkBtn.addActionListener(e -> resumeWork());
        stopWorkBtn.addActionListener(e -> stopWork());
        historyBtn.addActionListener(e -> historyViewer.showHistory(this));
        statsBtn.addActionListener(e -> statisticsViewer.showStatistics(this, session));
        
        return buttonPanel;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }
    
    private void setupSystemTray() {
        trayManager.setupSystemTray(
            e -> {
                setVisible(true);
                setState(Frame.NORMAL);
                toFront();
            },
            () -> {
                if (session.getCurrentState() != WorkSession.State.IDLE) {
                    setVisible(true);
                    setState(Frame.NORMAL);
                    toFront();
                    int result = JOptionPane.showConfirmDialog(this,
                        "Work session is active. Stop and exit?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        stopWork();
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            }
        );
    }
    
    private void startUITimer() {
        uiTimer = new Timer(1000, e -> updateTimeDisplay());
        uiTimer.start();
    }
    
    private void startWork() {
        session.startWork();
        updateUI();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void startBreak() {
        session.startBreak();
        updateUI();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void resumeWork() {
        session.resumeWork();
        updateUI();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void stopWork() {
        session.stopWork();
        showSummary();
        try {
            dataManager.logSession(session);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateUI();
        updateDailyWeeklyStats();
        trayManager.updateTrayIcon(session.getCurrentState());
    }
    
    private void showSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Work Session Summary\n\n");
        summary.append("Date: ").append(TimeUtils.formatDateWithDay(LocalDate.now())).append("\n");
        summary.append("Start Time: ").append(session.getWorkStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        summary.append("End Time: ").append(session.getWorkEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        summary.append("Total Work Time: ").append(TimeUtils.formatDuration(session.getTotalWorkSeconds())).append("\n");
        summary.append("Total Break Time: ").append(TimeUtils.formatDuration(session.getTotalBreakSeconds())).append("\n");
        
        if (!session.getBreakSessions().isEmpty()) {
            summary.append("\nBreak Sessions:\n");
            for (String breakSession : session.getBreakSessions()) {
                summary.append(breakSession).append("\n");
            }
        }
        
        JOptionPane.showMessageDialog(this, summary.toString(), "Session Summary", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateUI() {
        switch (session.getCurrentState()) {
            case IDLE:
                statusLabel.setText("Status: Ready to work");
                startWorkBtn.setEnabled(true);
                startBreakBtn.setEnabled(false);
                resumeWorkBtn.setEnabled(false);
                stopWorkBtn.setEnabled(false);
                historyBtn.setEnabled(true);
                statsBtn.setEnabled(true);
                break;
            case WORKING:
                statusLabel.setText("Status: Working");
                startWorkBtn.setEnabled(false);
                startBreakBtn.setEnabled(true);
                resumeWorkBtn.setEnabled(false);
                stopWorkBtn.setEnabled(true);
                historyBtn.setEnabled(true);
                statsBtn.setEnabled(true);
                break;
            case ON_BREAK:
                statusLabel.setText("Status: On Break");
                startWorkBtn.setEnabled(false);
                startBreakBtn.setEnabled(false);
                resumeWorkBtn.setEnabled(true);
                stopWorkBtn.setEnabled(true);
                historyBtn.setEnabled(true);
                statsBtn.setEnabled(true);
                break;
        }
    }
    
    private void updateTimeDisplay() {
        if (session.getCurrentState() == WorkSession.State.IDLE) {
            timeLabel.setText("Elapsed: 00:00:00");
            return;
        }
        
        long currentElapsed = session.getCurrentElapsedSeconds();
        
        if (session.getCurrentState() == WorkSession.State.WORKING) {
            long totalElapsed = session.getTotalWorkSeconds() + currentElapsed;
            timeLabel.setText("Work Time: " + TimeUtils.formatDuration(totalElapsed));
        } else {
            long totalElapsed = session.getTotalBreakSeconds() + currentElapsed;
            timeLabel.setText("Break Time: " + TimeUtils.formatDuration(totalElapsed));
        }
        
        updateDailyWeeklyStats();
    }
    
    private void updateDailyWeeklyStats() {
        long dailyWork = dataManager.calculateDailyWorkHours();
        long weeklyWork = dataManager.calculateWeeklyWorkHours();
        
        if (session.getCurrentState() == WorkSession.State.WORKING) {
            long currentElapsed = session.getCurrentElapsedSeconds();
            dailyWork += session.getTotalWorkSeconds() + currentElapsed;
            weeklyWork += session.getTotalWorkSeconds() + currentElapsed;
        } else if (session.getCurrentState() != WorkSession.State.IDLE) {
            dailyWork += session.getTotalWorkSeconds();
            weeklyWork += session.getTotalWorkSeconds();
        }
        
        LocalDate today = LocalDate.now();
        dailyLabel.setText("Today (" + TimeUtils.getShortDayName(today) + "): " + TimeUtils.formatDuration(dailyWork));
        weeklyLabel.setText("This Week: " + TimeUtils.formatDuration(weeklyWork));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel
            }
            new WorkHourTracker().setVisible(true);
        });
    }
}