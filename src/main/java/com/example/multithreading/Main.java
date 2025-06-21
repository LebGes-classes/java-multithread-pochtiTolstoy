package com.example.multithreading;

import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Симуляция рабочего дня ===");
        
        String filename = "work_data.xlsx";
        if (args.length > 0) {
            filename = args[0];
        }
        
        try {
            // Загружаем данные из Excel файла
            System.out.println("Загрузка данных из файла: " + filename);
            ExcelManager.LoadedData data = ExcelManager.loadData(filename);
            
            // Создаем рабочий день
            WorkDay workDay = new WorkDay();
            for (Employee emp : data.getEmployees()) {
                workDay.addEmployee(emp);
            }
            
            // Запускаем многодневную симуляцию
            MultiDaySimulator simulator = new MultiDaySimulator(filename, data.getEmployees());
            simulator.simulateUntilCompletion();
            
            // Выводим финальные результаты
            printFinalResults(data.getEmployees());
            
        } catch (Exception e) {
            System.err.println("Ошибка при запуске симуляции: " + e.getMessage());
            System.err.println("Убедитесь, что файл " + filename + " существует и создан с помощью 'make generate'");
            e.printStackTrace();
        }
    }
    
    private static void simulateWorkDay(WorkDay workDay) {
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
    
    private static void printResults(WorkDay workDay) {
        System.out.println("\n=== РЕЗУЛЬТАТЫ РАБОЧЕГО ДНЯ ===");
        
        for (Employee emp : workDay.getEmployees()) {
            System.out.println("\n" + emp.getName() + ":");
            System.out.println("  Всего задач: " + emp.getTotalTasksCount());
            System.out.println("  Выполнено: " + emp.getCompletedTasksCount());
            System.out.println("  Время работы: " + emp.getTimeString(emp.getTotalWorkTimeMinutes()));
            System.out.println("  Время на задачи: " + emp.getTimeString(emp.getTotalTaskTimeMinutes()));
            System.out.println("  Время нерабочее: " + emp.getTimeString(emp.getTotalNonWorkingTimeMinutes()));
            System.out.println("  Эффективность: " + String.format("%.1f", emp.getEfficiency()) + "%");
            
            // Проверяем, что сумма равна 8 часам
            long totalMinutes = emp.getTotalTaskTimeMinutes() + emp.getTotalNonWorkingTimeMinutes();
            System.out.println("  Проверка: " + emp.getTimeString(emp.getTotalTaskTimeMinutes()) + " + " + 
                             emp.getTimeString(emp.getTotalNonWorkingTimeMinutes()) + " = " + 
                             emp.getTimeString(totalMinutes) + " (должно быть 8 ч.)");
            
            System.out.println("  Задачи:");
            for (Task task : emp.getTasks()) {
                System.out.println("    - " + task.getName() + ": " + 
                                 task.getStatus() + " (потрачено " + task.getTimeString(task.getTimeSpentMinutes()) + 
                                 " из " + task.getTimeString(task.getTotalMinutes()) + ")");
            }
        }
    }
    
    private static void printFinalResults(List<Employee> employees) {
        System.out.println("\n=== ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ ===");
        
        for (Employee emp : employees) {
            System.out.println("\n" + emp.getName() + ":");
            System.out.println("  Всего задач: " + emp.getTotalTasksCount());
            System.out.println("  Выполнено: " + emp.getCompletedTasksCount());
            System.out.println("  Время на задачи (всего): " + emp.getTimeString(emp.getTotalTaskTimeAllDays()));
            System.out.println("  Время нерабочее (всего): " + emp.getTimeString(emp.getTotalNonWorkingTimeAllDays()));
            System.out.println("  Эффективность (всего): " + String.format("%.1f", emp.getTotalEfficiencyAllDays()) + "%");
            long totalMinutes = emp.getTotalTaskTimeAllDays() + emp.getTotalNonWorkingTimeAllDays();
            System.out.println("  Проверка: " + emp.getTimeString(emp.getTotalTaskTimeAllDays()) + " + " + 
                             emp.getTimeString(emp.getTotalNonWorkingTimeAllDays()) + " = " + 
                             emp.getTimeString(totalMinutes) + " (всего за все дни)");
            System.out.println("  Задачи:");
            for (Task task : emp.getTasks()) {
                System.out.println("    - " + task.getName() + ": " + 
                                 task.getStatus() + " (потрачено " + task.getTimeString(task.getTimeSpentMinutes()) + 
                                 " из " + task.getTimeString(task.getTotalMinutes()) + ")");
            }
        }
    }
} 