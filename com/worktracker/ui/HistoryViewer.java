package com.worktracker.ui;

import com.worktracker.data.DataManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoryViewer {
    private DataManager dataManager;
    
    public HistoryViewer(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    public void showHistory(Component parent) {
        JFrame historyFrame = new JFrame("Work History");
        historyFrame.setSize(950, 600);
        historyFrame.setLocationRelativeTo(parent);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        String[] columns = {"Date (Day)", "Start Time", "End Time", "Work Time", "Break Time", "Break Sessions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        List<Object[]> rows = dataManager.getHistoryData();
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No history found or error reading log file.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Add rows in reverse order (newest first)
        for (int i = rows.size() - 1; i >= 0; i--) {
            model.addRow(rows.get(i));
        }
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Date with day name
        table.getColumnModel().getColumn(5).setPreferredWidth(250);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Work Session History (" + rows.size() + " sessions)"));
        
        historyFrame.add(scrollPane);
        historyFrame.setVisible(true);
    }
}