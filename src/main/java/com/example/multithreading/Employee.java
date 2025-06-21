package com.example.multithreading;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Employee {
    private String name;
    private List<Task> tasks;
    private static final long WORK_DAY_MINUTES = 8 * 60; // 8 часов в минутах
    private long totalTaskTimeMinutes;      // время на задачи (в минутах)
    private long totalNonWorkingTimeMinutes; // время простоя + перерывы (в минутах)
    private volatile boolean isWorking;
    private Break currentBreak;
    private Random random;
    private long totalTaskTimeAllDays = 0;
    private long totalNonWorkingTimeAllDays = 0;

    public Employee(String name) {
        this.name = name;
        this.tasks = new ArrayList<>();
        this.totalTaskTimeMinutes = 0;
        this.totalNonWorkingTimeMinutes = 0;
        this.isWorking = false;
        this.currentBreak = null;
        this.random = new Random();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public long getTotalWorkTimeMinutes() { return WORK_DAY_MINUTES; }
    public long getTotalTaskTimeMinutes() { return totalTaskTimeMinutes; }
    public void setTotalTaskTimeMinutes(long totalTaskTimeMinutes) { this.totalTaskTimeMinutes = totalTaskTimeMinutes; }

    public long getTotalNonWorkingTimeMinutes() { return totalNonWorkingTimeMinutes; }
    public void setTotalNonWorkingTimeMinutes(long totalNonWorkingTimeMinutes) { this.totalNonWorkingTimeMinutes = totalNonWorkingTimeMinutes; }

    public boolean isWorking() { return isWorking; }
    public void setWorking(boolean working) { isWorking = working; }

    public Break getCurrentBreak() { return currentBreak; }
    public void setCurrentBreak(Break currentBreak) { this.currentBreak = currentBreak; }

    // Convert minutes to hours for display
    public long getTotalWorkTimeHours() { return WORK_DAY_MINUTES / 60; }
    public long getTotalTaskTimeHours() { return totalTaskTimeMinutes / 60; }
    public long getTotalNonWorkingTimeHours() { return totalNonWorkingTimeMinutes / 60; }

    // Business methods
    public void addTask(Task task) {
        synchronized (tasks) {
            tasks.add(task);
            task.setAssignedTo(this);
        }
    }

    public Task getNextTask() {
        synchronized (tasks) {
            return tasks.stream()
                    .filter(task -> !task.isCompleted())
                    .findFirst()
                    .orElse(null);
        }
    }

    public void workOnTask(Task task, int minutes) {
        if (task != null && task.getAssignedTo() == this) {
            // Проверяем, не превышаем ли лимит рабочего дня
            if (totalTaskTimeMinutes + totalNonWorkingTimeMinutes + minutes <= WORK_DAY_MINUTES) {
                task.workOn(minutes);
                totalTaskTimeMinutes += minutes;
            } else {
                // Если превышаем лимит, работаем только оставшееся время
                int remainingMinutes = (int) (WORK_DAY_MINUTES - totalTaskTimeMinutes - totalNonWorkingTimeMinutes);
                if (remainingMinutes > 0) {
                    task.workOn(remainingMinutes);
                    totalTaskTimeMinutes += remainingMinutes;
                }
            }
        }
    }

    public void addNonWorkingTime(int minutes) {
        // Проверяем, не превышаем ли лимит рабочего дня
        if (totalTaskTimeMinutes + totalNonWorkingTimeMinutes + minutes <= WORK_DAY_MINUTES) {
            totalNonWorkingTimeMinutes += minutes;
        } else {
            // Если превышаем лимит, добавляем только оставшееся время
            int remainingMinutes = (int) (WORK_DAY_MINUTES - totalTaskTimeMinutes - totalNonWorkingTimeMinutes);
            if (remainingMinutes > 0) {
                totalNonWorkingTimeMinutes += remainingMinutes;
            }
        }
    }

    // Метод для завершения рабочего дня - распределяем оставшееся время
    public void finalizeWorkDay() {
        long remainingMinutes = WORK_DAY_MINUTES - totalTaskTimeMinutes - totalNonWorkingTimeMinutes;
        if (remainingMinutes > 0) {
            totalNonWorkingTimeMinutes += remainingMinutes;
        }
        // Накопление итоговой статистики
        totalTaskTimeAllDays += totalTaskTimeMinutes;
        totalNonWorkingTimeAllDays += totalNonWorkingTimeMinutes;
    }

    // Метод для сброса дневной статистики (для многодневной симуляции)
    public void resetDailyStats() {
        totalTaskTimeMinutes = 0;
        totalNonWorkingTimeMinutes = 0;
        // НЕ сбрасываем прогресс задач - они должны сохраняться между днями
    }

    public double getEfficiency() {
        if (WORK_DAY_MINUTES == 0) return 0.0;
        return (double) totalTaskTimeMinutes / WORK_DAY_MINUTES * 100.0;
    }

    public int getCompletedTasksCount() {
        synchronized (tasks) {
            return (int) tasks.stream().filter(Task::isCompleted).count();
        }
    }

    public int getTotalTasksCount() {
        synchronized (tasks) {
            return tasks.size();
        }
    }

    // Break management
    public boolean shouldTakeBreak() {
        // 10% шанс взять перерыв каждый час
        return random.nextInt(100) < 10;
    }

    public Break generateRandomBreak() {
        Break.BreakType[] types = Break.BreakType.values();
        Break.BreakType randomType = types[random.nextInt(types.length)];
        return new Break(randomType);
    }

    public boolean isOnBreak() {
        return currentBreak != null && currentBreak.isActive();
    }

    public void startBreak(Break break_) {
        this.currentBreak = break_;
        break_.setActive(true);
        System.out.println(name + " ушел на " + break_.getName() + " (" + break_.getDurationString() + ")");
    }

    public void endBreak() {
        if (currentBreak != null && currentBreak.isActive()) {
            addNonWorkingTime(currentBreak.getDurationMinutes());
            System.out.println(name + " вернулся с " + currentBreak.getName());
            currentBreak.setActive(false);
            currentBreak = null;
        }
    }

    public String getTimeString(long minutes) {
        if (minutes >= 60) {
            long hours = minutes / 60;
            long mins = minutes % 60;
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
        return String.format("Employee{name='%s', tasks=%d, workTime=%s, taskTime=%s, nonWorkingTime=%s, efficiency=%.1f%%}", 
                           name, tasks.size(), getTimeString(WORK_DAY_MINUTES), 
                           getTimeString(totalTaskTimeMinutes), getTimeString(totalNonWorkingTimeMinutes), getEfficiency());
    }

    public long getTotalTaskTimeAllDays() { return totalTaskTimeAllDays; }
    public long getTotalNonWorkingTimeAllDays() { return totalNonWorkingTimeAllDays; }
    public double getTotalEfficiencyAllDays() {
        long total = totalTaskTimeAllDays + totalNonWorkingTimeAllDays;
        if (total == 0) return 0.0;
        return (double) totalTaskTimeAllDays / total * 100.0;
    }
} 