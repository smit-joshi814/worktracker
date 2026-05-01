package com.worktracker.ui;

import com.worktracker.core.WorkSession;
import com.worktracker.data.DataManager;
import com.worktracker.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
        statsFrame.setSize(750, 600);
        statsFrame.setLocationRelativeTo(parent);
        statsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(248, 249, 250)); // Light Gray Theme
        
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setBackground(new Color(248, 249, 250));
        topPanel.add(createCurrentSessionPanel(session), BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(createWeeklyBreakdownPanel(session), BorderLayout.CENTER);
        mainPanel.add(createSummaryPanel(session), BorderLayout.SOUTH);
        
        statsFrame.add(mainPanel);
        statsFrame.setVisible(true);
    }
    
    private JPanel createStyledCard(String title, Component content) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(new Color(73, 80, 87));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void addStyledRow(JPanel panel, String labelText, String valueText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(new Color(108, 117, 125));
        
        JLabel value = new JLabel(valueText);
        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        value.setForeground(new Color(33, 37, 41));
        
        panel.add(label);
        panel.add(value);
    }
    
    private JPanel createCurrentSessionPanel(WorkSession session) {
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 10));
        grid.setBackground(Color.WHITE);
        
        long activeSeconds = dataManager.getSessionActiveWorkSeconds(session);
        long todayWork = dataManager.calculateDailyWorkHours() + activeSeconds;
        long weeklyWork = dataManager.calculateWeeklyWorkHours() + activeSeconds;
        LocalDate today = LocalDate.now();
        
        addStyledRow(grid, "Status:", session.getCurrentState().toString().replace("_", " "));
        addStyledRow(grid, "Date:", TimeUtils.formatDateWithDay(today));
        addStyledRow(grid, "Today:", TimeUtils.formatDuration(todayWork));
        addStyledRow(grid, "Week Total:", TimeUtils.formatDuration(weeklyWork));
        
        return createStyledCard("Current Session", grid);
    }
    
    private JPanel createWeeklyBreakdownPanel(WorkSession session) {
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
                dayWork += dataManager.getSessionActiveWorkSeconds(session);
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
        weekTable.setRowHeight(35);
        weekTable.setShowVerticalLines(false);
        weekTable.setGridColor(new Color(233, 236, 239));
        weekTable.setBackground(Color.WHITE);
        weekTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        JTableHeader header = weekTable.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(73, 80, 87));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        header.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        weekTable.getTableHeader().setReorderingAllowed(false);
        weekTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Day name column
        
        JScrollPane scrollPane = new JScrollPane(weekTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        return createStyledCard("This Week's Breakdown", scrollPane);
    }
    
    private JPanel createSummaryPanel(WorkSession session) {
        Map<String, Long> monthlyStats = dataManager.calculateMonthlyStats();
        long totalSessions = dataManager.countTotalSessions();
        
        long weeklyWork = dataManager.calculateWeeklyWorkHours() + dataManager.getSessionActiveWorkSeconds(session);
        double avgDaily = weeklyWork / 7.0 / 3600.0; // Convert to hours
        
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 10));
        grid.setBackground(Color.WHITE);
        
        addStyledRow(grid, "This Month:", TimeUtils.formatDuration(monthlyStats.get("current")));
        addStyledRow(grid, "Last Month:", TimeUtils.formatDuration(monthlyStats.get("previous")));
        addStyledRow(grid, "Total Sessions:", String.valueOf(totalSessions));
        addStyledRow(grid, "Avg Daily:", String.format("%.1f hrs", avgDaily));
        
        return createStyledCard("Summary", grid);
    }
}