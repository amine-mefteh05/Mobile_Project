package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unimate.R;
import com.unimate.adapter.EventAdapter;
import com.unimate.controller.PlannerController;
import com.unimate.model.Event;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlannerFragment extends Fragment {

    private ChipGroup chipGroupDays;
    private RecyclerView recyclerViewAgenda;
    private TextView textEmptyEvents;
    private Button btnPlanMyWeek;
    private FloatingActionButton fabAddEvent;

    private PlannerController plannerController;
    private LocalDate selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);

        plannerController = new PlannerController(requireContext());
        selectedDate = LocalDate.now();

        initViews(view);
        setupWeekDaysChips();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAgendaForSelectedDate();
    }

    private void initViews(View view) {
        chipGroupDays = view.findViewById(R.id.chipGroupDays);
        recyclerViewAgenda = view.findViewById(R.id.recyclerViewAgenda);
        textEmptyEvents = view.findViewById(R.id.textEmptyPlannerEvents);
        btnPlanMyWeek = view.findViewById(R.id.btnPlanMyWeek);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);

        recyclerViewAgenda.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupWeekDaysChips() {
        chipGroupDays.removeAllViews();
        List<String> weekDates = plannerController.getWeekDates(LocalDate.now());

        for (int i = 0; i < weekDates.size(); i++) {
            String dateStr = weekDates.get(i);
            LocalDate date = LocalDate.parse(dateStr);

            Chip chip = new Chip(requireContext());
            chip.setCheckable(true);
            chip.setText(date.getDayOfWeek().name().substring(0, 3) + " " + date.getDayOfMonth());

            if (date.equals(selectedDate)) {
                chip.setChecked(true);
            }

            chip.setOnClickListener(v -> {
                selectedDate = date;
                loadAgendaForSelectedDate();
            });

            chipGroupDays.addView(chip);
        }
    }

    private void setupClickListeners() {
        btnPlanMyWeek.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlanProposalActivity.class);
            startActivity(intent);
        });

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventFormActivity.class);
            intent.putExtra(EventFormActivity.EXTRA_EVENT_DATE, selectedDate.toString());
            startActivity(intent);
        });
    }

    private void loadAgendaForSelectedDate() {
        if (selectedDate == null) selectedDate = LocalDate.now();

        List<Event> events = plannerController.getEventsForDate(selectedDate.toString());

        if (events == null || events.isEmpty()) {
            textEmptyEvents.setVisibility(View.VISIBLE);
            recyclerViewAgenda.setVisibility(View.GONE);
        } else {
            textEmptyEvents.setVisibility(View.GONE);
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
                    boolean success = plannerController.deleteEvent(event.getId());
                    if (success) {
                        Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                        loadAgendaForSelectedDate();
                    }
                }
            });
            recyclerViewAgenda.setAdapter(adapter);
        }
    }
}
