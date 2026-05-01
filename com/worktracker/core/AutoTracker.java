package com.worktracker.core;

import java.awt.MouseInfo;
import java.awt.Point;

public class AutoTracker {
    private Point lastMousePosition = null;
    private long lastActivityTime = System.currentTimeMillis();
    private final long idleTimeoutMs;
    private final Runnable onActive;
    private final Runnable onIdle;

    public AutoTracker(long idleTimeoutMs, Runnable onActive, Runnable onIdle) {
        this.idleTimeoutMs = idleTimeoutMs;
        this.onActive = onActive;
        this.onIdle = onIdle;
    }

    public void check() {
        Point currentMousePosition = null;
        try {
            currentMousePosition = MouseInfo.getPointerInfo().getLocation();
        } catch (Exception e) {
            // Ignore headless or security exceptions gracefully
        }

        if (currentMousePosition != null) {
            if (lastMousePosition == null || !currentMousePosition.equals(lastMousePosition)) {
                lastActivityTime = System.currentTimeMillis();
                lastMousePosition = currentMousePosition;
            }
        }

        long timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime;

        if (timeSinceLastActivity < idleTimeoutMs) {
            onActive.run();
        } else {
            onIdle.run();
        }
    }
}