package com.example.bugtaw.data;

public class Alarm {
    private long id;
    private int hour;
    private int minute;
    private String days;
    private String puzzleType;
    private boolean enabled;

    public Alarm(long id, int hour, int minute, String days, String puzzleType, boolean enabled) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
        this.puzzleType = puzzleType;
        this.enabled = enabled;
    }

    // Getters
    public long getId() { return id; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public String getDays() { return days; }
    public String getPuzzleType() { return puzzleType; }
    public boolean isEnabled() { return enabled; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setHour(int hour) { this.hour = hour; }
    public void setMinute(int minute) { this.minute = minute; }
    public void setDays(String days) { this.days = days; }
    public void setPuzzleType(String puzzleType) { this.puzzleType = puzzleType; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Helper methods
    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }

    public boolean isScheduledForDay(int dayOfWeek) {
        return days.contains(String.valueOf(dayOfWeek));
    }
} 