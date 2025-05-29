package com.example.bugtaw.data;

public class Alarm {
    private long id;
    private int hour;
    private int minute;
    private String days;
    private String puzzleType;
    private boolean enabled;
    private String label;
    private String sound;

    public Alarm(long id, int hour, int minute, String days, String puzzleType, boolean enabled, String label, String sound) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
        this.puzzleType = puzzleType;
        this.enabled = enabled;
        this.label = label;
        this.sound = sound;
    }

    // Getters
    public long getId() { return id; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public String getDays() { return days; }
    public String getPuzzleType() { return puzzleType; }
    public boolean isEnabled() { return enabled; }
    public String getLabel() { return label; }
    public String getSound() { return sound; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setHour(int hour) { this.hour = hour; }
    public void setMinute(int minute) { this.minute = minute; }
    public void setDays(String days) { this.days = days; }
    public void setPuzzleType(String puzzleType) { this.puzzleType = puzzleType; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Helper methods
    public String getTimeString() {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = hour < 12 ? "AM" : "PM";
        return String.format("%d:%02d %s", displayHour, minute, amPm);
    }

    public boolean isScheduledForDay(int dayOfWeek) {
        return days.contains(String.valueOf(dayOfWeek));
    }


}