package com.example.multithreading;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExcelGenerator {
    private static final String[] EMPLOYEE_NAMES = {
        "Иван Петров", "Мария Сидорова", "Петр Иванов", "Анна Козлова", 
        "Сергей Волков", "Елена Морозова", "Дмитрий Соколов", "Ольга Лебедева"
    };
    
    private static final String[] TASK_NAMES = {
        "Разработка модуля авторизации", "Тестирование API", "Документация проекта",
        "Код-ревью", "Оптимизация базы данных", "Исправление багов", 
        "Интеграция с внешними сервисами", "Настройка CI/CD", "Рефакторинг кода",
        "Создание тестов", "Анализ производительности", "Обновление зависимостей"
    };

    public static void generateInitialData(String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Создаем лист с сотрудниками
            Sheet employeesSheet = workbook.createSheet("Сотрудники");
            createEmployeesSheet(employeesSheet);
            
            // Создаем лист с задачами
            Sheet tasksSheet = workbook.createSheet("Задачи");
            createTasksSheet(tasksSheet);
            
            // Создаем лист с назначениями
            Sheet assignmentsSheet = workbook.createSheet("Назначения");
            createAssignmentsSheet(assignmentsSheet);
            
            // Сохраняем файл
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
            
            System.out.println("Файл " + filename + " успешно создан!");
            
        } catch (IOException e) {
            System.err.println("Ошибка при создании файла: " + e.getMessage());
        }
    }

    private static void createEmployeesSheet(Sheet sheet) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Имя", "Должность"};
        
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Создаем данные сотрудников
        Random random = new Random();
        String[] positions = {"Разработчик", "Тестировщик", "Аналитик", "DevOps"};
        
        for (int i = 0; i < EMPLOYEE_NAMES.length; i++) {
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(i + 1); // ID
            row.createCell(1).setCellValue(EMPLOYEE_NAMES[i]); // Имя
            row.createCell(2).setCellValue(positions[random.nextInt(positions.length)]); // Должность
        }
        
        // Автоматически подгоняем ширину столбцов
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createTasksSheet(Sheet sheet) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Название", "Длительность (часы)", "Статус"};
        
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Создаем данные задач
        Random random = new Random();
        String[] statuses = {"NEW", "IN_PROGRESS", "COMPLETED"};
        
        for (int i = 0; i < TASK_NAMES.length; i++) {
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(i + 1); // ID
            row.createCell(1).setCellValue(TASK_NAMES[i]); // Название
            row.createCell(2).setCellValue(random.nextInt(15) + 1); // Длительность 1-16 часов
            row.createCell(3).setCellValue(statuses[0]); // Статус (все NEW)
        }
        
        // Автоматически подгоняем ширину столбцов
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createAssignmentsSheet(Sheet sheet) {
        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "ID_Сотрудника", "ID_Задачи", "Дата_Назначения"};
        
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Создаем назначения (каждая задача назначается только одному сотруднику)
        Random random = new Random();
        int assignmentId = 1;
        
        // Создаем список всех задач
        List<Integer> availableTasks = new ArrayList<>();
        for (int i = 1; i <= TASK_NAMES.length; i++) {
            availableTasks.add(i);
        }
        
        // Распределяем задачи между сотрудниками
        for (int employeeId = 1; employeeId <= EMPLOYEE_NAMES.length; employeeId++) {
            int tasksCount = random.nextInt(2) + 2; // 2-3 задачи на сотрудника
            
            for (int j = 0; j < tasksCount && !availableTasks.isEmpty(); j++) {
                // Выбираем случайную задачу из доступных
                int taskIndex = random.nextInt(availableTasks.size());
                int taskId = availableTasks.remove(taskIndex);
                
                Row row = sheet.createRow(assignmentId);
                
                row.createCell(0).setCellValue(assignmentId); // ID назначения
                row.createCell(1).setCellValue(employeeId); // ID сотрудника
                row.createCell(2).setCellValue(taskId); // ID задачи
                row.createCell(3).setCellValue("2025-03-20"); // Дата назначения
                
                assignmentId++;
            }
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

    public static void main(String[] args) {
        String filename = "work_data.xlsx";
        if (args.length > 0) filename = args[0];
        generateInitialData(filename);
    }
} 