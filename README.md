# Task Management System

A simple Java desktop task manager built with Swing, FlatLaf Dark, JDBC, and MySQL.

The app keeps the interface intentionally straightforward: a task form, a table, a small search/status filter, and basic action buttons. The code is split into separate classes for the UI, database access, business logic, and domain models.

## Features

- Create, edit, and delete tasks
- Track title, description, priority, category, deadline, and status
- Mark tasks as `Pending` or `Completed`
- Filter by status
- Search tasks by title
- Highlight overdue pending tasks
- Show basic task statistics
- Auto-create the MySQL `tasks` table on startup
- FlatLaf Dark UI with a simple Swing layout

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| UI | Swing + FlatLaf Dark |
| Database | MySQL |
| Persistence | JDBC |
| Build | Maven |

## Project Structure

```text
src/main/java/com/taskmanager
├── Main.java                      Starts FlatLaf, creates dependencies, and opens the app window.
├── dao
│   └── TaskDao.java               Runs SQL queries for creating, reading, updating, and deleting tasks.
├── db
│   └── DatabaseConnection.java    Stores MySQL connection settings and creates the tasks table.
├── model
│   ├── Priority.java              Defines Low, Medium, and High task priorities.
│   ├── SortOption.java            Defines sort choices used by the data layer.
│   ├── Task.java                  Represents one task record.
│   ├── TaskFilter.java            Holds filter/search values for task queries.
│   ├── TaskStats.java             Holds total, completed, and overdue task counts.
│   └── TaskStatus.java            Defines Pending and Completed task states.
├── service
│   └── TaskService.java           Validates input and coordinates task actions.
└── ui
    ├── OverdueTaskRenderer.java   Highlights overdue rows in the task table.
    ├── TaskManagerFrame.java      Builds the Swing form, buttons, filters, table, and stats label.
    └── TaskTableModel.java        Connects task objects to the JTable columns.
```

## Database Setup

Create the database once:

```sql
CREATE DATABASE task_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The application creates the table automatically when it starts:

```sql
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    priority ENUM('Low','Medium','High') NOT NULL DEFAULT 'Medium',
    category VARCHAR(80) NOT NULL,
    deadline DATE NOT NULL,
    status ENUM('Pending','Completed') NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Configuration

Default local connection:

```text
DB_URL=jdbc:mysql://localhost:3306/task_manager?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=
```

Override the database settings with environment variables:

```bash
export DB_URL='jdbc:mysql://localhost:3306/task_manager?createDatabaseIfNotExist=true&serverTimezone=UTC'
export DB_USER='root'
export DB_PASSWORD='your_password'
```

## Run

Using Maven from your PATH:

```bash
mvn compile exec:java
```

Using the Homebrew Maven path on Apple Silicon:

```bash
/opt/homebrew/Cellar/maven/3.9.15/bin/mvn compile exec:java
```

## Build

```bash
mvn clean package
```

## Notes

- MySQL must be running before launching the app.
- If your MySQL root user has a password, set `DB_PASSWORD` before running.
- The UI uses FlatLaf Dark by default.
- Overdue highlighting applies only to pending tasks with deadlines before today.

## Main Classes

- `Main` starts FlatLaf and launches the UI.
- `DatabaseConnection` manages JDBC connection settings and schema creation.
- `TaskDao` contains SQL queries and CRUD operations.
- `TaskService` validates tasks and handles application logic.
- `TaskManagerFrame` contains the Swing interface.
- `TaskTableModel` adapts task data for the table.
