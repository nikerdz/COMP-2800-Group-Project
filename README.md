<img src="src/main/resources/images/logo2.png" alt="Coursely Logo" width="500"/>

# Coursely
A Java-based weekly course planner and timetable builder for university students.

## Overview
Coursely helps university students organize their schedules, visualize weekly timetables, and avoid time conflicts. Built with Java Swing and SQLite, it runs as a native desktop application with no internet connection required.

## Quick Start
- Want the easiest setup? Download the latest release from the [Releases](../../releases) page.
- **Windows:** extract `Coursely-1.0-windows.zip` and run `Coursely.exe`
- **Mac/Linux:** run `java -jar Coursely-1.0-any-platform.jar`
- The SQLite database is created automatically on first launch.

## Tech Stack
- Java 17+
- Java Swing (UI)
- SQLite (local database)
- JDBC (database connectivity)

## Features
- Add, edit, and delete course time blocks on a weekly timetable grid
- Colour-coded blocks with a preset palette
- Time conflict detection and visual feedback
- Save and load multiple named schedules
- Export timetable as a PNG image
- Persistent storage via local SQLite database

## Downloads
The latest release is available on the [Releases](../../releases) page.

- **Windows**: Download `Coursely-1.0-windows.zip` — no Java required, just extract and double-click `Coursely.exe`
- **Mac / Linux**: Download `Coursely-1.0-any-platform.jar` — requires Java 17+, then run:
```
  java -jar Coursely-1.0-any-platform.jar
```
> ⚠️ Avoid installing or running from paths with special characters (e.g. `!`, `&`).

## How to Run from Source
1. Clone the repository
2. Open the project in VS Code or IntelliJ
3. Ensure the SQLite JDBC JAR is on the classpath (see `lib/`)
4. Run `App.java` from the `com.coursely` package

On first startup the app creates the database at `~/Coursely/coursely.db` and initializes the schema automatically.

## Project Structure
```
src/main/java/        — application source code
  com/coursely/
    db/               — database layer (DAOs, DatabaseManager, initializer)
    model/            — data models (Schedule, Section, TimeBlock, etc.)
    service/          — business logic
    ui/               — Swing UI components
src/main/resources/   — fonts, images, and schema
database/             — SQL schema file
lib/                  — SQLite JDBC driver JAR
docs/                 — planning notes, sprint reports, diagrams
```

## Database
The app uses SQLite. The schema is defined in `database/schema.sql` and is automatically applied on first run. The database file is created at:
- **Windows**: `C:\Users\<you>\Coursely\coursely.db`
- **Mac/Linux**: `~/Coursely/coursely.db`

No manual database setup or connection configuration is required.

## Team Members
- Daniyal Ahmad
- Landon Hadre
- Shannel Narula
- Anika Khan

## Course
COMP 2800 – Software Development 
- Dr. Andreas Maniatis
- University of Windsor, Winter 2026
