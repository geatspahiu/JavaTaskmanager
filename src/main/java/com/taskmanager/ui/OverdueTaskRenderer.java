package com.taskmanager.ui;

import com.taskmanager.model.Task;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class OverdueTaskRenderer extends DefaultTableCellRenderer {
    private static final Color TABLE_BACKGROUND = new Color(22, 27, 34);
    private static final Color ALTERNATE_BACKGROUND = new Color(25, 31, 39);
    private static final Color OVERDUE_BACKGROUND = new Color(77, 39, 39);
    private static final Color SELECTED_BACKGROUND = new Color(37, 99, 145);
    private static final Color TEXT = new Color(229, 231, 235);
    private static final Color MUTED_TEXT = new Color(156, 163, 175);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int modelRow = table.convertRowIndexToModel(row);
        TaskTableModel model = (TaskTableModel) table.getModel();
        Task task = model.getTaskAt(modelRow);

        if (isSelected) {
            component.setBackground(SELECTED_BACKGROUND);
        } else if (task.isOverdue()) {
            component.setBackground(OVERDUE_BACKGROUND);
        } else if (row % 2 == 1) {
            component.setBackground(ALTERNATE_BACKGROUND);
        } else {
            component.setBackground(TABLE_BACKGROUND);
        }
        component.setForeground(task.isOverdue() ? Color.WHITE : TEXT);
        if (column == 0) {
            component.setForeground(MUTED_TEXT);
        }
        return component;
    }
}
