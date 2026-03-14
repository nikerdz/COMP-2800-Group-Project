package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import com.coursely.model.SampleData;
import com.coursely.model.Schedule;
import com.coursely.model.Section;

public class TimetablePanel extends JPanel {
    private static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private static final String[] TIME_SLOTS = {
        "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM", "2:00 PM",
        "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public TimetablePanel() {
        setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Weekly Timetable");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(TIME_SLOTS.length + 1, DAYS.length + 1));
        grid.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
        grid.setBackground(Color.WHITE);

        grid.add(createHeaderCell("Time"));
        for (String day : DAYS) {
            grid.add(createHeaderCell(day));
        }

        Map<String, TimetableBodyCell> bodyCells = new HashMap<>();
        for (String timeSlot : TIME_SLOTS) {
            grid.add(createTimeCell(timeSlot));
            for (String day : DAYS) {
                TimetableBodyCell bodyCell = createBodyCell();
                bodyCells.put(createCellKey(day, timeSlot), bodyCell);
                grid.add(bodyCell);
            }
        }

        populateWithSampleEntries(bodyCells, SampleData.createSampleSchedule());
        add(grid, BorderLayout.CENTER);

        JLabel previewNote = new JLabel("Showing sample schedule blocks for UI preview.");
        previewNote.setForeground(new Color(90, 90, 90));
        add(previewNote, BorderLayout.SOUTH);
    }

    private JLabel createHeaderCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(new Color(236, 243, 252));
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private JLabel createTimeCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(new Color(250, 250, 250));
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private TimetableBodyCell createBodyCell() {
        TimetableBodyCell label = new TimetableBodyCell();
        label.setBackground(Color.WHITE);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));
        return label;
    }

    private JLabel createBaseCell(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));
        return label;
    }

    private void populateWithSampleEntries(Map<String, TimetableBodyCell> bodyCells, Schedule schedule) {
        for (Section section : schedule.getSections()) {
            String day = section.getTimeBlock().getDay();
            LocalTime start = parseTimeFlexible(section.getTimeBlock().getStartTime());
            LocalTime end = parseTimeFlexible(section.getTimeBlock().getEndTime());
            if (start == null || end == null || !end.isAfter(start)) {
                continue;
            }

            boolean firstCoveredSlot = true;
            for (String slotLabel : TIME_SLOTS) {
                LocalTime slotStart = LocalTime.parse(slotLabel, SLOT_FORMAT);
                LocalTime slotEnd = slotStart.plusHours(1);

                LocalTime overlapStart = start.isAfter(slotStart) ? start : slotStart;
                LocalTime overlapEnd = end.isBefore(slotEnd) ? end : slotEnd;
                if (overlapStart.isBefore(overlapEnd)) {
                    TimetableBodyCell targetCell = bodyCells.get(createCellKey(day, slotLabel));
                    if (targetCell == null) {
                        continue;
                    }

                    double fillStart = (overlapStart.toSecondOfDay() - slotStart.toSecondOfDay()) / 3600.0;
                    double fillEnd = (overlapEnd.toSecondOfDay() - slotStart.toSecondOfDay()) / 3600.0;
                    targetCell.setFillRange(fillStart, fillEnd, new Color(219, 235, 255));

                    if (firstCoveredSlot) {
                        targetCell.setText(String.format(
                            "<html><b>%s</b><br/>Sec %s<br/>%s-%s</html>",
                            section.getCourse().getCourseCode(),
                            section.getSectionNumber(),
                            section.getTimeBlock().getStartTime(),
                            section.getTimeBlock().getEndTime()
                        ));
                        firstCoveredSlot = false;
                    }
                }
            }
        }
    }

    private String createCellKey(String day, String time) {
        return day + "|" + time;
    }

    private LocalTime parseTimeFlexible(String time) {
        LocalTime parsed = tryParseTime(time, "H:mm");
        if (parsed == null) {
            parsed = tryParseTime(time, "h:mm");
        }
        if (parsed == null) {
            parsed = tryParseTime(time, "h:mm a");
        }
        return parsed;
    }

    private LocalTime tryParseTime(String value, String pattern) {
        try {
            return LocalTime.parse(value.toUpperCase(Locale.ENGLISH),
                DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static class TimetableBodyCell extends JLabel {
        private double fillStartFraction = 0;
        private double fillEndFraction = 0;
        private Color fillColor = new Color(219, 235, 255);

        TimetableBodyCell() {
            super("");
            setOpaque(false);
        }

        void setFillRange(double startFraction, double endFraction, Color color) {
            fillStartFraction = clamp(startFraction);
            fillEndFraction = clamp(endFraction);
            fillColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (fillEndFraction > fillStartFraction) {
                int y = (int) Math.round(getHeight() * fillStartFraction);
                int h = (int) Math.round(getHeight() * (fillEndFraction - fillStartFraction));
                g.setColor(fillColor);
                g.fillRect(0, y, getWidth(), h);
            }

            super.paintComponent(g);
        }

        private double clamp(double value) {
            if (value < 0) {
                return 0;
            }
            if (value > 1) {
                return 1;
            }
            return value;
        }
    }
}
