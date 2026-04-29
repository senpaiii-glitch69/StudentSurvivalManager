# Student Survival Management - Enhancement Summary

## What's New in v2.0

### ✅ Completed Features

#### 1. Data Persistence (JSON)
- All data automatically saved to `~/.studyhub/data.json`
- Automatic backup system with timestamped backups
- Data survives application restarts

#### 2. User Management
- Multi-user support with secure SHA-256 password hashing
- User registration with email and full name
- Login/Register tabs on login screen
- Default admin account (admin/1234)

#### 3. Pomodoro Timer
- Customizable work/break duration (default 25/5 minutes)
- Visual progress circle
- Automatic mode switching between focus and break
- Session tracking

#### 4. Calendar View
- Visual monthly calendar
- Shows indicators for days with tasks/events
- Click to see details for selected date
- Navigate between months
- "Today" quick-jump button

#### 5. Grade/GPA Tracker
- Add courses with name, code, credits, and grade
- Automatic letter grade calculation (A, A-, B+, etc.)
- Automatic GPA calculation based on grade points
- Semester organization
- Real-time statistics display

#### 6. Charts & Analytics
- Expense breakdown pie chart by category
- Monthly expense trend bar chart
- Task completion status pie chart
- Tasks by priority distribution
- Overview dashboard with key metrics

#### 7. Export/Import
- Export expenses, tasks, or events to CSV
- Full backup creation with timestamps
- Restore from backup files
- Reset all data option

#### 8. Attendance Tracker
- Track class attendance by subject
- Mark present/absent for each class
- Calculate overall attendance percentage
- View weekly and monthly attendance stats
- Filter by subject

#### 9. Exam Tracker
- Track exam dates and results
- Enter scores and calculate grades
- View exam status (Upcoming, Completed, Missed)
- Track performance history
- Calculate average exam scores

#### 10. Notes Module
- Create and organize study notes by subject
- Rich text editor with formatting options
- Tag notes for easy organization
- Search notes by title, content, or subject
- Auto-save functionality

#### 11. Study Schedule
- Weekly class schedule with time blocks
- Visual grid showing all classes
- Add/edit/delete classes
- View today's schedule at a glance
- Support for different class types (Lecture, Lab, Tutorial)

#### 12. Goal Setting
- Set academic goals with targets
- Track progress with visual indicators
- Set deadlines for goals
- Categorize goals (Assignments, Study Hours, Grades, etc.)
- Mark goals as complete

#### 13. Global Search
- Search across all data types
- Find expenses, tasks, events, notes, courses, exams, and goals
- Real-time search results
- Categorized results display

#### 14. Theme Switcher
- Toggle between dark and light themes
- Customize accent colors
- Adjust font size
- Preview theme changes

#### 15. Notification System
- Automatic deadline reminders
- Task due today/tomorrow alerts
- Overdue task notifications
- Event reminders
- Mark notifications as read/unread
- Notification settings

## Project Structure

```
src/main/java/com/student/
├── StudyHubPro.java              # Main application (NEW)
├── StudentManagerFinal.java      # Old version (kept for reference)
├── model/                        # Data models (NEW)
│   ├── AppData.java             # Main data container
│   ├── AppSettings.java         # User settings
│   ├── Attendance.java          # Attendance tracking
│   ├── Course.java              # Grade tracking
│   ├── Exam.java                # Exam tracking
│   ├── EventItem.java           # Event model
│   ├── Expense.java             # Expense model
│   ├── Goal.java                # Goal tracking
│   ├── Note.java                # Notes model
│   ├── ScheduleItem.java        # Schedule model
│   ├── Task.java                # Task model
│   └── User.java                # User model
├── data/                        # Data management (NEW)
│   └── DataManager.java         # JSON persistence, auth, export
└── ui/                          # UI components (NEW)
    ├── AttendanceTracker.java   # Attendance UI
    ├── CalendarView.java        # Calendar widget
    ├── ChartsView.java           # Analytics charts
    ├── ExamTracker.java         # Exam tracking UI
    ├── ExportImportDialog.java  # Export/Import dialog
    ├── GoalSetting.java          # Goal tracking UI
    ├── GradeTracker.java         # Grade tracking UI
    ├── GlobalSearch.java         # Search functionality
    ├── LoginScreen.java          # Login/Register UI
    ├── NotesModule.java          # Notes UI
    ├── NotificationSystem.java   # Notifications UI
    ├── PomodoroTimer.java        # Pomodoro timer UI
    ├── StudySchedule.java        # Schedule UI
    └── ThemeSwitcher.java        # Theme settings UI
```

## How to Run

### Option 1: Using Maven
```bash
cd ~/Desktop/StudentSurvivalManager
mvn javafx:run
```

### Option 2: Using run.sh
```bash
cd ~/Desktop/StudentSurvivalManager
./run.sh
```

### Option 3: Build Distribution
```bash
cd ~/Desktop/StudentSurvivalManager
./build.sh
```

This creates a distributable package in `dist/` with:
- `StudyHubPro-2.0.jar` - The application JAR
- `run.sh` - Launcher script for macOS/Linux
- `run.bat` - Launcher script for Windows
- `README.txt` - Instructions

## Deployment

### Native Installers

For native installers (DMG/EXE/DEB), use:
```bash
./deploy.sh        # macOS/Linux
deploy.bat         # Windows
```

Note: Requires `jpackage` (included with JDK 14+)

## Data Storage

- **Main Data**: `~/.studyhub/data.json`
- **Backups**: `~/.studyhub/backups/backup_YYYYMMDD_HHMMSS.json`

## Default Credentials

- **Username**: admin
- **Password**: 1234

## Next Steps

To fully deploy the application:

1. **Test the application**:
   ```bash
   mvn javafx:run
   ```

2. **Create distribution**:
   ```bash
   ./build.sh
   ```

3. **Share the dist folder** with users who have Java and JavaFX installed

4. **For native installers** (no Java required for end users):
   ```bash
   ./deploy.sh
   ```

## Dependencies Added

- `gson:2.10.1` - For JSON serialization

## Notes

- The old `StudentManagerFinal.java` is kept for reference
- All new features are in `StudyHubPro.java`
- Data is automatically saved when changes are made
- The app creates the `~/.studyhub` directory on first run
