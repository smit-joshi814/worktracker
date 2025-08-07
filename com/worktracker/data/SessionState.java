package com.worktracker.data;

import com.worktracker.core.WorkSession;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionState {
    private static final String STATE_FILE = ".worktracker-state";
    
    public static void saveState(WorkSession session) {
        if (session.getCurrentState() == WorkSession.State.IDLE) {
            deleteStateFile();
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(STATE_FILE))) {
            writer.println(session.getCurrentState().name());
            writer.println(session.getWorkStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            writer.println(session.getTotalWorkSeconds());
            writer.println(session.getTotalBreakSeconds());
            writer.println(System.currentTimeMillis());
            
            // Save break sessions
            List<String> breaks = session.getBreakSessions();
            writer.println(breaks.size());
            for (String breakSession : breaks) {
                writer.println(breakSession);
            }
        } catch (IOException e) {
            // Ignore save errors
        }
    }
    
    public static boolean restoreState(WorkSession session) {
        File stateFile = new File(STATE_FILE);
        if (!stateFile.exists()) {
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(stateFile))) {
            String stateStr = reader.readLine();
            if (stateStr == null) return false;
            
            WorkSession.State state = WorkSession.State.valueOf(stateStr);
            String startTimeStr = reader.readLine();
            long totalWorkSeconds = Long.parseLong(reader.readLine());
            long totalBreakSeconds = Long.parseLong(reader.readLine());
            long lastUpdateTime = Long.parseLong(reader.readLine());
            
            // Calculate elapsed time since save
            long elapsedSinceUpdate = (System.currentTimeMillis() - lastUpdateTime) / 1000;
            
            // Restore session
            session.restoreState(
                state,
                LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm:ss")),
                totalWorkSeconds,
                totalBreakSeconds,
                elapsedSinceUpdate
            );
            
            // Restore break sessions
            int breakCount = Integer.parseInt(reader.readLine());
            for (int i = 0; i < breakCount; i++) {
                String breakSession = reader.readLine();
                if (breakSession != null) {
                    session.addBreakSession(breakSession);
                }
            }
            
            deleteStateFile();
            return true;
            
        } catch (Exception e) {
            deleteStateFile();
            return false;
        }
    }
    
    private static void deleteStateFile() {
        new File(STATE_FILE).delete();
    }
}