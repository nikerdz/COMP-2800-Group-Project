# Coursely

## Overview
Coursely is a Java-based weekly course planner and timetable builder designed to help university students organize their schedules, visualize weekly timetables, and avoid time conflicts.

## Team Members
- Daniyal Ahmad
- Landon Hadre
- Shannel Narula
- Anika Khan

## Course
COMP 2800 – Software Development

## Tech Stack
- Java
- Java Swing
- SQLite
- JDBC

## Current Project Status (Sprint 2)
We are currently in **Sprint 2 (Mar 16 – Mar 30)**. Sprint 1 established the project foundation (repo structure, initial UI skeleton, database schema + initialization). Sprint 2 focuses on making the application more functional and polished, including timetable block editing and usability improvements.

### Sprint 2 Focus
- Create/edit/remove timetable blocks (interactive controls)
- Color-coded blocks (preset palette)
- Conflict feedback (time overlap detection)
- Save/load timetables locally (multiple saved schedules by title)
- Export timetable as PNG (if time allows)
- Branding + UI theme (logo, fonts, color scheme)

## Project Structure
- `docs/` – planning notes, sprint reports, diagrams, and supporting documentation
- `database/` – SQL schema and database design assets (ER diagram)
- `data/` – local database file created at runtime (ignored by Git)
- `src/main/java/` – main application source code
- `src/main/resources/` – app resources (images, fonts, etc.)
- `src/test/java/` – test source files (unit/integration tests as needed)
- `lib/` – local JAR dependencies (e.g., SQLite JDBC driver)

## How to Run
1. Open the project in your IDE (VS Code / IntelliJ).
2. Ensure the SQLite JDBC JAR is available (see `lib/` and VS Code referenced libraries settings if applicable).
3. Run `App.java` from the `com.coursely` package.

On startup, the app initializes the SQLite database using `database/schema.sql` and creates `data/coursely.db` if it does not exist.

## Branch Workflow
Use feature branches for development and merge into `main` via Pull Request after review.

**Branch naming:**
- `feature/<short-description>`
- `fix/<short-description>`
- `docs/<short-description>`

Examples:
- `feature/block-edit-delete`
- `feature/schedule-save-load`
- `feature/ui-theme-branding`
- `fix/db-init`
- `docs/sprint2-update`

**Commit messages:**
Use short, descriptive messages starting with a verb:
- `Add timetable block edit/delete actions`
- `Implement schedule save/load to SQLite`
- `Apply branding theme and logo assets`

## Sprint 2 Goal
Deliver a functional, polished timetable builder where users can create, edit, and remove blocks, see them clearly on the weekly grid (with colors), and begin saving/loading schedules locally.
