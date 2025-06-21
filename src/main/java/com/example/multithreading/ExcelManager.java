package com.example.multithreading;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelManager {
    
    public static class LoadedData {
        private List<Employee> employees;
        private List<Task> tasks;
        private Map<Integer, List<Integer>> assignments; // employeeId -> taskIds
        
        public LoadedData(List<Employee> employees, List<Task> tasks, Map<Integer, List<Integer>> assignments) {
            this.employees = employees;
            this.tasks = tasks;
            this.assignments = assignments;
        }
        
        public List<Employee> getEmployees() { return employees; }
        public List<Task> getTasks() { return tasks; }
        public Map<Integer, List<Integer>> getAssignments() { return assignments; }
    }
    
    public static LoadedData loadData(String filename) {
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // Загружаем сотрудников
            List<Employee> employees = loadEmployees(workbook);
            
            // Загружаем задачи
            List<Task> tasks = loadTasks(workbook);
            
            // Загружаем назначения
            Map<Integer, List<Integer>> assignments = loadAssignments(workbook);
            
            // Назначаем задачи сотрудникам
            assignTasksToEmployees(employees, tasks, assignments);
            
            System.out.println("✓ Загружено " + employees.size() + " сотрудников");
            System.out.println("✓ Загружено " + tasks.size() + " задач");
            System.out.println("✓ Создано " + assignments.size() + " назначений");
            
            return new LoadedData(employees, tasks, assignments);
            
        } catch (IOException e) {
            System.err.println("✗ Ошибка при загрузке файла " + filename + ": " + e.getMessage());
            throw new RuntimeException("Не удалось загрузить данные из Excel", e);
        }
    }
    
    private static List<Employee> loadEmployees(Workbook workbook) {
        List<Employee> employees = new ArrayList<>();
        Sheet sheet = workbook.getSheet("Сотрудники");
        
        if (sheet == null) {
            throw new RuntimeException("Лист 'Сотрудники' не найден в файле");
        }
        
        // Пропускаем заголовок (первая строка)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                int id = (int) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                String position = row.getCell(2).getStringCellValue();
                
                Employee employee = new Employee(name);
                employees.add(employee);
                
            } catch (Exception e) {
                System.err.println("Ошибка при загрузке сотрудника из строки " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        return employees;
    }
    
    private static List<Task> loadTasks(Workbook workbook) {
        List<Task> tasks = new ArrayList<>();
        Sheet sheet = workbook.getSheet("Задачи");
        
        if (sheet == null) {
            throw new RuntimeException("Лист 'Задачи' не найден в файле");
        }
        
        // Пропускаем заголовок (первая строка)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                int id = (int) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                int duration = (int) row.getCell(2).getNumericCellValue();
                String status = row.getCell(3).getStringCellValue();
                
                Task task = new Task(name, duration);
                tasks.add(task);
                
            } catch (Exception e) {
                System.err.println("Ошибка при загрузке задачи из строки " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        return tasks;
    }
    
    private static Map<Integer, List<Integer>> loadAssignments(Workbook workbook) {
        Map<Integer, List<Integer>> assignments = new HashMap<>();
        Sheet sheet = workbook.getSheet("Назначения");
        
        if (sheet == null) {
            throw new RuntimeException("Лист 'Назначения' не найден в файле");
        }
        
        // Пропускаем заголовок (первая строка)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                int assignmentId = (int) row.getCell(0).getNumericCellValue();
                int employeeId = (int) row.getCell(1).getNumericCellValue();
                int taskId = (int) row.getCell(2).getNumericCellValue();
                
                // Добавляем назначение
                assignments.computeIfAbsent(employeeId, k -> new ArrayList<>()).add(taskId);
                
            } catch (Exception e) {
                System.err.println("Ошибка при загрузке назначения из строки " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        return assignments;
    }
    
    private static void assignTasksToEmployees(List<Employee> employees, List<Task> tasks, 
                                             Map<Integer, List<Integer>> assignments) {
        // Назначаем задачи сотрудникам
        for (Map.Entry<Integer, List<Integer>> entry : assignments.entrySet()) {
            int employeeId = entry.getKey();
            List<Integer> taskIds = entry.getValue();
            
            if (employeeId > 0 && employeeId <= employees.size()) {
                Employee employee = employees.get(employeeId - 1); // ID начинается с 1
                
                for (Integer taskId : taskIds) {
                    if (taskId > 0 && taskId <= tasks.size()) {
                        Task task = tasks.get(taskId - 1); // ID начинается с 1
                        employee.addTask(task);
                    }
                }
            }
        }
    }
    
    public static void saveStatistics(String filename, List<Employee> employees) {
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // Удаляем существующий лист статистики, если он есть
            int sheetIndex = workbook.getSheetIndex("Статистика");
            if (sheetIndex != -1) {
                workbook.removeSheetAt(sheetIndex);
            }
            
            // Создаем новый лист статистики
            Sheet statsSheet = workbook.createSheet("Статистика");
            createStatisticsSheet(statsSheet, employees);
            
            // Сохраняем файл
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
            
            System.out.println("✓ Статистика сохранена в файл " + filename);
            
        } catch (IOException e) {
            System.err.println("✗ Ошибка при сохранении статистики: " + e.getMessage());
            throw new RuntimeException("Не удалось сохранить статистику", e);
        }
    }
    
    public static void saveStatisticsToSheet(String filename, List<Employee> employees, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // Удаляем существующий лист, если он есть
            int sheetIndex = workbook.getSheetIndex(sheetName);
            if (sheetIndex != -1) {
                workbook.removeSheetAt(sheetIndex);
            }
            
            // Создаем новый лист статистики
            Sheet statsSheet = workbook.createSheet(sheetName);
            createStatisticsSheet(statsSheet, employees);
            
            // Сохраняем файл
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
            
        } catch (IOException e) {
            System.err.println("✗ Ошибка при сохранении статистики в лист " + sheetName + ": " + e.getMessage());
            throw new RuntimeException("Не удалось сохранить статистику", e);
        }
    }
    
    private static void createStatisticsSheet(Sheet sheet, List<Employee> employees) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Имя сотрудника", "Всего задач", "Выполнено", 
            "Время на задачи", "Время нерабочее", "Эффективность (%)"
        };
        
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Заполняем данные по сотрудникам
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(emp.getName());
            row.createCell(1).setCellValue(emp.getTotalTasksCount());
            row.createCell(2).setCellValue(emp.getCompletedTasksCount());
            row.createCell(3).setCellValue(emp.getTimeString(emp.getTotalTaskTimeMinutes()));
            row.createCell(4).setCellValue(emp.getTimeString(emp.getTotalNonWorkingTimeMinutes()));
            row.createCell(5).setCellValue(String.format("%.1f", emp.getEfficiency()));
        }
        
        // Добавляем итоговую строку
        Row totalRow = sheet.createRow(employees.size() + 1);
        CellStyle totalStyle = createTotalStyle(sheet.getWorkbook());
        
        totalRow.createCell(0).setCellValue("ИТОГО");
        totalRow.getCell(0).setCellStyle(totalStyle);
        
        // Считаем общие показатели
        int totalTasks = employees.stream().mapToInt(Employee::getTotalTasksCount).sum();
        int completedTasks = employees.stream().mapToInt(Employee::getCompletedTasksCount).sum();
        long totalTaskTime = employees.stream().mapToLong(Employee::getTotalTaskTimeMinutes).sum();
        long totalNonWorkingTime = employees.stream().mapToLong(Employee::getTotalNonWorkingTimeMinutes).sum();
        double avgEfficiency = employees.stream().mapToDouble(Employee::getEfficiency).average().orElse(0.0);
        
        totalRow.createCell(1).setCellValue(totalTasks);
        totalRow.createCell(2).setCellValue(completedTasks);
        totalRow.createCell(3).setCellValue(formatTime(totalTaskTime));
        totalRow.createCell(4).setCellValue(formatTime(totalNonWorkingTime));
        totalRow.createCell(5).setCellValue(String.format("%.1f", avgEfficiency));
        
        // Применяем стиль к итоговой строке
        for (int i = 1; i < headers.length; i++) {
            totalRow.getCell(i).setCellStyle(totalStyle);
        }
        
        // Автоматически подгоняем ширину столбцов
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private static String formatTime(long minutes) {
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
} 