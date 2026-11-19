package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.unimate.MainActivity;
import com.unimate.R;
import com.unimate.adapter.EventAdapter;
import com.unimate.controller.BudgetController;
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
import com.unimate.view.ui.UiKit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView textGreeting, textDate, textAvatar, textAssistantTime;
    private TextView textSuggestionTitle, textSuggestionDesc, textSuggestionReason;
    private TextView textRecommendedSpot, textBudgetBannerText;
    private View layoutSuggestionReason, layoutRecommendedSpot;
    private MaterialButton btnWhatNowAction, btnSeePlanner;
    private MaterialCardView cardBudgetBanner;
    private View statTasks, statEvents, statBudget;
    private View btnQuickAddTask, btnQuickAddEvent, btnQuickPlanWeek, btnQuickBudget;
    private RecyclerView recyclerViewTodayAgenda;
    private View emptyAgenda;

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
        textAvatar = view.findViewById(R.id.textHomeAvatar);
        textAssistantTime = view.findViewById(R.id.textAssistantTime);

        textSuggestionTitle = view.findViewById(R.id.textSuggestionTitle);
        textSuggestionDesc = view.findViewById(R.id.textSuggestionDesc);
        textSuggestionReason = view.findViewById(R.id.textSuggestionReason);
        layoutSuggestionReason = view.findViewById(R.id.layoutSuggestionReason);
        textRecommendedSpot = view.findViewById(R.id.textRecommendedSpot);
        layoutRecommendedSpot = view.findViewById(R.id.layoutRecommendedSpot);
        btnWhatNowAction = view.findViewById(R.id.btnWhatNowAction);
        btnSeePlanner = view.findViewById(R.id.btnSeePlanner);

        statTasks = view.findViewById(R.id.statTasks);
        statEvents = view.findViewById(R.id.statEvents);
        statBudget = view.findViewById(R.id.statBudget);

        btnQuickAddTask = view.findViewById(R.id.btnQuickAddTask);
        btnQuickAddEvent = view.findViewById(R.id.btnQuickAddEvent);
        btnQuickPlanWeek = view.findViewById(R.id.btnQuickPlanWeek);
        btnQuickBudget = view.findViewById(R.id.btnQuickBudget);
        bindQuickAction(btnQuickAddTask, R.drawable.ic_add, "Task");
        bindQuickAction(btnQuickAddEvent, R.drawable.ic_event, "Event");
        bindQuickAction(btnQuickPlanWeek, R.drawable.ic_sparkle, "Plan week");
        bindQuickAction(btnQuickBudget, R.drawable.ic_budget, "Budget");

        cardBudgetBanner = view.findViewById(R.id.cardHomeBudgetBanner);
        textBudgetBannerText = view.findViewById(R.id.textHomeBudgetBanner);

        recyclerViewTodayAgenda = view.findViewById(R.id.recyclerViewTodayAgenda);
        emptyAgenda = view.findViewById(R.id.emptyAgenda);
        UiKit.bindEmptyState(emptyAgenda, R.drawable.ic_event,
                "Your day is clear", "Nothing scheduled today. Add an event or plan study time.");

        recyclerViewTodayAgenda.setLayoutManager(new LinearLayoutManager(requireContext()));
        UiKit.addDividers(recyclerViewTodayAgenda);
    }

    private void bindQuickAction(View tile, @DrawableRes int icon, String label) {
        ((ImageView) tile.findViewById(R.id.quickIcon)).setImageResource(icon);
        ((TextView) tile.findViewById(R.id.quickLabel)).setText(label);
        tile.setContentDescription(label);
    }

    private void setupClickListeners() {
        btnQuickAddTask.setOnClickListener(v -> startActivity(new Intent(requireContext(), TaskFormActivity.class)));
        btnQuickAddEvent.setOnClickListener(v -> startActivity(new Intent(requireContext(), EventFormActivity.class)));
        btnQuickPlanWeek.setOnClickListener(v -> startActivity(new Intent(requireContext(), PlanProposalActivity.class)));
        btnQuickBudget.setOnClickListener(v -> startActivity(new Intent(requireContext(), BudgetActivity.class)));
        cardBudgetBanner.setOnClickListener(v -> startActivity(new Intent(requireContext(), BudgetActivity.class)));

        btnSeePlanner.setOnClickListener(v -> switchTab(R.id.nav_planner));
        textAvatar.setOnClickListener(v -> switchTab(R.id.nav_profile));
        statTasks.setOnClickListener(v -> switchTab(R.id.nav_tasks));
        statEvents.setOnClickListener(v -> switchTab(R.id.nav_planner));
        statBudget.setOnClickListener(v -> startActivity(new Intent(requireContext(), BudgetActivity.class)));
    }

    private void switchTab(int navId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateTo(navId);
        }
    }

    private void bindStat(View tile, String value, String label, int progress) {
        ((TextView) tile.findViewById(R.id.statValue)).setText(value);
        ((TextView) tile.findViewById(R.id.statLabel)).setText(label);
        LinearProgressIndicator bar = tile.findViewById(R.id.statProgress);
        if (progress >= 0) {
            bar.setVisibility(View.VISIBLE);
            bar.setProgressCompat(progress, true);
        } else {
            bar.setVisibility(View.GONE);
        }
        tile.setContentDescription(value + " " + label);
    }

    private void loadHomeData() {
        try {
            // Header
            Student student = prefsManager.getStudentProfile();
            String name = student.getName();
            textGreeting.setText(UiKit.greeting() + ", " + UiKit.firstName(name));
            textAvatar.setText(UiKit.initials(name));
            textDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())));

            // At a glance
            List<Task> allTasks = taskController.getFilteredAndSortedTasks("ALL", "ALL", "Deadline");
            int total = allTasks != null ? allTasks.size() : 0;
            int done = 0;
            if (allTasks != null) {
                for (Task t : allTasks) if (t.getStatus() == Task.Status.DONE) done++;
            }
            bindStat(statTasks, done + "/" + total, "Tasks done", total > 0 ? (done * 100 / total) : 0);

            List<Event> todayEvents = plannerController.getTodayAgenda();
            int eventCount = todayEvents != null ? todayEvents.size() : 0;
            bindStat(statEvents, String.valueOf(eventCount), eventCount == 1 ? "Event today" : "Events today", -1);

            double remaining = budgetController.getRemainingBudget();
            bindStat(statBudget, String.format(Locale.getDefault(), "%.0f", remaining), "TND left this week", -1);

            // Budget warning
            boolean isTight = budgetController.isBudgetTight();
            cardBudgetBanner.setVisibility(isTight ? View.VISIBLE : View.GONE);
            if (isTight && getView() != null) {
                boolean over = budgetController.getCurrentWeekSpent() > budgetController.getWeeklyBudgetLimit();
                ((android.widget.TextView) getView().findViewById(R.id.textHomeBudgetBannerTitle))
                        .setText(over ? "Over budget this week" : "Budget almost spent");
                textBudgetBannerText.setText(String.format(Locale.getDefault(), "%.2f of %.2f TND used this week",
                        budgetController.getCurrentWeekSpent(), budgetController.getWeeklyBudgetLimit()));
            }

            // Today's agenda (read-only here; editing lives in the Planner)
            if (todayEvents == null || todayEvents.isEmpty()) {
                emptyAgenda.setVisibility(View.VISIBLE);
                recyclerViewTodayAgenda.setVisibility(View.GONE);
            } else {
                emptyAgenda.setVisibility(View.GONE);
                recyclerViewTodayAgenda.setVisibility(View.VISIBLE);
                recyclerViewTodayAgenda.setAdapter(new EventAdapter(requireContext(), todayEvents, null));
            }

            // Assistant: "What should I do now?"
            List<Task> pendingTasks = taskController.getPendingTasks();
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            textAssistantTime.setText(currentTime);
            StudyPlace topPlace = spotController.getTopRecommendedPlace();

            Suggestion suggestion = RecommendationEngine.whatNow(
                    pendingTasks, todayEvents, currentTime,
                    prefsManager.getDayStart(), prefsManager.getDayEnd(), topPlace
            );

            if (suggestion != null) {
                bindSuggestion(suggestion);
            } else {
                textSuggestionTitle.setText("You're all caught up");
                textSuggestionDesc.setText("No suggestion right now. Add tasks to get personalised recommendations.");
                layoutSuggestionReason.setVisibility(View.GONE);
                layoutRecommendedSpot.setVisibility(View.GONE);
                btnWhatNowAction.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            // Defensive behavior: keep the screen usable even if one data source fails.
        }
    }

    private void bindSuggestion(Suggestion suggestion) {
        textSuggestionTitle.setText(suggestion.getTitle());
        textSuggestionDesc.setText(suggestion.getDescription());

        String reason = suggestion.getReason();
        boolean hasReason = reason != null && !reason.trim().isEmpty();
        layoutSuggestionReason.setVisibility(hasReason ? View.VISIBLE : View.GONE);
        textSuggestionReason.setText(hasReason ? reason : "");

        StudyPlace place = suggestion.getRecommendedPlace();
        if (place != null) {
            layoutRecommendedSpot.setVisibility(View.VISIBLE);
            textRecommendedSpot.setText("Best spot right now: " + place.getName());
            layoutRecommendedSpot.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), StudySpotDetailsActivity.class);
                intent.putExtra(StudySpotDetailsActivity.EXTRA_PLACE_ID, place.getId());
                startActivity(intent);
            });
        } else {
            layoutRecommendedSpot.setVisibility(View.GONE);
        }

        Task task = suggestion.getRecommendedTask();
        if (task != null) {
            btnWhatNowAction.setVisibility(View.VISIBLE);
            btnWhatNowAction.setText("Open task");
            btnWhatNowAction.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TaskFormActivity.class);
                intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, task.getId());
                startActivity(intent);
            });
        } else {
            btnWhatNowAction.setVisibility(View.GONE);
        }
    }
}
