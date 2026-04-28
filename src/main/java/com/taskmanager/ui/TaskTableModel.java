package com.taskmanager.ui;

import com.taskmanager.model.Task;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TaskTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"ID", "Title", "Description", "Priority", "Category", "Deadline", "Status"};
    private List<Task> tasks = new ArrayList<>();

    public void setTasks(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
        fireTableDataChanged();
    }

    public Task getTaskAt(int rowIndex) {
        return tasks.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = tasks.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> task.getId();
            case 1 -> task.getTitle();
            case 2 -> task.getDescription();
            case 3 -> task.getPriority();
            case 4 -> task.getCategory();
            case 5 -> task.getDeadline();
            case 6 -> task.getStatus();
            default -> "";
        };
    }
}
