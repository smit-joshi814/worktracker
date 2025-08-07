package com.worktracker.data;

import com.worktracker.core.WorkSession;
import com.worktracker.utils.TimeUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String LOG_FILE = "work-log.csv";

    public void logSession(WorkSession session) {
        try {
            boolean fileExists = Files.exists(Paths.get(LOG_FILE));
            
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                if (!fileExists) {
                    writer.write("Date,Start Time,End Time,Total Work Time,Total Break Time,Break Sessions\n");
                }

                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String startTime = session.getWorkStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String endTime = session.getWorkEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String workTime = TimeUtils.formatDuration(session.getTotalWorkSeconds());
                String breakTime = TimeUtils.formatDuration(session.getTotalBreakSeconds());
                String breaks = String.join("; ", session.getBreakSessions());

                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        date, startTime, endTime, workTime, breakTime, breaks));
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving log: " + e.getMessage());
        }
    }

    public List<Object[]> getHistoryData() {
        List<Object[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length >= 5) {
                    String breakSessions = parts.length > 5 ? parts[5] : "";
                    // Add day name to date display
                    LocalDate date = LocalDate.parse(parts[0]);
                    String dateWithDay = TimeUtils.formatDateWithDay(date);
                    rows.add(new Object[] { dateWithDay, parts[1], parts[2], parts[3], parts[4], breakSessions });
                }
            }
        } catch (IOException e) {
            // File doesn't exist or error reading
        }
        return rows;
    }

    public long calculateWorkHours(LocalDate startDate, LocalDate endDate) {
        long totalSeconds = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    LocalDate logDate = LocalDate.parse(parts[0]);
                    if (!logDate.isBefore(startDate) && !logDate.isAfter(endDate)) {
                        totalSeconds += TimeUtils.parseDuration(parts[3]);
                    }
                }
            }
        } catch (IOException e) {
            // File doesn't exist or error reading
        }
        return totalSeconds;
    }

    public long calculateDailyWorkHours() {
        return calculateWorkHours(LocalDate.now(), LocalDate.now());
    }

    public long calculateWeeklyWorkHours() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return calculateWorkHours(monday, today.minusDays(1));
    }

    public int countDailySessions(LocalDate date) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && LocalDate.parse(parts[0]).equals(date)) {
                    count++;
                }
            }
        } catch (IOException e) {
        }
        return count;
    }

    public long countTotalSessions() {
        File file = new File(LOG_FILE);
        if (!file.exists())
            return 0;

        long count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // skip header
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace(); // Optional: log it or handle as needed
        }
        return count;
    }

    public Map<String, Long> calculateMonthlyStats() {
        Map<String, Long> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = currentMonthStart.minusDays(1);

        stats.put("current", calculateWorkHours(currentMonthStart, today));
        stats.put("previous", calculateWorkHours(previousMonthStart, previousMonthEnd));

        return stats;
    }
}