package com.worktracker.ui;

import com.worktracker.core.WorkSession;
import com.worktracker.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class MainStatusPanel extends JPanel {
    private JLabel statusLabel, timeLabel, dailyLabel, weeklyLabel, dateLabel;
    private JProgressBar dailyGoalProgress;
    private static final long DAILY_GOAL_SECONDS = 8 * 60 * 60; // 8 hours goal

    public MainStatusPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        
        statusLabel = new JLabel("Ready to work", SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        statusLabel.setForeground(new Color(108, 117, 125));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 42));
        timeLabel.setForeground(new Color(33, 37, 41));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        LocalDate today = LocalDate.now();
        dateLabel = new JLabel(TimeUtils.formatDateWithDay(today), SwingConstants.CENTER);
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dateLabel.setForeground(new Color(108, 117, 125));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        dailyLabel = new JLabel("Today: 00:00:00", SwingConstants.CENTER);
        dailyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dailyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        weeklyLabel = new JLabel("This Week: 00:00:00", SwingConstants.CENTER);
        weeklyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        weeklyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        dailyGoalProgress = new JProgressBar(0, (int) DAILY_GOAL_SECONDS);
        dailyGoalProgress.setStringPainted(true);
        dailyGoalProgress.setForeground(new Color(40, 167, 69)); // Success Green
        dailyGoalProgress.setBackground(new Color(233, 236, 239));
        dailyGoalProgress.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        dailyGoalProgress.setPreferredSize(new Dimension(300, 22));
        dailyGoalProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        dailyGoalProgress.setBorderPainted(false);
        
        add(statusLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(timeLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(dateLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(dailyLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(weeklyLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(dailyGoalProgress);
    }

    public void updateState(WorkSession.State state) {
        if (state == WorkSession.State.IDLE) {
            statusLabel.setText("Ready to work");
            statusLabel.setForeground(new Color(108, 117, 125)); // Gray
        } else if (state == WorkSession.State.WORKING) {
            statusLabel.setText("Working");
            statusLabel.setForeground(new Color(40, 167, 69)); // Green
        } else if (state == WorkSession.State.ON_BREAK) {
            statusLabel.setText("On Break");
            statusLabel.setForeground(new Color(253, 126, 20)); // Orange
        }
    }
    
    public void updateTimeText(String timeStr) {
        timeLabel.setText(timeStr);
    }
    
    public void updateStats(long dailyWork, long weeklyWork) {
        LocalDate today = LocalDate.now();
        dailyLabel.setText("Today (" + TimeUtils.getShortDayName(today) + "): " + TimeUtils.formatDuration(dailyWork));
        weeklyLabel.setText("This Week: " + TimeUtils.formatDuration(weeklyWork));
        
        int progress = (int) Math.min(dailyWork, DAILY_GOAL_SECONDS);
        dailyGoalProgress.setValue(progress);
        int percentage = (int) ((dailyWork * 100) / DAILY_GOAL_SECONDS);
        dailyGoalProgress.setString("Daily Goal: " + percentage + "%");
    }
}