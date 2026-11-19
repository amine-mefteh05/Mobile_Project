package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.unimate.R;
import com.unimate.adapter.EventAdapter;
import com.unimate.controller.PlannerController;
import com.unimate.model.Event;
import com.unimate.view.ui.UiKit;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlannerFragment extends Fragment {

    private LinearLayout weekStrip;
    private RecyclerView recyclerViewAgenda;
    private View emptyPlanner;
    private View btnPlanMyWeek;
    private TextView textMonth, textSelectedDay, textDayCount;
    private ExtendedFloatingActionButton fabAddEvent;

    private final List<View> dayCells = new ArrayList<>();
    private PlannerController plannerController;
    private LocalDate selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);

        plannerController = new PlannerController(requireContext());
        selectedDate = LocalDate.now();

        initViews(view);
        setupWeekStrip(inflater);
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAgendaForSelectedDate();
    }

    private void initViews(View view) {
        weekStrip = view.findViewById(R.id.weekStrip);
        recyclerViewAgenda = view.findViewById(R.id.recyclerViewAgenda);
        emptyPlanner = view.findViewById(R.id.emptyPlanner);
        btnPlanMyWeek = view.findViewById(R.id.btnPlanMyWeek);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        textMonth = view.findViewById(R.id.textPlannerMonth);
        textSelectedDay = view.findViewById(R.id.textSelectedDay);
        textDayCount = view.findViewById(R.id.textDayCount);

        recyclerViewAgenda.setLayoutManager(new LinearLayoutManager(requireContext()));
        UiKit.addDividers(recyclerViewAgenda);
        // Shrink the FAB to an icon while scrolling, expand it again at rest.
        recyclerViewAgenda.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 4 && fabAddEvent.isExtended()) fabAddEvent.shrink();
                else if (dy < -4 && !fabAddEvent.isExtended()) fabAddEvent.extend();
            }
        });
    }

    private void setupWeekStrip(LayoutInflater inflater) {
        weekStrip.removeAllViews();
        dayCells.clear();
        LocalDate today = LocalDate.now();
        textMonth.setText(today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())));

        List<String> weekDates = plannerController.getWeekDates(today);
        for (String dateStr : weekDates) {
            LocalDate date = LocalDate.parse(dateStr);
            View cell = inflater.inflate(R.layout.item_day_cell, weekStrip, false);
            ((TextView) cell.findViewById(R.id.textDayName))
                    .setText(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            ((TextView) cell.findViewById(R.id.textDayNumber)).setText(String.valueOf(date.getDayOfMonth()));
            cell.findViewById(R.id.viewTodayDot).setVisibility(date.equals(today) ? View.VISIBLE : View.INVISIBLE);
            cell.setTag(date);
            cell.setContentDescription(date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())));
            cell.setOnClickListener(v -> {
                selectedDate = date;
                updateDaySelection();
                loadAgendaForSelectedDate();
            });
            weekStrip.addView(cell);
            dayCells.add(cell);
        }
        updateDaySelection();
    }

    private void updateDaySelection() {
        for (View cell : dayCells) {
            boolean selected = selectedDate.equals(cell.getTag());
            cell.setSelected(selected);
            // The today dot would vanish on the brand fill; tint it to match.
            cell.findViewById(R.id.viewTodayDot).setBackgroundTintList(
                    UiKit.colorList(requireContext(), selected ? R.color.on_brand : R.color.brand));
        }
    }

    private void setupClickListeners() {
        btnPlanMyWeek.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PlanProposalActivity.class)));

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventFormActivity.class);
            intent.putExtra(EventFormActivity.EXTRA_EVENT_DATE, selectedDate.toString());
            startActivity(intent);
        });
    }

    private void loadAgendaForSelectedDate() {
        if (selectedDate == null) selectedDate = LocalDate.now();

        LocalDate today = LocalDate.now();
        String dayTitle;
        if (selectedDate.equals(today)) dayTitle = "Today";
        else if (selectedDate.equals(today.plusDays(1))) dayTitle = "Tomorrow";
        else if (selectedDate.equals(today.minusDays(1))) dayTitle = "Yesterday";
        else dayTitle = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault()));
        textSelectedDay.setText(dayTitle);

        List<Event> events = plannerController.getEventsForDate(selectedDate.toString());
        int count = events != null ? events.size() : 0;
        textDayCount.setText(UiKit.plural(count, "event"));

        if (count == 0) {
            UiKit.bindEmptyState(emptyPlanner, R.drawable.ic_event,
                    selectedDate.equals(today) ? "Nothing scheduled today" : "Nothing scheduled",
                    "Tap Event to add a class, exam or study block.");
            emptyPlanner.setVisibility(View.VISIBLE);
            recyclerViewAgenda.setVisibility(View.GONE);
            return;
        }

        emptyPlanner.setVisibility(View.GONE);
        recyclerViewAgenda.setVisibility(View.VISIBLE);

        EventAdapter adapter = new EventAdapter(requireContext(), events, new EventAdapter.OnEventActionListener() {
            @Override
            public void onEventClick(Event event) {
                Intent intent = new Intent(requireContext(), EventFormActivity.class);
                intent.putExtra(EventFormActivity.EXTRA_EVENT_ID, event.getId());
                startActivity(intent);
            }

            @Override
            public void onEventDelete(Event event) {
                confirmDelete(event);
            }
        });
        recyclerViewAgenda.setAdapter(adapter);
    }

    private void confirmDelete(Event event) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete event?")
                .setMessage("“" + event.getTitle() + "” will be removed from your planner.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    boolean success = plannerController.deleteEvent(event.getId());
                    if (success && getView() != null) {
                        Snackbar.make(getView(), "Event deleted", Snackbar.LENGTH_SHORT).show();
                        loadAgendaForSelectedDate();
                    }
                })
                .create();
        dialog.show();
    }
}
