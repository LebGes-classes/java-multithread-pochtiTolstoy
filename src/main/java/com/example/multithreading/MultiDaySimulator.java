package com.example.multithreading;

import java.util.List;
import java.util.ArrayList;

public class MultiDaySimulator {
    private String filename;
    private List<Employee> employees;
    private int currentDay = 1;
    
    public MultiDaySimulator(String filename, List<Employee> employees) {
        this.filename = filename;
        this.employees = employees;
    }
    
    public void simulateUntilCompletion() {
        System.out.println("=== МНОГОДНЕВНАЯ СИМУЛЯЦИЯ ===");
        System.out.println("Цель: выполнить все задачи");
        
        while (!allTasksCompleted()) {
            System.out.println("\n--- ДЕНЬ " + currentDay + " ---");
            
            // Сбрасываем дневную статистику (но сохраняем прогресс задач)
            for (Employee emp : employees) {
                emp.resetDailyStats();
            }
            
            // Создаем рабочий день
            WorkDay workDay = new WorkDay();
            for (Employee emp : employees) {
                workDay.addEmployee(emp);
            }
            
            // Симулируем один день
            simulateWorkDay(workDay);
            
            // Выводим результаты дня
            printDayResults(workDay);
            
            // Сохраняем статистику дня
            saveDayStatistics();
            
            // Показываем прогресс
            printProgress();
            
            currentDay++;
            
            // Небольшая пауза между днями
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("\n=== ВСЕ ЗАДАЧИ ВЫПОЛНЕНЫ! ===");
        System.out.println("Потребовалось дней: " + (currentDay - 1));
    }
    
    private boolean allTasksCompleted() {
        return employees.stream()
                .allMatch(emp -> emp.getTasks().stream()
                        .allMatch(Task::isCompleted));
    }
    
    private void simulateWorkDay(WorkDay workDay) {
        workDay.start();
        
        // Создаем потоки для сотрудников
        List<Thread> workerThreads = new ArrayList<>();
        List<EmployeeWorker> workers = new ArrayList<>();
        
        for (Employee emp : workDay.getEmployees()) {
            EmployeeWorker worker = new EmployeeWorker(emp, workDay);
            Thread thread = new Thread(worker);
            workers.add(worker);
            workerThreads.add(thread);
            thread.start();
        }
        
        // Симулируем прохождение часов
        while (!workDay.isDayComplete()) {
            workDay.advanceHour();
            
            // Ждем немного для симуляции
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Останавливаем все потоки
        for (EmployeeWorker worker : workers) {
            worker.stop();
        }
        
        // Ждем завершения всех потоков
        for (Thread thread : workerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Завершаем рабочий день для всех сотрудников
        for (Employee emp : workDay.getEmployees()) {
            emp.finalizeWorkDay();
        }
    }
    
    private void printDayResults(WorkDay workDay) {
        System.out.println("\n--- РЕЗУЛЬТАТЫ ДНЯ " + currentDay + " ---");
        
        for (Employee emp : workDay.getEmployees()) {
            System.out.println(emp.getName() + ":");
            System.out.println("  Выполнено задач: " + emp.getCompletedTasksCount() + "/" + emp.getTotalTasksCount());
            System.out.println("  Время на задачи: " + emp.getTimeString(emp.getTotalTaskTimeMinutes()));
            System.out.println("  Эффективность: " + String.format("%.1f", emp.getEfficiency()) + "%");
        }
    }
    
    private void saveDayStatistics() {
        try {
            // Создаем имя листа с номером дня
            String sheetName = "Статистика_День_" + currentDay;
            ExcelManager.saveStatisticsToSheet(filename, employees, sheetName);
            System.out.println("✓ Статистика дня " + currentDay + " сохранена");
        } catch (Exception e) {
            System.err.println("✗ Ошибка при сохранении статистики дня " + currentDay + ": " + e.getMessage());
        }
    }
    
    private void printProgress() {
        int totalTasks = employees.stream().mapToInt(Employee::getTotalTasksCount).sum();
        int completedTasks = employees.stream().mapToInt(Employee::getCompletedTasksCount).sum();
        double progress = (double) completedTasks / totalTasks * 100.0;
        
        System.out.println("\n📊 ПРОГРЕСС: " + completedTasks + "/" + totalTasks + " задач (" + 
                          String.format("%.1f", progress) + "%)");
    }
} 