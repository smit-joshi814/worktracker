package com.worktracker.utils;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class TimeUtils {
    
    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    public static long parseDuration(String duration) {
        String[] parts = duration.split(":");
        if (parts.length == 3) {
            return Long.parseLong(parts[0]) * 3600 + 
                   Long.parseLong(parts[1]) * 60 + 
                   Long.parseLong(parts[2]);
        }
        return 0;
    }
    
    public static String getDayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
    
    public static String getShortDayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }
    
    public static String formatDateWithDay(LocalDate date) {
        return getDayName(date) + ", " + date.toString();
    }
}