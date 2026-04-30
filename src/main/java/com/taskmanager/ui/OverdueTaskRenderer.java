package com.taskmanager.ui;

import com.taskmanager.model.Task;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class OverdueTaskRenderer extends DefaultTableCellRenderer {
    private static final Color OVERDUE_BACKGROUND = new Color(90, 45, 45);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int modelRow = table.convertRowIndexToModel(row);
        TaskTableModel model = (TaskTableModel) table.getModel();
        Task task = model.getTaskAt(modelRow);

        if (!isSelected && task.isOverdue()) {
            component.setBackground(OVERDUE_BACKGROUND);
            component.setForeground(Color.WHITE);
        }
        return component;
    }
}
