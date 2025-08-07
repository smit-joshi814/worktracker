package com.worktracker.core;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WorkSession {
    public enum State { IDLE, WORKING, ON_BREAK }
    
    private State currentState = State.IDLE;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private LocalTime breakStartTime;
    private long totalWorkSeconds = 0;
    private long totalBreakSeconds = 0;
    private long currentSessionStart = 0;
    private List<String> breakSessions = new ArrayList<>();
    
    public void startWork() {
        currentState = State.WORKING;
        workStartTime = LocalTime.now();
        currentSessionStart = System.currentTimeMillis();
        totalWorkSeconds = 0;
        totalBreakSeconds = 0;
        breakSessions.clear();
    }
    
    public void startBreak() {
        if (currentState != State.WORKING) return;
        
        totalWorkSeconds += (System.currentTimeMillis() - currentSessionStart) / 1000;
        currentState = State.ON_BREAK;
        breakStartTime = LocalTime.now();
        currentSessionStart = System.currentTimeMillis();
    }
    
    public void resumeWork() {
        if (currentState != State.ON_BREAK) return;
        
        totalBreakSeconds += (System.currentTimeMillis() - currentSessionStart) / 1000;
        LocalTime breakEndTime = LocalTime.now();
        breakSessions.add(String.format("[%s - %s]", 
            breakStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            breakEndTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        
        currentState = State.WORKING;
        currentSessionStart = System.currentTimeMillis();
    }
    
    public void stopWork() {
        if (currentState == State.IDLE) return;
        
        workEndTime = LocalTime.now();
        
        if (currentState == State.WORKING) {
            totalWorkSeconds += (System.currentTimeMillis() - currentSessionStart) / 1000;
        } else if (currentState == State.ON_BREAK) {
            totalBreakSeconds += (System.currentTimeMillis() - currentSessionStart) / 1000;
            LocalTime breakEndTime = LocalTime.now();
            breakSessions.add(String.format("[%s - %s]", 
                breakStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                breakEndTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        }
        
        currentState = State.IDLE;
    }
    
    public long getCurrentElapsedSeconds() {
        if (currentState == State.IDLE) return 0;
        return (System.currentTimeMillis() - currentSessionStart) / 1000;
    }
    
    public void restoreState(State state, LocalTime startTime, long workSecs, long breakSecs, long elapsedSecs) {
        this.currentState = state;
        this.workStartTime = startTime;
        this.totalWorkSeconds = workSecs;
        this.totalBreakSeconds = breakSecs;
        this.currentSessionStart = System.currentTimeMillis() - (elapsedSecs * 1000);
        this.breakSessions.clear();
    }
    
    public void addBreakSession(String breakSession) {
        this.breakSessions.add(breakSession);
    }
    
    // Getters
    public State getCurrentState() { return currentState; }
    public LocalTime getWorkStartTime() { return workStartTime; }
    public LocalTime getWorkEndTime() { return workEndTime; }
    public long getTotalWorkSeconds() { return totalWorkSeconds; }
    public long getTotalBreakSeconds() { return totalBreakSeconds; }
    public List<String> getBreakSessions() { return new ArrayList<>(breakSessions); }
}