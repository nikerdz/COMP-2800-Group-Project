package com.coursely.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Schedule {

    private final List<Section> sections = new ArrayList<>();

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    public boolean removeSectionById(String id) {
        return sections.removeIf(s -> s.getId().equals(id));
    }

    public Optional<Section> findSectionById(String id) {
        return sections.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public boolean updateSection(String id, Section updated) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId().equals(id)) {
                // keep original id by replacing fields instead of object
                Section existing = sections.get(i);
                existing.setTitle(updated.getTitle());
                existing.setType(updated.getType());
                existing.setColor(updated.getColor());
                existing.setTimeBlock(updated.getTimeBlock());
                return true;
            }
        }
        return false;
    }

    // Optional: helper for conflict detection later
    public List<Section> findConflicts(Section candidate) {
        List<Section> conflicts = new ArrayList<>();
        for (Section s : sections) {
            if (s.getTimeBlock().overlaps(candidate.getTimeBlock())) {
                conflicts.add(s);
            }
        }
        return conflicts;
    }
}