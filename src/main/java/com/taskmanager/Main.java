package com.taskmanager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.taskmanager.dao.TaskDao;
import com.taskmanager.db.DatabaseConnection;
import com.taskmanager.service.TaskService;
import com.taskmanager.ui.TaskManagerFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.rowHeight", 34);

        SwingUtilities.invokeLater(() -> {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            TaskDao taskDao = new TaskDao(databaseConnection);
            TaskService taskService = new TaskService(taskDao);
            new TaskManagerFrame(taskService).setVisible(true);
        });
    }
}
