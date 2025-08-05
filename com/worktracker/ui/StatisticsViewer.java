package com.worktracker.ui;

import com.worktracker.core.WorkSession;
import com.worktracker.data.DataManager;
import com.worktracker.utils.TimeUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.Map;

public class StatisticsViewer {
    private DataManager dataManager;
    
    public StatisticsViewer(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    public void showStatistics(Component parent, WorkSession session) {
        JFrame statsFrame = new JFrame("Work Statistics");
        statsFrame.setSize(650, 500);
        statsFrame.setLocationRelativeTo(parent);
        statsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Current session info
        JPanel currentPanel = createCurrentSessionPanel(session);
        
        // Weekly breakdown
        JPanel weeklyPanel = createWeeklyBreakdownPanel(session);
        
        // Summary stats
        JPanel summaryPanel = createSummaryPanel(session);
        
        mainPanel.add(currentPanel, BorderLayout.NORTH);
        mainPanel.add(weeklyPanel, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        statsFrame.add(mainPanel);
        statsFrame.setVisible(true);
    }
    
    private JPanel createCurrentSessionPanel(WorkSession session) {
        JPanel currentPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        currentPanel.setBorder(BorderFactory.createTitledBorder("Current Session"));
        
        long todayWork = dataManager.calculateDailyWorkHours();
        long weeklyWork = dataManager.calculateWeeklyWorkHours();
        
        if (session.getCurrentState() == WorkSession.State.WORKING) {
            long currentElapsed = session.getCurrentElapsedSeconds();
            todayWork += session.getTotalWorkSeconds() + currentElapsed;
            weeklyWork += session.getTotalWorkSeconds() + currentElapsed;
        } else if (session.getCurrentState() != WorkSession.State.IDLE) {
            todayWork += session.getTotalWorkSeconds();
            weeklyWork += session.getTotalWorkSeconds();
        }
        
        LocalDate today = LocalDate.now();
        
        currentPanel.add(new JLabel("Status:"));
        currentPanel.add(new JLabel(session.getCurrentState().toString().replace("_", " ")));
        currentPanel.add(new JLabel("Today (" + TimeUtils.getDayName(today) + "):"));
        currentPanel.add(new JLabel(TimeUtils.formatDuration(todayWork)));
        currentPanel.add(new JLabel("This Week Total:"));
        currentPanel.add(new JLabel(TimeUtils.formatDuration(weeklyWork)));
        currentPanel.add(new JLabel("Date:"));
        currentPanel.add(new JLabel(TimeUtils.formatDateWithDay(today)));
        
        return currentPanel;
    }
    
    private JPanel createWeeklyBreakdownPanel(WorkSession session) {
        JPanel weeklyPanel = new JPanel(new BorderLayout());
        weeklyPanel.setBorder(BorderFactory.createTitledBorder("This Week's Breakdown"));
        
        String[] weekColumns = {"Day", "Date", "Work Hours", "Sessions"};
        DefaultTableModel weekModel = new DefaultTableModel(weekColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            if (day.isAfter(today)) break;
            
            long dayWork = dataManager.calculateWorkHours(day, day);
            int sessions = dataManager.countDailySessions(day);
            
            if (day.equals(today)) {
                if (session.getCurrentState() == WorkSession.State.WORKING) {
                    long currentElapsed = session.getCurrentElapsedSeconds();
                    dayWork += session.getTotalWorkSeconds() + currentElapsed;
                } else if (session.getCurrentState() != WorkSession.State.IDLE) {
                    dayWork += session.getTotalWorkSeconds();
                }
                if (session.getCurrentState() != WorkSession.State.IDLE) sessions++;
            }
            
            weekModel.addRow(new Object[]{
                TimeUtils.getDayName(day),
                day.toString(),
                TimeUtils.formatDuration(dayWork),
                sessions
            });
        }
        
        JTable weekTable = new JTable(weekModel);
        weekTable.setRowHeight(25);
        weekTable.getTableHeader().setReorderingAllowed(false);
        weekTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Day name column
        weeklyPanel.add(new JScrollPane(weekTable));
        
        return weeklyPanel;
    }
    
    private JPanel createSummaryPanel(WorkSession session) {
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        
        Map<String, Long> monthlyStats = dataManager.calculateMonthlyStats();
        long totalSessions = dataManager.countTotalSessions();
        
        long weeklyWork = dataManager.calculateWeeklyWorkHours();
        if (session.getCurrentState() == WorkSession.State.WORKING) {
            long currentElapsed = session.getCurrentElapsedSeconds();
            weeklyWork += session.getTotalWorkSeconds() + currentElapsed;
        } else if (session.getCurrentState() != WorkSession.State.IDLE) {
            weeklyWork += session.getTotalWorkSeconds();
        }
        
        double avgDaily = weeklyWork / 7.0 / 3600.0; // Convert to hours
        
        summaryPanel.add(new JLabel("This Month:"));
        summaryPanel.add(new JLabel(TimeUtils.formatDuration(monthlyStats.get("current"))));
        summaryPanel.add(new JLabel("Last Month:"));
        summaryPanel.add(new JLabel(TimeUtils.formatDuration(monthlyStats.get("previous"))));
        summaryPanel.add(new JLabel("Total Sessions:"));
        summaryPanel.add(new JLabel(String.valueOf(totalSessions)));
        summaryPanel.add(new JLabel("Avg Daily (This Week):"));
        summaryPanel.add(new JLabel(String.format("%.1f hours", avgDaily)));
        
        return summaryPanel;
    }
}