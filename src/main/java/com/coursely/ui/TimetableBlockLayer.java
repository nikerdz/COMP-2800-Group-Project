package com.coursely.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JPanel;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

public class TimetableBlockLayer extends JPanel {

    private static final LocalTime GRID_START = LocalTime.of(8, 0);

    private final String[] days;
    private final String[] timeSlots;
    private final Supplier<List<Section>> sectionsSupplier;
    private final java.util.function.Function<String, String> displayTypeSupplier;
    private final Predicate<String> selectedChecker;
    private final Consumer<String> onSelectSection;
    private final Runnable onClearSelection;
    private final Consumer<String> onEditSection;
    private final Consumer<String> onDeleteSection;

    public TimetableBlockLayer(
            String[] days,
            String[] timeSlots,
            Supplier<List<Section>> sectionsSupplier,
            java.util.function.Function<String, String> displayTypeSupplier,
            Predicate<String> selectedChecker,
            Consumer<String> onSelectSection,
            Runnable onClearSelection,
            Consumer<String> onEditSection,
            Consumer<String> onDeleteSection
    ) {
        this.days = days;
        this.timeSlots = timeSlots;
        this.sectionsSupplier = sectionsSupplier;
        this.displayTypeSupplier = displayTypeSupplier;
        this.selectedChecker = selectedChecker;
        this.onSelectSection = onSelectSection;
        this.onClearSelection = onClearSelection;
        this.onEditSection = onEditSection;
        this.onDeleteSection = onDeleteSection;

        setLayout(null);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Component clicked = getComponentAt(e.getPoint());
                if (clicked == TimetableBlockLayer.this) {
                    onClearSelection.run();
                }
            }
        });
    }

    public void rebuildBlocks() {
        removeAll();

        for (Section section : sectionsSupplier.get()) {
            for (TimeBlock timeBlock : section.getTimeBlocks()) {
                BlockView block = new BlockView(
                        section,
                        timeBlock,
                        displayTypeSupplier.apply(section.getUiId()),
                        selectedChecker.test(section.getUiId()),
                        onSelectSection,
                        onEditSection,
                        onDeleteSection
                );
                add(block);
            }
        }

        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        layoutBlocks();
    }

    private void layoutBlocks() {
        int totalCols = days.length + 1;
        int totalRows = timeSlots.length + 1;

        int colWidth = getWidth() / totalCols;
        int rowHeight = getHeight() / totalRows;

        for (Component c : getComponents()) {
            if (!(c instanceof BlockView block)) continue;

            TimeBlock tb = block.getTimeBlock();
            int dayIndex = dayIndex(tb.getDayOfWeek());
            if (dayIndex < 0) continue;

            int x = (dayIndex + 1) * colWidth + 1;

            double startMinutes = Duration.between(GRID_START, tb.getStartTime()).toMinutes();
            double durationMinutes = Duration.between(tb.getStartTime(), tb.getEndTime()).toMinutes();

            int y = rowHeight + (int) Math.round((startMinutes / 60.0) * rowHeight) + 1;
            int h = Math.max(34, (int) Math.round((durationMinutes / 60.0) * rowHeight) - 2);
            int w = colWidth - 2;

            block.setBounds(x, y, w, h);
        }
    }

    private int dayIndex(String day) {
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(day)) return i;
        }
        return -1;
    }
}