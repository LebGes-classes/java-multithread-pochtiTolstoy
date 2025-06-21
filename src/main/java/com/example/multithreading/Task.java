package com.example.multithreading;

public class Task {
    private String name;
    private int totalMinutes;
    private int remainingMinutes;
    private int timeSpentMinutes; // Время, фактически потраченное на задачу
    private TaskStatus status;
    private Employee assignedTo;

    public enum TaskStatus {
        NEW, IN_PROGRESS, COMPLETED
    }

    public Task(String name, int totalHours) {
        this.name = name;
        this.totalMinutes = totalHours * 60; // Конвертируем часы в минуты
        this.remainingMinutes = this.totalMinutes;
        this.timeSpentMinutes = 0;
        this.status = TaskStatus.NEW;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; }

    public int getRemainingMinutes() { return remainingMinutes; }
    public void setRemainingMinutes(int remainingMinutes) { this.remainingMinutes = remainingMinutes; }

    public int getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }

    // Для обратной совместимости
    public int getTotalHours() { return totalMinutes / 60; }
    public int getRemainingHours() { return remainingMinutes / 60; }
    public int getTimeSpentHours() { return timeSpentMinutes / 60; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Employee getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Employee assignedTo) { this.assignedTo = assignedTo; }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public void workOn(int minutes) {
        if (status == TaskStatus.COMPLETED) return;
        
        status = TaskStatus.IN_PROGRESS;
        remainingMinutes = Math.max(0, remainingMinutes - minutes);
        timeSpentMinutes += minutes; // Учитываем потраченное время
        
        if (remainingMinutes == 0) {
            status = TaskStatus.COMPLETED;
        }
    }

    public String getTimeString(int minutes) {
        if (minutes >= 60) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins == 0) {
                return hours + " ч.";
            } else {
                return hours + " ч. " + mins + " мин.";
            }
        } else {
            return minutes + " мин.";
        }
    }

    @Override
    public String toString() {
        return String.format("Task{name='%s', total=%s, remaining=%s, spent=%s, status=%s}", 
                           name, getTimeString(totalMinutes), getTimeString(remainingMinutes), 
                           getTimeString(timeSpentMinutes), status);
    }
} 