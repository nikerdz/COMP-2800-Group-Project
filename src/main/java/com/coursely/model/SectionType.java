package com.coursely.model;

public enum SectionType {
    LECTURE,
    LAB,
    TUTORIAL,
    SEMINAR;

    public static SectionType fromString(String value) {
        if (value == null) return LECTURE;
        return SectionType.valueOf(value.trim().toUpperCase());
    }
}