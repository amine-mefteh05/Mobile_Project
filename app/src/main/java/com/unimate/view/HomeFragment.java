package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.unimate.R;
import com.unimate.adapter.EventAdapter;
import com.unimate.controller.BudgetController;
import com.unimate.controller.OpportunityController;
import com.unimate.controller.PlannerController;
import com.unimate.controller.StudySpotController;
import com.unimate.controller.TaskController;
import com.unimate.database.PreferencesManager;
import com.unimate.dto.Suggestion;
import com.unimate.intelligence.RecommendationEngine;
import com.unimate.model.Event;
import com.unimate.model.Student;
import com.unimate.model.StudyPlace;
import com.unimate.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView textGreeting, textDate, textTodayProgress;
    private TextView textSuggestionTitle, textSuggestionDesc, textSuggestionReason;
    private TextView textRecommendedSpot;
    private Button btnWhatNowAction, btnQuickAddTask, btnQuickAddEvent, btnQuickBudget;
    private MaterialCardView cardBudgetBanner;
    private TextView textBudgetBannerText;
    private RecyclerView recyclerViewTodayAgenda;
    private TextView textEmptyAgenda;

    private TaskController taskController;
    private PlannerController plannerController;
    private StudySpotController spotController;
    private BudgetController budgetController;
    private PreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        taskController = new TaskController(requireContext());
        plannerController = new PlannerController(requireContext());
        spotController = new StudySpotController(requireContext());
        budgetController = new BudgetController(requireContext());
        prefsManager = PreferencesManager.getInstance(requireContext());

        initViews(view);
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHomeData();
    }

    private void initViews(View view) {
        textGreeting = view.findViewById(R.id.textHomeGreeting);
        textDate = view.findViewById(R.id.textHomeDate);
        textTodayProgress = view.findViewById(R.id.textTodayProgress);

        textSuggestionTitle = view.findViewById(R.id.textSuggestionTitle);
        textSuggestionDesc = view.findViewById(R.id.textSuggestionDesc);
        textSuggestionReason = view.findViewById(R.id.textSuggestionReason);
        textRecommendedSpot = view.findViewById(R.id.textRecommendedSpot);
        btnWhatNowAction = view.findViewById(R.id.btnWhatNowAction);

        btnQuickAddTask = view.findViewById(R.id.btnQuickAddTask);
        btnQuickAddEvent = view.findViewById(R.id.btnQuickAddEvent);
        btnQuickBudget = view.findViewById(R.id.btnQuickBudget);

        cardBudgetBanner = view.findViewById(R.id.cardHomeBudgetBanner);
        textBudgetBannerText = view.findViewById(R.id.textHomeBudgetBanner);

        recyclerViewTodayAgenda = view.findViewById(R.id.recyclerViewTodayAgenda);
        textEmptyAgenda = view.findViewById(R.id.textEmptyAgenda);

        recyclerViewTodayAgenda.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupClickListeners() {
        btnQuickAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TaskFormActivity.class);
            startActivity(intent);
        });

        btnQuickAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventFormActivity.class);
            startActivity(intent);
        });

        btnQuickBudget.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BudgetActivity.class);
            startActivity(intent);
        });

        cardBudgetBanner.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BudgetActivity.class);
            startActivity(intent);
        });
    }

    private void loadHomeData() {
        try {
            Student student = prefsManager.getStudentProfile();
            textGreeting.setText("Welcome back, " + (student.getName() != null ? student.getName() : "Student") + "!");

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
            textDate.setText(today.format(formatter));

            // Today's Progress
            textTodayProgress.setText("Task Progress: " + taskController.getTodayProgress());

            // Budget Warning
            boolean isTight = budgetController.isBudgetTight();
            cardBudgetBanner.setVisibility(isTight ? View.VISIBLE : View.GONE);
            if (isTight) {
                textBudgetBannerText.setText(String.format("Warning: Spent %.2f TND of %.2f TND limit!",
                        budgetController.getCurrentWeekSpent(), budgetController.getWeeklyBudgetLimit()));
            }

            // Today's Agenda
            List<Event> todayEvents = plannerController.getTodayAgenda();
            if (todayEvents == null || todayEvents.isEmpty()) {
                textEmptyAgenda.setVisibility(View.VISIBLE);
                recyclerViewTodayAgenda.setVisibility(View.GONE);
            } else {
                textEmptyAgenda.setVisibility(View.GONE);
                recyclerViewTodayAgenda.setVisibility(View.VISIBLE);
                EventAdapter adapter = new EventAdapter(requireContext(), todayEvents, null);
                recyclerViewTodayAgenda.setAdapter(adapter);
            }

            // Smart Assistant: "What should I do now?"
            List<Task> pendingTasks = taskController.getPendingTasks();
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            StudyPlace topPlace = spotController.getTopRecommendedPlace();

            Suggestion suggestion = RecommendationEngine.whatNow(
                    pendingTasks, todayEvents, currentTime,
                    prefsManager.getDayStart(), prefsManager.getDayEnd(), topPlace
            );

            if (suggestion != null) {
                textSuggestionTitle.setText(suggestion.getTitle());
                textSuggestionDesc.setText(suggestion.getDescription());
                textSuggestionReason.setText(suggestion.getReason());

                if (suggestion.getRecommendedPlace() != null) {
                    textRecommendedSpot.setVisibility(View.VISIBLE);
                    textRecommendedSpot.setText("Suggested Spot: " + suggestion.getRecommendedPlace().getName());
                } else {
                    textRecommendedSpot.setVisibility(View.GONE);
                }

                if (suggestion.getRecommendedTask() != null) {
                    btnWhatNowAction.setVisibility(View.VISIBLE);
                    btnWhatNowAction.setText("Start Task");
                    btnWhatNowAction.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), TaskFormActivity.class);
                        intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, suggestion.getRecommendedTask().getId());
                        startActivity(intent);
                    });
                } else {
                    btnWhatNowAction.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            // Defensive behavior
        }
    }
}
