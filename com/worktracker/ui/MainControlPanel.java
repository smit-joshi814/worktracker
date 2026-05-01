package com.worktracker.ui;

import com.worktracker.core.WorkSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainControlPanel extends JPanel {
    private JButton startWorkBtn, startBreakBtn, resumeWorkBtn, stopWorkBtn, historyBtn, statsBtn;
    private JCheckBox autoTrackCheckBox;

    public MainControlPanel(Runnable onStartWork, Runnable onStartBreak, Runnable onResumeWork, 
                            Runnable onStopWork, Runnable onHistory, Runnable onStats) {
        setLayout(new BorderLayout(5, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        setBackground(new Color(248, 249, 250));
        
        JPanel buttonGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonGrid.setBackground(new Color(248, 249, 250));
        
        startWorkBtn = createStyledButton("Start Work", new Color(40, 167, 69));
        startBreakBtn = createStyledButton("Start Break", new Color(253, 126, 20));
        resumeWorkBtn = createStyledButton("Resume Work", new Color(40, 167, 69));
        stopWorkBtn = createStyledButton("Stop Work", new Color(220, 53, 69));
        historyBtn = createStyledButton("View History", new Color(13, 110, 253));
        statsBtn = createStyledButton("Statistics", new Color(111, 66, 193));
        
        buttonGrid.add(startWorkBtn);
        buttonGrid.add(startBreakBtn);
        buttonGrid.add(resumeWorkBtn);
        buttonGrid.add(stopWorkBtn);
        buttonGrid.add(historyBtn);
        buttonGrid.add(statsBtn);
        
        add(buttonGrid, BorderLayout.CENTER);
        
        autoTrackCheckBox = new JCheckBox("Auto-Track (Start/Stop automatically based on mouse activity)");
        autoTrackCheckBox.setBackground(new Color(248, 249, 250));
        autoTrackCheckBox.setForeground(new Color(108, 117, 125));
        autoTrackCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        autoTrackCheckBox.setToolTipText("Starts work automatically when active. Takes a break after 5 mins of idle.");
        autoTrackCheckBox.setSelected(true);
        add(autoTrackCheckBox, BorderLayout.SOUTH);
        
        startWorkBtn.addActionListener(e -> onStartWork.run());
        startBreakBtn.addActionListener(e -> onStartBreak.run());
        resumeWorkBtn.addActionListener(e -> onResumeWork.run());
        stopWorkBtn.addActionListener(e -> onStopWork.run());
        historyBtn.addActionListener(e -> onHistory.run());
        statsBtn.addActionListener(e -> onStats.run());
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);
                setBackground(b ? color : new Color(200, 200, 200));
            }
        };
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(color.brighter()); }
            @Override public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(color); }
        });
        return button;
    }

    public void updateState(WorkSession.State state) {
        startWorkBtn.setEnabled(state == WorkSession.State.IDLE);
        startBreakBtn.setEnabled(state == WorkSession.State.WORKING);
        resumeWorkBtn.setEnabled(state == WorkSession.State.ON_BREAK);
        stopWorkBtn.setEnabled(state != WorkSession.State.IDLE);
    }
    
    public boolean isAutoTrackEnabled() { return autoTrackCheckBox.isSelected(); }
    public void setAutoTrackEnabled(boolean enabled) { autoTrackCheckBox.setSelected(enabled); }
}