package com.example.multithreading;

import java.util.Random;

public class Break {
    private String name;
    private int durationMinutes;
    private BreakType type;
    private boolean isActive;

    public enum BreakType {
        COFFEE_BREAK("Кофе-брейк", 15, 30),
        LUNCH("Обед", 60, 60),
        MEETING("Встреча", 30, 60),
        TECHNICAL_ISSUE("Техническая проблема", 10, 45);

        private final String displayName;
        private final int minMinutes;
        private final int maxMinutes;

        BreakType(String displayName, int minMinutes, int maxMinutes) {
            this.displayName = displayName;
            this.minMinutes = minMinutes;
            this.maxMinutes = maxMinutes;
        }

        public String getDisplayName() { return displayName; }
        public int getMinMinutes() { return minMinutes; }
        public int getMaxMinutes() { return maxMinutes; }

        public int getRandomDuration() {
            Random random = new Random();
            return random.nextInt(maxMinutes - minMinutes + 1) + minMinutes;
        }
    }

    public Break(BreakType type) {
        this.type = type;
        this.name = type.getDisplayName();
        this.durationMinutes = type.getRandomDuration();
        this.isActive = false;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public BreakType getType() { return type; }
    public void setType(BreakType type) { this.type = type; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getDurationString() {
        if (durationMinutes >= 60) {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            if (minutes == 0) {
                return hours + " ч.";
            } else {
                return hours + " ч. " + minutes + " мин.";
            }
        } else {
            return durationMinutes + " мин.";
        }
    }

    @Override
    public String toString() {
        return String.format("Break{name='%s', duration=%s, type=%s, active=%s}", 
                           name, getDurationString(), type, isActive);
    }
} 