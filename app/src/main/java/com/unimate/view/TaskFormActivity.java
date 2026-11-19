package com.unimate.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unimate.R;
import com.unimate.controller.TaskController;
import com.unimate.model.Task;
import com.unimate.view.ui.UiKit;

import java.util.Locale;

public class TaskFormActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextInputEditText editTitle, editDescription, editDeadline, editDuration, editReminderTime;
    private TextInputLayout layoutTitle, layoutDeadline, layoutDuration, layoutReminderTime;
    private ChipGroup chipGroupCategory;
    private MaterialButtonToggleGroup togglePriority;
    private MaterialSwitch switchReminder;
    private View btnSaveTask;

    private TaskController taskController;
    private long taskId = -1;
    private Task currentTask;
    /** ISO yyyy-MM-dd; the field itself shows a friendly date. */
    private String deadlineIso = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        taskController = new TaskController(this);
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);

        UiKit.setupSecondaryScreen(this, R.id.toolbarTaskForm,
                taskId > 0 ? "Edit task" : "New task", R.drawable.ic_close, "Close");
        UiKit.padForSystemBars(findViewById(R.id.bottomBarTaskForm), false, true);

        initViews();

        if (taskId > 0) {
            loadTaskData();
        } else {
            currentTask = new Task();
            editDuration.setText("60");
        }

        setupListeners();
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTaskTitle);
        editDescription = findViewById(R.id.editTaskDescription);
        editDeadline = findViewById(R.id.editTaskDeadline);
        editDuration = findViewById(R.id.editTaskDuration);
        editReminderTime = findViewById(R.id.editReminderTime);
        layoutTitle = findViewById(R.id.layoutTaskTitle);
        layoutDeadline = findViewById(R.id.layoutTaskDeadline);
        layoutDuration = findViewById(R.id.layoutTaskDuration);
        layoutReminderTime = findViewById(R.id.layoutReminderTime);
        chipGroupCategory = findViewById(R.id.chipGroupTaskCategory);
        togglePriority = findViewById(R.id.togglePriority);
        switchReminder = findViewById(R.id.switchReminder);
        btnSaveTask = findViewById(R.id.btnSaveTask);
    }

    private void setupListeners() {
        editDeadline.setOnClickListener(v -> UiKit.pickDate(this, "Deadline", deadlineIso, this::setDeadline));
        editReminderTime.setOnClickListener(v -> UiKit.pickTime(this, "Reminder time",
                textOf(editReminderTime), editReminderTime::setText));

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) ->
                layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        UiKit.clearErrorOnEdit(layoutTitle, layoutDuration);
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void setDeadline(String iso) {
        deadlineIso = iso == null ? "" : iso;
        editDeadline.setText(deadlineIso.isEmpty() ? "" : UiKit.prettyDate(deadlineIso));
        layoutDeadline.setError(null);
        layoutDeadline.setHelperText(deadlineIso.isEmpty() ? null : UiKit.relativeDue(deadlineIso));
    }

    private void loadTaskData() {
        currentTask = taskController.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "This task no longer exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTitle.setText(currentTask.getTitle());
        editDescription.setText(currentTask.getDescription());
        setDeadline(currentTask.getDeadline());
        editDuration.setText(String.valueOf(currentTask.getDuration()));

        if (currentTask.getCategory() != null) {
            checkChipWithTag(currentTask.getCategory().name());
        }

        if (currentTask.getPriority() == Task.Priority.HIGH) {
            togglePriority.check(R.id.radioHigh);
        } else if (currentTask.getPriority() == Task.Priority.LOW) {
            togglePriority.check(R.id.radioLow);
        } else {
            togglePriority.check(R.id.radioMedium);
        }

        switchReminder.setChecked(currentTask.isHasReminder());
        layoutReminderTime.setVisibility(currentTask.isHasReminder() ? View.VISIBLE : View.GONE);
        editReminderTime.setText(currentTask.getReminderTime() != null ? currentTask.getReminderTime() : "09:00");
    }

    private void checkChipWithTag(String tag) {
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            View child = chipGroupCategory.getChildAt(i);
            if (child instanceof Chip && tag.equals(child.getTag())) {
                chipGroupCategory.check(child.getId());
                return;
            }
        }
    }

    private static String textOf(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString() : "";
    }

    private void saveTask() {
        String title = textOf(editTitle);
        String description = textOf(editDescription);
        String durationStr = textOf(editDuration);
        String reminderTime = textOf(editReminderTime);
        if (reminderTime.trim().isEmpty()) reminderTime = "09:00";

        layoutTitle.setError(null);
        layoutDeadline.setError(null);
        layoutDuration.setError(null);

        // Validate via controller, then attach the message to the field it concerns.
        String error = taskController.validateTaskInput(title, deadlineIso, durationStr);
        if (error != null) {
            String lower = error.toLowerCase(Locale.ROOT);
            if (lower.contains("title")) {
                layoutTitle.setError(error);
                editTitle.requestFocus();
            } else if (lower.contains("deadline") || lower.contains("date")) {
                layoutDeadline.setError(error);
            } else {
                layoutDuration.setError(error);
                editDuration.requestFocus();
            }
            return;
        }

        if (currentTask == null) currentTask = new Task();
        currentTask.setTitle(title.trim());
        currentTask.setDescription(description.trim());
        currentTask.setDeadline(deadlineIso.trim());
        currentTask.setDuration(Integer.parseInt(durationStr.trim()));

        Chip checked = chipGroupCategory.findViewById(chipGroupCategory.getCheckedChipId());
        if (checked != null && checked.getTag() != null) {
            try { currentTask.setCategory(Task.Category.valueOf(checked.getTag().toString())); } catch (Exception ignored) {}
        }

        int priorityId = togglePriority.getCheckedButtonId();
        if (priorityId == R.id.radioHigh) {
            currentTask.setPriority(Task.Priority.HIGH);
        } else if (priorityId == R.id.radioLow) {
            currentTask.setPriority(Task.Priority.LOW);
        } else {
            currentTask.setPriority(Task.Priority.MEDIUM);
        }

        currentTask.setHasReminder(switchReminder.isChecked());
        currentTask.setReminderTime(switchReminder.isChecked() ? reminderTime.trim() : null);

        boolean success = taskController.saveTask(currentTask);
        if (success) {
            Toast.makeText(this, taskId > 0 ? "Task updated" : "Task added", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Couldn't save the task. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
