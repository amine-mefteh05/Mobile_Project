package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.unimate.R;
import com.unimate.controller.BudgetController;
import com.unimate.controller.PlannerController;
import com.unimate.controller.TaskController;
import com.unimate.database.PreferencesManager;
import com.unimate.dto.WeeklySummary;
import com.unimate.dto.Weights;
import com.unimate.intelligence.RecommendationEngine;
import com.unimate.model.Student;
import com.unimate.view.ui.UiKit;

import java.time.LocalTime;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private EditText editName, editUniv, editLevel, editDayStart, editDayEnd;
    private Slider seekQuiet, seekWifi, seekBudget, seekDistance;
    private TextView textQuietVal, textWifiVal, textBudgetVal, textDistanceVal;
    private TextView textInitials, textDisplayName, textMeta, textBudgetCaption;
    private View statTasks, statStudy, statSpent;
    private MaterialSwitch switchNotifs;
    private View btnSaveProfile, btnOpenBudget;

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
        setupSliders();
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

        textInitials = view.findViewById(R.id.textProfileInitials);
        textDisplayName = view.findViewById(R.id.textProfileDisplayName);
        textMeta = view.findViewById(R.id.textProfileMeta);
        textBudgetCaption = view.findViewById(R.id.textProfileBudgetCaption);

        statTasks = view.findViewById(R.id.statProfileTasks);
        statStudy = view.findViewById(R.id.statProfileStudy);
        statSpent = view.findViewById(R.id.statProfileSpent);

        switchNotifs = view.findViewById(R.id.switchProfileNotifs);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnOpenBudget = view.findViewById(R.id.btnProfileOpenBudget);

        // Keep the identity header in sync while the user types.
        TextWatcher identityWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { bindIdentity(); }
        };
        editName.addTextChangedListener(identityWatcher);
        editUniv.addTextChangedListener(identityWatcher);
        editLevel.addTextChangedListener(identityWatcher);
    }

    private void setupSliders() {
        bindSliderValue(seekQuiet, textQuietVal);
        bindSliderValue(seekWifi, textWifiVal);
        bindSliderValue(seekBudget, textBudgetVal);
        bindSliderValue(seekDistance, textDistanceVal);
    }

    private void bindSliderValue(Slider slider, TextView valueView) {
        slider.addOnChangeListener((s, value, fromUser) -> valueView.setText(String.valueOf(Math.round(value))));
    }

    private static void setSlider(Slider slider, int value) {
        slider.setValue(Math.max(0, Math.min(100, value)));
    }

    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
        btnOpenBudget.setOnClickListener(v -> startActivity(new Intent(requireContext(), BudgetActivity.class)));

        editDayStart.setOnClickListener(v -> UiKit.pickTime(requireActivity(), "Study day starts",
                editDayStart.getText().toString(), editDayStart::setText));
        editDayEnd.setOnClickListener(v -> UiKit.pickTime(requireActivity(), "Study day ends",
                editDayEnd.getText().toString(), editDayEnd::setText));
    }

    private void bindIdentity() {
        String name = editName.getText().toString().trim();
        String univ = editUniv.getText().toString().trim();
        String level = editLevel.getText().toString().trim();

        textInitials.setText(UiKit.initials(name));
        textDisplayName.setText(name.isEmpty() ? "Your name" : name);

        String meta;
        if (!univ.isEmpty() && !level.isEmpty()) meta = univ + " · " + level;
        else if (!univ.isEmpty()) meta = univ;
        else if (!level.isEmpty()) meta = level;
        else meta = "Add your university and year";
        textMeta.setText(meta);
    }

    private void loadProfileData() {
        Student student = prefsManager.getStudentProfile();
        editName.setText(student.getName());
        editUniv.setText(student.getUniversity());
        editLevel.setText(student.getStudyLevel());
        editDayStart.setText(student.getDayStart());
        editDayEnd.setText(student.getDayEnd());
        switchNotifs.setChecked(student.isNotificationsEnabled());
        bindIdentity();

        Weights weights = prefsManager.getWeights();
        setSlider(seekQuiet, weights.getQuietWeight());
        setSlider(seekWifi, weights.getWifiWeight());
        setSlider(seekBudget, weights.getBudgetWeight());
        setSlider(seekDistance, weights.getDistanceWeight());
        textQuietVal.setText(String.valueOf(Math.round(seekQuiet.getValue())));
        textWifiVal.setText(String.valueOf(Math.round(seekWifi.getValue())));
        textBudgetVal.setText(String.valueOf(Math.round(seekBudget.getValue())));
        textDistanceVal.setText(String.valueOf(Math.round(seekDistance.getValue())));

        // Weekly productivity summary
        WeeklySummary summary = RecommendationEngine.weeklySummary(
                taskController.getPendingTasks(),
                plannerController.getAllEvents(),
                budgetController.getCurrentWeekExpenses()
        );

        if (summary != null) {
            bindStat(statTasks, summary.getCompletedTasks() + "/" + summary.getTotalTasks(), "Tasks done",
                    (int) Math.round(summary.getCompletionRate()));
            bindStat(statStudy, String.format(Locale.getDefault(), "%.1fh", summary.getTotalStudyHours()),
                    "Study time", -1);
            bindStat(statSpent, String.format(Locale.getDefault(), "%.0f", summary.getTotalSpent()),
                    "TND spent", -1);
        }

        double limit = budgetController.getWeeklyBudgetLimit();
        double spent = budgetController.getCurrentWeekSpent();
        textBudgetCaption.setText(String.format(Locale.getDefault(), "%.2f of %.2f TND spent", spent, limit));
    }

    private void bindStat(View tile, String value, String label, int progress) {
        ((TextView) tile.findViewById(R.id.statValue)).setText(value);
        ((TextView) tile.findViewById(R.id.statLabel)).setText(label);
        LinearProgressIndicator bar = tile.findViewById(R.id.statProgress);
        if (progress >= 0) {
            bar.setVisibility(View.VISIBLE);
            bar.setProgressCompat(Math.max(0, Math.min(100, progress)), false);
        } else {
            bar.setVisibility(View.GONE);
        }
    }

    private void saveProfileData() {
        String dayStart = editDayStart.getText().toString().trim();
        String dayEnd = editDayEnd.getText().toString().trim();
        try {
            if (!LocalTime.parse(dayStart).isBefore(LocalTime.parse(dayEnd))) {
                showMessage("Your study day must end after it starts.");
                return;
            }
        } catch (Exception e) {
            showMessage("Pick a start and end time for your study day.");
            return;
        }

        Student student = prefsManager.getStudentProfile();
        student.setName(editName.getText().toString().trim());
        student.setUniversity(editUniv.getText().toString().trim());
        student.setStudyLevel(editLevel.getText().toString().trim());
        student.setDayStart(dayStart);
        student.setDayEnd(dayEnd);
        student.setNotificationsEnabled(switchNotifs.isChecked());

        prefsManager.saveStudentProfile(student);

        Weights weights = new Weights(
                Math.round(seekQuiet.getValue()),
                Math.round(seekWifi.getValue()),
                Math.round(seekBudget.getValue()),
                Math.round(seekDistance.getValue())
        );
        prefsManager.saveWeights(weights);

        showMessage("Changes saved");
    }

    private void showMessage(String message) {
        View root = getView();
        if (root == null) return;
        Snackbar bar = Snackbar.make(root, message, Snackbar.LENGTH_SHORT);
        View nav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (nav != null) bar.setAnchorView(nav);
        bar.show();
    }
}
