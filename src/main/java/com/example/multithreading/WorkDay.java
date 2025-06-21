package com.example.multithreading;

import java.util.List;
import java.util.ArrayList;

public class WorkDay {
    private static final int MAX_HOURS = 8;
    private List<Employee> employees;
    private volatile int currentHour;
    private volatile boolean isActive;

    public WorkDay() {
        this.employees = new ArrayList<>();
        this.currentHour = 0;
        this.isActive = false;
    }

    // Getters and setters
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }

    public int getCurrentHour() { return currentHour; }
    public void setCurrentHour(int currentHour) { this.currentHour = currentHour; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public static int getMaxHours() { return MAX_HOURS; }

    // Business methods
    public void addEmployee(Employee employee) {
        synchronized (employees) {
            employees.add(employee);
        }
    }

    public void start() {
        synchronized (this) {
            currentHour = 0;
            isActive = true;
            System.out.println("=== Рабочий день начался ===");
        }
    }

    public void end() {
        synchronized (this) {
            isActive = false;
            System.out.println("=== Рабочий день завершен ===");
        }
    }

    public boolean advanceHour() {
        synchronized (this) {
            if (!isActive) return false;
            
            if (currentHour >= MAX_HOURS) {
                end();
                return false;
            }
            
            currentHour++;
            System.out.println("--- Час " + currentHour + " ---");
            
            return true;
        }
    }

    public boolean isDayComplete() {
        return currentHour >= MAX_HOURS || !isActive;
    }

    public int getRemainingHours() {
        return Math.max(0, MAX_HOURS - currentHour);
    }

    @Override
    public String toString() {
        return String.format("WorkDay{currentHour=%d, isActive=%s, employees=%d}", 
                           currentHour, isActive, employees.size());
    }
} 