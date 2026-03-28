package com.coursely.service;

import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods related to schedule conflict detection.
 */
public class ScheduleService {

    /**
     * Finds all time conflicts between sections in a schedule.
     * Each conflict is returned as a readable message.
     *
     * @param schedule the schedule to inspect
     * @return a list of conflict messages
     */
    public static List<String> findConflicts(Schedule schedule) {
        List<String> conflicts = new ArrayList<>();

        // Retrieve all sections currently in the schedule.
        List<Section> sections = schedule.getSections();

        // Compare each section against every later section in the list.
        // Starting j at i + 1 avoids duplicate comparisons and self-comparisons.
        for (int i = 0; i < sections.size(); i++) {
            for (int j = i + 1; j < sections.size(); j++) {

                Section s1 = sections.get(i);
                Section s2 = sections.get(j);

                // Compare all time blocks from the first section with all time blocks
                // from the second section to detect overlaps.
                for (TimeBlock tb1 : s1.getTimeBlocks()) {
                    for (TimeBlock tb2 : s2.getTimeBlocks()) {

                        if (tb1.overlaps(tb2)) {
                            String message =
                                    s1.getSectionCode() + " conflicts with " +
                                    s2.getSectionCode() + " on " +
                                    tb1.getDayOfWeek();

                            conflicts.add(message);
                        }
                    }
                }
            }
        }

        return conflicts;
    }
}