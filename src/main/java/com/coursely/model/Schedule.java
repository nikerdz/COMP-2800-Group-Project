package com.coursely.model;

import java.util.ArrayList;
import java.util.List;

public class Schedule {

    private List<Section> sections;

    public Schedule() {
        sections = new ArrayList<>();
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    public List<Section> getSections() {
        return sections;
    }

    public void printSchedule() {
        for (Section s : sections) {
            System.out.println(s);
        }
    }
}