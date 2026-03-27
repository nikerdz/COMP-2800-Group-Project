package com.coursely.service;

import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

import java.util.ArrayList;
import java.util.List;

public class ScheduleService {

    /**
     * Find all conflicting sections inside a schedule
     */
    public static List<String> findConflicts(Schedule schedule) {
        List<String> conflicts = new ArrayList<>();

        List<Section> sections = schedule.getSections();

        // Compare every pair of sections
        for (int i = 0; i < sections.size(); i++) {
            for (int j = i + 1; j < sections.size(); j++) {

                Section s1 = sections.get(i);
                Section s2 = sections.get(j);

                // Compare all time blocks
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