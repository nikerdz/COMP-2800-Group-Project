# Project Structure

This document describes the folder and file organization for the Coursely project.

## Root Directory

### `.vscode/`
Contains shared Visual Studio Code workspace settings needed for the project to be recognized and run correctly.

### `bin/`
Contains compiled output files generated when the Java project is built or run locally in the IDE. These files are not source code.

### `database/`
Stores database-related files for the project.

- `schema.sql` – SQL schema for creating the project database tables
- `coursely_er_diagram.drawio.png` – exported ER diagram image for database design reference

### `docs/`
Stores project documentation, planning notes, diagrams, and supporting files.

### `src/`
Contains all source code and source-related project files.

---

## Source Code Structure

### `src/main/`
Contains the main application source code and resources.

#### `src/main/java/`
Contains all Java source files for the application.

#### `src/main/java/com/coursely/`
Base package for the Coursely application.

- `App.java` – main application entry point

---

## Java Packages

### `src/main/java/com/coursely/model/`
Contains model classes that represent the main entities in the system.

- `Course.java` – represents a course
- `Section.java` – represents a course section such as a lecture, lab, or tutorial
- `TimeBlock.java` – represents a scheduled meeting time for a section
- `Schedule.java` – represents a saved timetable or schedule
- `ScheduleSection.java` – represents the relationship between a schedule and its selected sections

### `src/main/java/com/coursely/db/`
Contains database-related Java classes.

- `DatabaseManager.java` – manages database connection setup and database access support

### `src/main/java/com/coursely/ui/`
Contains user interface classes built with Java Swing.

- `MainFrame.java` – main application window
- `TimetablePanel.java` – panel for displaying the weekly timetable layout

---

## Resources and Testing

### `src/main/resources/`
Contains non-code resources used by the application, such as configuration files, icons, and other assets.

### `src/test/java/`
Contains test source files for unit tests and other testing code.

---

## Notes

- The `src/main/java` folder is the main Java source root.
- The package name for the project is `com.coursely`.
- Build tool files such as Maven or Gradle configuration have not been added yet and may be introduced later once the team agrees on a build system.