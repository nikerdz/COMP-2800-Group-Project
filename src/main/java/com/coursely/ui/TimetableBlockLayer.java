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

/**
 * Transparent overlay panel that renders timetable blocks
 * on top of the static grid.
 */
public class TimetableBlockLayer extends JPanel {

    // Earliest visible time represented by the timetable grid.
    private static final LocalTime GRID_START = LocalTime.of(8, 0);

    // Ordered day labels used to calculate horizontal placement.
    private final String[] days;

    // Visible time slots used to calculate row sizing.
    private final String[] timeSlots;

    // Supplier that returns the current set of sections to render.
    private final Supplier<List<Section>> sectionsSupplier;

    // Provides display text for a section based on its UI id.
    private final java.util.function.Function<String, String> displayTypeSupplier;

    // Checks whether a section is currently selected.
    private final Predicate<String> selectedChecker;

    // Callback invoked when a section is selected.
    private final Consumer<String> onSelectSection;

    // Callback invoked when selection should be cleared.
    private final Runnable onClearSelection;

    // Callback invoked when a section should be edited.
    private final Consumer<String> onEditSection;

    // Callback invoked when a section should be deleted.
    private final Consumer<String> onDeleteSection;

    /**
     * Creates the overlay layer used to place timetable blocks.
     *
     * @param days the ordered day labels
     * @param timeSlots the visible time slot labels
     * @param sectionsSupplier supplies the current sections to render
     * @param displayTypeSupplier supplies display text for a section
     * @param selectedChecker checks whether a section is selected
     * @param onSelectSection callback for section selection
     * @param onClearSelection callback for clearing selection
     * @param onEditSection callback for editing a section
     * @param onDeleteSection callback for deleting a section
     */
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

        // Clicking on empty space in the overlay clears the current selection.
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

    /**
     * Rebuilds all visible block components from the current section data.
     * One block view is created for each time block in each section.
     */
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

    /**
     * Lays out block components after the panel size changes.
     */
    @Override
    public void doLayout() {
        super.doLayout();
        layoutBlocks();
    }

    /**
     * Positions each timetable block according to its day, start time,
     * and duration relative to the visible grid.
     */
    private void layoutBlocks() {
        int totalCols = days.length + 1;
        int totalRows = timeSlots.length + 1;

        // The first column is reserved for time labels,
        // and the first row is reserved for day headers.
        int colWidth = getWidth() / totalCols;
        int rowHeight = getHeight() / totalRows;

        for (Component c : getComponents()) {
            if (!(c instanceof BlockView block)) continue;

            TimeBlock tb = block.getTimeBlock();
            int dayIndex = dayIndex(tb.getDayOfWeek());
            if (dayIndex < 0) continue;

            // Offset by one column to skip the time-label column.
            int x = (dayIndex + 1) * colWidth + 1;

            double startMinutes = Duration.between(GRID_START, tb.getStartTime()).toMinutes();
            double durationMinutes = Duration.between(tb.getStartTime(), tb.getEndTime()).toMinutes();

            // Offset by one row to skip the header row.
            int y = rowHeight + (int) Math.round((startMinutes / 60.0) * rowHeight) + 1;

            // Enforce a minimum height so short blocks remain readable.
            int h = Math.max(34, (int) Math.round((durationMinutes / 60.0) * rowHeight) - 2);
            int w = colWidth - 2;

            block.setBounds(x, y, w, h);
        }
    }

    /**
     * Finds the array index for a day label.
     *
     * @param day the day label
     * @return the matching index, or -1 if not found
     */
    private int dayIndex(String day) {
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(day)) return i;
        }
        return -1;
    }
}