package com.example.multithreading;

public class EmployeeWorker implements Runnable {
    private Employee employee;
    private WorkDay workDay;
    private volatile boolean shouldStop;
    private int lastProcessedHour;
    private int timeSpentInCurrentHour; // Время, потраченное в текущем часу

    public EmployeeWorker(Employee employee, WorkDay workDay) {
        this.employee = employee;
        this.workDay = workDay;
        this.shouldStop = false;
        this.lastProcessedHour = 0;
        this.timeSpentInCurrentHour = 0;
    }

    @Override
    public void run() {
        System.out.println(employee.getName() + " начал работу");
        
        while (!shouldStop && workDay.isActive()) {
            // Ждем нового часа
            waitForNextHour();
            
            if (shouldStop || !workDay.isActive()) break;
            
            // Обрабатываем текущий час
            processCurrentHour();
        }
        
        System.out.println(employee.getName() + " завершил работу");
    }

    private void waitForNextHour() {
        while (!shouldStop && workDay.isActive() && 
               workDay.getCurrentHour() <= lastProcessedHour) {
            try {
                Thread.sleep(50); // Проверяем каждые 50мс
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                shouldStop = true;
                break;
            }
        }
    }

    private void processCurrentHour() {
        int currentHour = workDay.getCurrentHour();
        if (currentHour <= lastProcessedHour) return;
        
        lastProcessedHour = currentHour;
        timeSpentInCurrentHour = 0; // Сбрасываем счетчик для нового часа
        
        // Проверяем, не на перерыве ли сотрудник
        if (employee.isOnBreak()) {
            handleBreak();
            return;
        }
        
        // Проверяем, не нужно ли взять перерыв
        if (employee.shouldTakeBreak()) {
            Break break_ = employee.generateRandomBreak();
            employee.startBreak(break_);
            handleBreak();
            
            // После короткого перерыва возвращаемся к работе в том же часу
            if (!employee.isOnBreak()) {
                continueWorkingInSameHour();
            }
            return;
        }
        
        // Работаем над задачами
        workInCurrentHour();
    }

    private void handleBreak() {
        if (employee.getCurrentBreak() != null) {
            Break break_ = employee.getCurrentBreak();
            int breakMinutes = break_.getDurationMinutes();
            
            // Если перерыв длится больше часа, разбиваем его
            if (breakMinutes > 60) {
                // Занимаем весь час перерывом
                employee.addNonWorkingTime(60);
                timeSpentInCurrentHour = 60;
                break_.setDurationMinutes(breakMinutes - 60);
                System.out.println(employee.getName() + " продолжает " + break_.getName() + 
                                 " (осталось " + break_.getDurationString() + ")");
            } else {
                // Завершаем перерыв
                employee.endBreak();
                timeSpentInCurrentHour += break_.getDurationMinutes();
            }
        }
    }

    private void continueWorkingInSameHour() {
        // После короткого перерыва продолжаем работать в том же часу
        int remainingMinutes = 60 - timeSpentInCurrentHour;
        if (remainingMinutes <= 0) return;
        
        Task currentTask = employee.getNextTask();
        if (currentTask != null) {
            workOnTaskPartial(currentTask, remainingMinutes);
        } else {
            idlePartial(remainingMinutes);
        }
    }

    private void workInCurrentHour() {
        Task currentTask = employee.getNextTask();
        
        if (currentTask != null) {
            workOnTask(currentTask);
        } else {
            idle();
        }
    }

    private void workOnTask(Task task) {
        employee.setWorking(true);
        
        // Работаем 60 минут (1 час) над задачей
        int actualMinutes = Math.min(60, task.getRemainingMinutes());
        
        if (actualMinutes > 0) {
            System.out.println(employee.getName() + " работает над задачей '" + 
                             task.getName() + "' (" + task.getTimeString(actualMinutes) + ")");
            
            employee.workOnTask(task, actualMinutes);
            timeSpentInCurrentHour += actualMinutes;
            
            if (task.isCompleted()) {
                System.out.println(employee.getName() + " завершил задачу '" + task.getName() + "'");
            }
        }
        
        employee.setWorking(false);
    }

    private void workOnTaskPartial(Task task, int availableMinutes) {
        employee.setWorking(true);
        
        // Работаем только доступное время над задачей
        int actualMinutes = Math.min(availableMinutes, task.getRemainingMinutes());
        
        if (actualMinutes > 0) {
            System.out.println(employee.getName() + " работает над задачей '" + 
                             task.getName() + "' (" + task.getTimeString(actualMinutes) + ")");
            
            employee.workOnTask(task, actualMinutes);
            timeSpentInCurrentHour += actualMinutes;
            
            if (task.isCompleted()) {
                System.out.println(employee.getName() + " завершил задачу '" + task.getName() + "'");
            }
        }
        
        employee.setWorking(false);
    }

    private void idle() {
        employee.setWorking(false);
        employee.addNonWorkingTime(60); // 1 час простоя
        timeSpentInCurrentHour = 60;
        System.out.println(employee.getName() + " простаивает (нет задач)");
    }

    private void idlePartial(int availableMinutes) {
        employee.setWorking(false);
        employee.addNonWorkingTime(availableMinutes);
        timeSpentInCurrentHour += availableMinutes;
        System.out.println(employee.getName() + " простаивает (нет задач) - " + 
                         employee.getTimeString(availableMinutes));
    }

    public void stop() {
        shouldStop = true;
    }

    public Employee getEmployee() {
        return employee;
    }

    public boolean isRunning() {
        return !shouldStop;
    }
} 