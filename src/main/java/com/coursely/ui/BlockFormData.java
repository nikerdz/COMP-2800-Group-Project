package com.coursely.ui;

import java.awt.Color;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

public class BlockFormData {
    public final String title;
    public final List<String> days;
    public final LocalTime start;
    public final LocalTime end;
    public final String typeText;
    public final Color color;

    public BlockFormData(String title, List<String> days, LocalTime start,
                         LocalTime end, String typeText, Color color) {
        this.title = title;
        this.days = days;
        this.start = start;
        this.end = end;
        this.typeText = typeText;
        this.color = color;
    }

    public static BlockFormData empty() {
        return new BlockFormData(
                "",
                new ArrayList<>(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "",
                Theme.BLOCK_BLUE
        );
    }

    public static BlockFormData fromSection(Section section, String typeText) {
        List<String> days = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        if (!section.getTimeBlocks().isEmpty()) {
            start = section.getTimeBlocks().get(0).getStartTime();
            end = section.getTimeBlocks().get(0).getEndTime();
        }

        for (TimeBlock tb : section.getTimeBlocks()) {
            days.add(tb.getDayOfWeek());
        }

        Color color = section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor();

        return new BlockFormData(
                section.getSectionCode(),
                days,
                start,
                end,
                typeText == null ? "" : typeText,
                color
        );
    }
}