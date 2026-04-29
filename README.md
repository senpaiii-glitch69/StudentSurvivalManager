# Student Survival Management

A comprehensive JavaFX application for students to manage their academic life.

## Features

### Core Features
- **📊 Dashboard** - Overview of expenses, tasks, and events
- **💸 Expense Tracking** - Track daily expenses with categories
- **📋 Assignment Management** - Manage assignments with due dates and priorities
- **📅 Event Calendar** - Schedule and track events

### Study Tools
- **⏱ Pomodoro Timer** - Built-in 25/5 minute study timer with customizable durations
- **🗓 Calendar View** - Visual calendar showing all events and assignments
- **🗓 Study Schedule** - Weekly class schedule with time blocks
- **📝 Notes Module** - Create and organize study notes by subject

### Academic Tracking
- **🎓 Grade Tracker** - Track courses, grades, and calculate GPA
- **📊 Attendance Tracker** - Track class attendance with percentage calculations
- **📝 Exam Tracker** - Track exam dates, results, and performance history
- **🎯 Goal Setting** - Set academic goals and track progress over time

### Data & Analytics
- **👤 User Management** - Multiple users with secure authentication
- **💾 Data Persistence** - All data saved automatically to JSON
- **📈 Analytics** - Charts and visualizations for expenses and tasks
- **📤 Export/Import** - CSV export and backup/restore functionality
- **🔍 Global Search** - Search across all data types

### Customization
- **🎨 Theme Switcher** - Toggle between dark and light themes
- **🔔 Notifications** - Desktop notifications for upcoming deadlines

## Installation

### Prerequisites
- Java 17 or higher
- JavaFX 21.0.5
- Maven 3.6+

### Running from Source

1. Clone the repository
2. Navigate to the project directory
3. Run the application:

```bash
# Compile
mvn clean compile

# Run
./run.sh
```

Or using Maven:

```bash
mvn javafx:run
```

### Default Credentials
- Username: `admin`
- Password: `1234`

## Building Native Packages

### macOS
```bash
./deploy.sh
```

This will create a `.dmg` installer in the `dist/` directory.

### Linux
```bash
./deploy.sh
```

This will create a `.deb` package in the `dist/` directory.

### Windows
```bash
deploy.bat
```

This will create an `.exe` installer in the `dist/` directory.

## Data Storage

All data is stored in:
- macOS/Linux: `~/.studyhub/data.json`
- Windows: `%USERPROFILE%\.studyhub\data.json`

Backups are stored in:
- `~/.studyhub/backups/`

## Usage

### Adding Expenses
1. Navigate to the Expenses tab
2. Fill in the expense name, amount, category, and date
3. Click "Add Expense"

### Managing Assignments
1. Navigate to the Assignments tab
2. Enter assignment details including title, due date, and priority
3. Click "Add Assignment"
4. Mark as complete using the ✔ button

### Using Pomodoro Timer
1. Navigate to the Pomodoro tab
2. Adjust work/break duration if needed
3. Click "Start" to begin a focus session
4. Timer will automatically switch between work and break periods

### Tracking Grades
1. Navigate to the Grades tab
2. Add courses with name, code, credits, and grade
3. GPA is calculated automatically

### Managing Notes
1. Navigate to the Notes tab
2. Click "New Note" to create a note
3. Enter title, subject, and content
4. Notes are auto-saved as you type

### Creating Study Schedule
1. Navigate to the Schedule tab
2. Double-click on a time slot to add a class
3. Enter subject, day, time, and location
4. View today's classes at the bottom

### Setting Goals
1. Navigate to the Goals tab
2. Click "New Goal"
3. Enter title, category, target value, and deadline
4. Track progress and mark complete when done

### Exporting Data
1. Click "Export/Import" in the top bar
2. Choose to export expenses, tasks, or events to CSV
3. Or create a full backup

## Development

### Project Structure
```
src/main/java/com/student/
├── StudyHubPro.java          # Main application
├── model/                    # Data models
│   ├── AppData.java
│   ├── AppSettings.java
│   ├── Attendance.java
│   ├── Course.java
│   ├── Exam.java
│   ├── EventItem.java
│   ├── Expense.java
│   ├── Goal.java
│   ├── Note.java
│   ├── ScheduleItem.java
│   ├── Task.java
│   └── User.java
├── data/                     # Data management
│   └── DataManager.java
└── ui/                       # UI components
    ├── AttendanceTracker.java
    ├── CalendarView.java
    ├── ChartsView.java
    ├── ExamTracker.java
    ├── ExportImportDialog.java
    ├── GoalSetting.java
    ├── GradeTracker.java
    ├── GlobalSearch.java
    ├── LoginScreen.java
    ├── NotesModule.java
    ├── NotificationSystem.java
    ├── PomodoroTimer.java
    ├── StudySchedule.java
    └── ThemeSwitcher.java
```

### Building
```bash
mvn clean package
```

### Running Tests
```bash
mvn test
```

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
