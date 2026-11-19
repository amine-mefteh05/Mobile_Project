package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.unimate.R;
import com.unimate.controller.BudgetController;
import com.unimate.controller.PlannerController;
import com.unimate.controller.TaskController;
import com.unimate.database.PreferencesManager;
import com.unimate.dto.WeeklySummary;
import com.unimate.dto.Weights;
import com.unimate.intelligence.RecommendationEngine;
import com.unimate.model.Student;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private EditText editName, editUniv, editLevel, editDayStart, editDayEnd;
    private SeekBar seekQuiet, seekWifi, seekBudget, seekDistance;
    private TextView textQuietVal, textWifiVal, textBudgetVal, textDistanceVal;
    private SwitchMaterial switchNotifs;
    private Button btnSaveProfile, btnOpenBudget;
    private TextView textSummaryTasks, textSummaryStudyHours, textSummarySpent;

    private PreferencesManager prefsManager;
    private TaskController taskController;
    private PlannerController plannerController;
    private BudgetController budgetController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefsManager = PreferencesManager.getInstance(requireContext());
        taskController = new TaskController(requireContext());
        plannerController = new PlannerController(requireContext());
        budgetController = new BudgetController(requireContext());

        initViews(view);
        setupSeekBars();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void initViews(View view) {
        editName = view.findViewById(R.id.editProfileName);
        editUniv = view.findViewById(R.id.editProfileUniv);
        editLevel = view.findViewById(R.id.editProfileLevel);
        editDayStart = view.findViewById(R.id.editProfileDayStart);
        editDayEnd = view.findViewById(R.id.editProfileDayEnd);

        seekQuiet = view.findViewById(R.id.seekQuietWeight);
        seekWifi = view.findViewById(R.id.seekWifiWeight);
        seekBudget = view.findViewById(R.id.seekBudgetWeight);
        seekDistance = view.findViewById(R.id.seekDistanceWeight);

        textQuietVal = view.findViewById(R.id.textQuietWeightVal);
        textWifiVal = view.findViewById(R.id.textWifiWeightVal);
        textBudgetVal = view.findViewById(R.id.textBudgetWeightVal);
        textDistanceVal = view.findViewById(R.id.textDistanceWeightVal);

        switchNotifs = view.findViewById(R.id.switchProfileNotifs);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnOpenBudget = view.findViewById(R.id.btnProfileOpenBudget);

        textSummaryTasks = view.findViewById(R.id.textSummaryTasks);
        textSummaryStudyHours = view.findViewById(R.id.textSummaryStudyHours);
        textSummarySpent = view.findViewById(R.id.textSummarySpent);
    }

    private void setupSeekBars() {
        setupSeekBarListener(seekQuiet, textQuietVal);
        setupSeekBarListener(seekWifi, textWifiVal);
        setupSeekBarListener(seekBudget, textBudgetVal);
        setupSeekBarListener(seekDistance, textDistanceVal);
    }

    private void setupSeekBarListener(SeekBar seekBar, TextView textView) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
        btnOpenBudget.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BudgetActivity.class);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        Student student = prefsManager.getStudentProfile();
        editName.setText(student.getName());
        editUniv.setText(student.getUniversity());
        editLevel.setText(student.getStudyLevel());
        editDayStart.setText(student.getDayStart());
        editDayEnd.setText(student.getDayEnd());
        switchNotifs.setChecked(student.isNotificationsEnabled());

        Weights weights = prefsManager.getWeights();
        seekQuiet.setProgress(weights.getQuietWeight());
        textQuietVal.setText(String.valueOf(weights.getQuietWeight()));

        seekWifi.setProgress(weights.getWifiWeight());
        textWifiVal.setText(String.valueOf(weights.getWifiWeight()));

        seekBudget.setProgress(weights.getBudgetWeight());
        textBudgetVal.setText(String.valueOf(weights.getBudgetWeight()));

        seekDistance.setProgress(weights.getDistanceWeight());
        textDistanceVal.setText(String.valueOf(weights.getDistanceWeight()));

        // Weekly Productivity Summary
        WeeklySummary summary = RecommendationEngine.weeklySummary(
                taskController.getPendingTasks(),
                plannerController.getAllEvents(),
                budgetController.getCurrentWeekExpenses()
        );

        if (summary != null) {
            textSummaryTasks.setText(String.format(Locale.getDefault(), "Completed: %d/%d (%.0f%%)",
                    summary.getCompletedTasks(), summary.getTotalTasks(), summary.getCompletionRate()));
            textSummaryStudyHours.setText(String.format(Locale.getDefault(), "Study Sessions: %.1f hrs", summary.getTotalStudyHours()));
            textSummarySpent.setText(String.format(Locale.getDefault(), "Spent This Week: %.2f TND", summary.getTotalSpent()));
        }
    }

    private void saveProfileData() {
        Student student = prefsManager.getStudentProfile();
        student.setName(editName.getText().toString().trim());
        student.setUniversity(editUniv.getText().toString().trim());
        student.setStudyLevel(editLevel.getText().toString().trim());
        student.setDayStart(editDayStart.getText().toString().trim());
        student.setDayEnd(editDayEnd.getText().toString().trim());
        student.setNotificationsEnabled(switchNotifs.isChecked());

        prefsManager.saveStudentProfile(student);

        Weights weights = new Weights(
                seekQuiet.getProgress(),
                seekWifi.getProgress(),
                seekBudget.getProgress(),
                seekDistance.getProgress()
        );
        prefsManager.saveWeights(weights);

        Toast.makeText(requireContext(), "Profile and preferences saved!", Toast.LENGTH_SHORT).show();
    }
}
