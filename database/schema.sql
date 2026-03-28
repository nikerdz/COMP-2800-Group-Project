-- Enforces foreign key constraints, which SQLite disables by default
PRAGMA foreign_keys = ON;

-- Stores course catalog entries, each uniquely identified by their code and term
CREATE TABLE IF NOT EXISTS courses (
    course_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    course_code TEXT NOT NULL,              -- Short identifier (e.g. "CS101")
    course_name TEXT NOT NULL,              -- Full descriptive name of the course
    faculty     TEXT,                       -- Faculty or department offering the course
    term        TEXT,                       -- Academic term the course runs in (e.g. "Fall 2025")
    UNIQUE (course_code, term)              -- Same course code can exist across different terms
);

-- Stores individual sections belonging to a course (e.g. a lecture and a lab for the same course)
CREATE TABLE IF NOT EXISTS sections (
    section_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id    INTEGER NOT NULL,
    section_code TEXT NOT NULL,             -- Section identifier (e.g. "001", "L01")
    section_type TEXT NOT NULL CHECK (section_type IN ('LECTURE', 'LAB', 'TUTORIAL', 'SEMINAR')),
    instructor   TEXT,                      -- Name of the instructor teaching the section
    location     TEXT,                      -- Room or building where the section meets
    color        TEXT,                      -- Display color for UI representation
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE CASCADE                   -- Removing a course removes all its sections
        ON UPDATE CASCADE,                  -- Course ID changes propagate to sections
    UNIQUE (course_id, section_code, section_type) -- A course cannot have duplicate section codes of the same type
);

-- Stores the recurring weekly time slots for a section
CREATE TABLE IF NOT EXISTS time_blocks (
    time_block_id INTEGER PRIMARY KEY AUTOINCREMENT,
    section_id    INTEGER NOT NULL,
    day_of_week   TEXT NOT NULL CHECK (
        day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')
    ),
    start_time    TEXT NOT NULL,            -- Start time in HH:MM format
    end_time      TEXT NOT NULL,            -- End time in HH:MM format
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
        ON DELETE CASCADE                   -- Removing a section removes all its time blocks
        ON UPDATE CASCADE,
    CHECK (start_time < end_time)           -- Ensures end time is always after start time
);

-- Stores named schedules, each representing a possible arrangement of sections for a term
CREATE TABLE IF NOT EXISTS schedules (
    schedule_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_name TEXT NOT NULL,
    term          TEXT,                     -- Academic term this schedule is built for
    created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP -- Timestamp of when the schedule was created
);

-- Junction table linking schedules to their selected sections (many-to-many relationship)
CREATE TABLE IF NOT EXISTS schedule_sections (
    schedule_section_id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_id         INTEGER NOT NULL,
    section_id          INTEGER NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id)
        ON DELETE CASCADE                   -- Removing a schedule removes all its section associations
        ON UPDATE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
        ON DELETE CASCADE                   -- Removing a section removes it from all schedules
        ON UPDATE CASCADE,
    UNIQUE (schedule_id, section_id)        -- A section can only appear once per schedule
);