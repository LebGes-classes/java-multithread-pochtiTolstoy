package com.example.multithreading;

public class TestExcelGenerator {
    public static void main(String[] args) {
        System.out.println("=== Генерация тестовых данных ===");
        
        try {
            ExcelGenerator.generateInitialData("work_data.xlsx");
            System.out.println("✓ Файл work_data.xlsx успешно создан");
            System.out.println("✓ Содержит листы: Сотрудники, Задачи, Назначения");
            System.out.println("✓ Готов для использования в симуляции");
        } catch (Exception e) {
            System.err.println("✗ Ошибка при создании файла: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 