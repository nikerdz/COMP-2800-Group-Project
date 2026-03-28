package com.coursely.model;

/**
 * Enumerates the supported types of course sections.
 */
public enum SectionType {
    LECTURE,
    LAB,
    TUTORIAL,
    SEMINAR;

    /**
     * Converts a string value to a SectionType.
     * Defaults to LECTURE when the input is null.
     *
     * @param value the string representation of the section type
     * @return the matching SectionType
     */
    public static SectionType fromString(String value) {
        if (value == null) {
            return LECTURE;
        }
        return SectionType.valueOf(value.trim().toUpperCase());
    }
}