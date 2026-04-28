package com.taskmanager.ui;

import com.formdev.flatlaf.FlatClientProperties;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskManagerFrame extends JFrame {
    private static final String ALL = "All";
    private static final Color BACKGROUND = new Color(13, 17, 23);
    private static final Color SURFACE = new Color(22, 27, 34);
    private static final Color SURFACE_ALT = new Color(30, 38, 49);
    private static final Color BORDER = new Color(48, 54, 61);
    private static final Color TEXT = new Color(229, 231, 235);
    private static final Color MUTED_TEXT = new Color(156, 163, 175);

    private final TaskService taskService;
    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable taskTable = new JTable(tableModel);

    private final JTextField titleField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(4, 20);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JTextField categoryField = new JTextField();
    private final JTextField deadlineField = new JTextField();
    private final JComboBox<TaskStatus> statusBox = new JComboBox<>(TaskStatus.values());

    private final JComboBox<String> statusFilter = new JComboBox<>();
    private final JComboBox<String> priorityFilter = new JComboBox<>();
    private final JComboBox<String> categoryFilter = new JComboBox<>();
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<SortOption> sortBox = new JComboBox<>(SortOption.values());

    private final JLabel totalLabel = new JLabel("Total: 0");
    private final JLabel completedLabel = new JLabel("Completed: 0");
    private final JLabel overdueLabel = new JLabel("Overdue: 0");

    private Integer selectedTaskId;
    private boolean refreshingCategories;

    public TaskManagerFrame(TaskService taskService) {
        super("Task Management System");
        this.taskService = taskService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);

        configureFormComponents();
        configureTable();
        setContentPane(buildContent());
        attachListeners();
        resetFilters();
        loadTasks();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFormPanel(), buildTablePanel());
        splitPane.setResizeWeight(0.26);
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);
        splitPane.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildToolbar(), BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(buildStatsPanel(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Task Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Track priority, deadlines, and completion in one workspace");
        subtitle.setForeground(MUTED_TEXT);

        JPanel copy = new JPanel(new BorderLayout(0, 4));
        copy.setOpaque(false);
        copy.add(title, BorderLayout.NORTH);
        copy.add(subtitle, BorderLayout.SOUTH);
        header.add(copy, BorderLayout.WEST);
        return header;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(SURFACE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        statusFilter.addItem(ALL);
        for (TaskStatus status : TaskStatus.values()) {
            statusFilter.addItem(status.getLabel());
        }

        priorityFilter.addItem(ALL);
        for (Priority priority : Priority.values()) {
            priorityFilter.addItem(priority.getLabel());
        }

        categoryFilter.addItem(ALL);

        toolbar.add(new JLabel("Status"));
        toolbar.add(statusFilter);
        toolbar.add(new JLabel("Priority"));
        toolbar.add(priorityFilter);
        toolbar.add(new JLabel("Category"));
        toolbar.add(categoryFilter);
        toolbar.add(new JLabel("Search"));
        toolbar.add(searchField);
        toolbar.add(new JLabel("Sort"));
        toolbar.add(sortBox);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        refreshButton.addActionListener(event -> loadTasks());
        toolbar.add(refreshButton);
        return toolbar;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setBackground(SURFACE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel heading = sectionLabel("Task Details");

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(fields, gbc, 0, "Title", titleField);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        addField(fields, gbc, 1, "Description", new JScrollPane(descriptionArea));
        addField(fields, gbc, 2, "Priority", priorityBox);
        addField(fields, gbc, 3, "Category", categoryField);
        addField(fields, gbc, 4, "Deadline (yyyy-mm-dd)", deadlineField);
        addField(fields, gbc, 5, "Status", statusBox);

        form.add(heading, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        form.add(buildFormButtons(), BorderLayout.SOUTH);
        return form;
    }

    private JPanel buildFormButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        JButton createButton = new JButton("Create");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton completeButton = new JButton("Mark Completed");
        JButton pendingButton = new JButton("Mark Pending");
        JButton clearButton = new JButton("Clear");

        createButton.putClientProperty(FlatClientProperties.STYLE, "background: #2563eb; foreground: #ffffff; font: bold");
        updateButton.putClientProperty(FlatClientProperties.STYLE, "background: #374151; foreground: #f9fafb; font: bold");
        deleteButton.putClientProperty(FlatClientProperties.STYLE, "background: #7f1d1d; foreground: #ffffff; font: bold");

        createButton.addActionListener(event -> createTask());
        updateButton.addActionListener(event -> updateTask());
        deleteButton.addActionListener(event -> deleteTask());
        completeButton.addActionListener(event -> updateSelectedStatus(TaskStatus.COMPLETED));
        pendingButton.addActionListener(event -> updateSelectedStatus(TaskStatus.PENDING));
        clearButton.addActionListener(event -> clearForm());

        buttons.add(createButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(completeButton);
        buttons.add(pendingButton);
        buttons.add(clearButton);
        return buttons;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        panel.add(sectionLabel("Tasks"), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(SURFACE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        totalLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(metricPanel(totalLabel));
        panel.add(metricPanel(completedLabel));
        panel.add(metricPanel(overdueLabel));
        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        label.setForeground(TEXT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        return label;
    }

    private JPanel metricPanel(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE_ALT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(MUTED_TEXT);
        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(6, 12, 6, 0);
        panel.add(component, gbc);
        gbc.insets = new Insets(6, 0, 6, 0);
    }

    private void configureFormComponents() {
        titleField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Write the task title");
        categoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Work, School, Personal");
        deadlineField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, LocalDate.now().toString());
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by title");

        descriptionArea.setBackground(SURFACE_ALT);
        descriptionArea.setForeground(TEXT);
        descriptionArea.setCaretColor(TEXT);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void configureTable() {
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(34);
        taskTable.setAutoCreateRowSorter(true);
        taskTable.setBackground(SURFACE);
        taskTable.setForeground(TEXT);
        taskTable.setGridColor(BORDER);
        taskTable.setShowVerticalLines(false);
        taskTable.setIntercellSpacing(new Dimension(0, 1));
        taskTable.setFillsViewportHeight(true);
        taskTable.setBorder(null);
        taskTable.getTableHeader().setBackground(SURFACE_ALT);
        taskTable.getTableHeader().setForeground(MUTED_TEXT);
        taskTable.getTableHeader().setFont(taskTable.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        OverdueTaskRenderer renderer = new OverdueTaskRenderer();
        for (int i = 0; i < taskTable.getColumnCount(); i++) {
            taskTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(280);
    }

    private void attachListeners() {
        taskTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateSelectedTask();
            }
        });

        statusFilter.addActionListener(event -> loadTasks());
        priorityFilter.addActionListener(event -> loadTasks());
        categoryFilter.addActionListener(event -> {
            if (!refreshingCategories) {
                loadTasks();
            }
        });
        sortBox.addActionListener(event -> loadTasks());
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
            TaskFilter filter = buildFilter();
            List<Task> tasks = taskService.findTasks(filter);
            tableModel.setTasks(tasks);
            updateStats();
            refreshCategories();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private TaskFilter buildFilter() {
        return new TaskFilter(
                selectedStatusFilter(),
                selectedPriorityFilter(),
                selectedCategoryFilter(),
                searchField.getText(),
                (SortOption) sortBox.getSelectedItem()
        );
    }

    private TaskStatus selectedStatusFilter() {
        String value = (String) statusFilter.getSelectedItem();
        return value == null || ALL.equals(value) ? null : TaskStatus.fromLabel(value);
    }

    private Priority selectedPriorityFilter() {
        String value = (String) priorityFilter.getSelectedItem();
        return value == null || ALL.equals(value) ? null : Priority.fromLabel(value);
    }

    private String selectedCategoryFilter() {
        String value = (String) categoryFilter.getSelectedItem();
        return value == null || ALL.equals(value) ? null : value;
    }

    private void refreshCategories() throws SQLException {
        String selected = (String) categoryFilter.getSelectedItem();
        refreshingCategories = true;
        try {
            categoryFilter.removeAllItems();
            categoryFilter.addItem(ALL);
            for (String category : taskService.findCategories()) {
                categoryFilter.addItem(category);
            }
            categoryFilter.setSelectedItem(selected == null ? ALL : selected);
        } finally {
            refreshingCategories = false;
        }
    }

    private void updateStats() throws SQLException {
        TaskStats stats = taskService.calculateAllStats();
        totalLabel.setText("Total: " + stats.totalTasks());
        completedLabel.setText("Completed: " + stats.completedTasks());
        overdueLabel.setText("Overdue: " + stats.overdueTasks());
    }

    private void resetFilters() {
        statusFilter.setSelectedItem(ALL);
        priorityFilter.setSelectedItem(ALL);
        categoryFilter.setSelectedItem(ALL);
        sortBox.setSelectedItem(SortOption.NONE);
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
