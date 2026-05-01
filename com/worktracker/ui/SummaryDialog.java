package com.worktracker.ui;

import com.worktracker.core.WorkSession;
import com.worktracker.utils.TimeUtils;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SummaryDialog {
    public static void show(Component parent, WorkSession session) {
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
        
        JOptionPane.showMessageDialog(parent, summary.toString(), "Session Summary", JOptionPane.INFORMATION_MESSAGE);
    }
}