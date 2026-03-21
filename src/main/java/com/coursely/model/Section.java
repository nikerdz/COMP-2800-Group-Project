package com.coursely.model;

import java.awt.Color;
import java.util.Objects;
import java.util.UUID;

public class Section {

    private final String id;        // unique id for edit/remove
    private String title;           // e.g. "COMP-2800" or "Math"
    private String type;            // optional e.g. "Lecture"
    private Color color;            // chosen from palette
    private TimeBlock timeBlock;

    public Section(String title, String type, Color color, TimeBlock timeBlock) {
        this.id = UUID.randomUUID().toString();
        this.title = Objects.requireNonNull(title);
        this.type = (type == null) ? "" : type;
        this.color = color; // can be null; UI can default
        this.timeBlock = Objects.requireNonNull(timeBlock);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public TimeBlock getTimeBlock() {
        return timeBlock;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title);
    }

    public void setType(String type) {
        this.type = (type == null) ? "" : type;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setTimeBlock(TimeBlock timeBlock) {
        this.timeBlock = Objects.requireNonNull(timeBlock);
    }

    @Override
    public String toString() {
        return title + " | " + type + " | " + timeBlock;
    }
}