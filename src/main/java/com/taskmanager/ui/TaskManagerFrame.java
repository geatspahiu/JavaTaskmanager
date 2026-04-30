package com.taskmanager.ui;

import com.taskmanager.model.Priority;
import com.taskmanager.model.SortOption;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskFilter;
import com.taskmanager.model.TaskStats;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.service.TaskService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskManagerFrame extends JFrame {
    private static final String ALL = "All";

    private final TaskService taskService;
    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable taskTable = new JTable(tableModel);

    private final JTextField titleField = new JTextField(22);
    private final JTextArea descriptionArea = new JTextArea(3, 22);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JTextField categoryField = new JTextField(16);
    private final JTextField deadlineField = new JTextField(10);
    private final JComboBox<TaskStatus> statusBox = new JComboBox<>(TaskStatus.values());

    private final JComboBox<String> statusFilter = new JComboBox<>();
    private final JTextField searchField = new JTextField(18);
    private final JLabel statsLabel = new JLabel("Total: 0 | Completed: 0 | Overdue: 0");

    private Integer selectedTaskId;

    public TaskManagerFrame(TaskService taskService) {
        super("Task Manager");
        this.taskService = taskService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        configureTable();
        setContentPane(buildContent());
        attachListeners();
        clearForm();
        loadTasks();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        root.add(buildFormPanel(), BorderLayout.NORTH);
        root.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Task"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(fields, gbc, 0, 0, "Title", titleField);
        addField(fields, gbc, 0, 2, "Priority", priorityBox);
        addField(fields, gbc, 1, 0, "Category", categoryField);
        addField(fields, gbc, 1, 2, "Deadline", deadlineField);
        addField(fields, gbc, 2, 0, "Status", statusBox);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        addField(fields, gbc, 3, 0, "Description", new JScrollPane(descriptionArea), 3);

        form.add(fields, BorderLayout.CENTER);
        form.add(buildButtons(), BorderLayout.SOUTH);
        return form;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton doneButton = new JButton("Complete");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(event -> createTask());
        updateButton.addActionListener(event -> updateTask());
        deleteButton.addActionListener(event -> deleteTask());
        doneButton.addActionListener(event -> updateSelectedStatus(TaskStatus.COMPLETED));
        clearButton.addActionListener(event -> clearForm());

        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(doneButton);
        buttons.add(clearButton);
        return buttons;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusFilter.addItem(ALL);
        for (TaskStatus status : TaskStatus.values()) {
            statusFilter.addItem(status.getLabel());
        }

        filters.add(new JLabel("Status"));
        filters.add(statusFilter);
        filters.add(new JLabel("Search"));
        filters.add(searchField);

        bottom.add(filters, BorderLayout.WEST);
        bottom.add(statsLabel, BorderLayout.EAST);
        return bottom;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, java.awt.Component field) {
        addField(panel, gbc, row, col, label, field, 1);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label,
                          java.awt.Component field, int fieldWidth) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 1;
        gbc.gridwidth = fieldWidth;
        panel.add(field, gbc);
        gbc.gridwidth = 1;
    }

    private void configureTable() {
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(28);
        taskTable.setAutoCreateRowSorter(true);
        taskTable.setFillsViewportHeight(true);

        OverdueTaskRenderer renderer = new OverdueTaskRenderer();
        for (int i = 0; i < taskTable.getColumnCount(); i++) {
            taskTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(260);
    }

    private void attachListeners() {
        taskTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateSelectedTask();
            }
        });

        statusFilter.addActionListener(event -> loadTasks());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadTasks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadTasks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadTasks();
            }
        });
    }

    private void createTask() {
        try {
            taskService.createTask(taskFromForm(0));
            clearForm();
            loadTasks();
        } catch (IllegalArgumentException | SQLException ex) {
            showError(ex);
        }
    }

    private void updateTask() {
        if (selectedTaskId == null) {
            showMessage("Select a task to update.");
            return;
        }
        try {
            taskService.updateTask(taskFromForm(selectedTaskId));
            clearForm();
            loadTasks();
        } catch (IllegalArgumentException | SQLException ex) {
            showError(ex);
        }
    }

    private void deleteTask() {
        if (selectedTaskId == null) {
            showMessage("Select a task to delete.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete selected task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            taskService.deleteTask(selectedTaskId);
            clearForm();
            loadTasks();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateSelectedStatus(TaskStatus status) {
        if (selectedTaskId == null) {
            showMessage("Select a task first.");
            return;
        }
        try {
            if (status == TaskStatus.COMPLETED) {
                taskService.markCompleted(selectedTaskId);
            } else {
                taskService.markPending(selectedTaskId);
            }
            loadTasks();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private Task taskFromForm(int id) {
        return new Task(
                id,
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                (Priority) priorityBox.getSelectedItem(),
                categoryField.getText().trim(),
                parseDeadline(),
                (TaskStatus) statusBox.getSelectedItem()
        );
    }

    private LocalDate parseDeadline() {
        try {
            return LocalDate.parse(deadlineField.getText().trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Deadline must use yyyy-mm-dd format.");
        }
    }

    private void populateSelectedTask() {
        int viewRow = taskTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        Task task = tableModel.getTaskAt(taskTable.convertRowIndexToModel(viewRow));
        selectedTaskId = task.getId();
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        priorityBox.setSelectedItem(task.getPriority());
        categoryField.setText(task.getCategory());
        deadlineField.setText(task.getDeadline().toString());
        statusBox.setSelectedItem(task.getStatus());
    }

    private void loadTasks() {
        try {
            List<Task> tasks = taskService.findTasks(buildFilter());
            tableModel.setTasks(tasks);
            updateStats();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private TaskFilter buildFilter() {
        return new TaskFilter(selectedStatusFilter(), null, null, searchField.getText(), SortOption.NONE);
    }

    private TaskStatus selectedStatusFilter() {
        String value = (String) statusFilter.getSelectedItem();
        return value == null || ALL.equals(value) ? null : TaskStatus.fromLabel(value);
    }

    private void updateStats() throws SQLException {
        TaskStats stats = taskService.calculateAllStats();
        statsLabel.setText("Total: " + stats.totalTasks()
                + " | Completed: " + stats.completedTasks()
                + " | Overdue: " + stats.overdueTasks());
    }

    private void clearForm() {
        selectedTaskId = null;
        taskTable.clearSelection();
        titleField.setText("");
        descriptionArea.setText("");
        priorityBox.setSelectedItem(Priority.MEDIUM);
        categoryField.setText("");
        deadlineField.setText(LocalDate.now().toString());
        statusBox.setSelectedItem(TaskStatus.PENDING);
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Task Manager", JOptionPane.INFORMATION_MESSAGE);
    }
}
