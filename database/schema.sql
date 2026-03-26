PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS courses (
    course_id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_code TEXT NOT NULL,
    course_name TEXT NOT NULL,
    faculty TEXT,
    term TEXT,
    UNIQUE (course_code, term)
);

CREATE TABLE IF NOT EXISTS sections (
    section_id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL,
    section_code TEXT NOT NULL,
    section_type TEXT NOT NULL CHECK (section_type IN ('LECTURE', 'LAB', 'TUTORIAL', 'SEMINAR')),
    instructor TEXT,
    location TEXT,
    color TEXT,
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    UNIQUE (course_id, section_code, section_type)
);

CREATE TABLE IF NOT EXISTS time_blocks (
    time_block_id INTEGER PRIMARY KEY AUTOINCREMENT,
    section_id INTEGER NOT NULL,
    day_of_week TEXT NOT NULL CHECK (
        day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')
    ),
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CHECK (start_time < end_time)
);

CREATE TABLE IF NOT EXISTS schedules (
    schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_name TEXT NOT NULL,
    term TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedule_sections (
    schedule_section_id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_id INTEGER NOT NULL,
    section_id INTEGER NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    UNIQUE (schedule_id, section_id)
);