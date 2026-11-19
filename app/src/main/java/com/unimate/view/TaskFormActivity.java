package com.unimate.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unimate.R;
import com.unimate.controller.TaskController;
import com.unimate.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;

public class TaskFormActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextInputEditText editTitle, editDescription, editDeadline, editDuration, editReminderTime;
    private TextInputLayout layoutReminderTime;
    private Spinner spinnerCategory;
    private RadioGroup radioGroupPriority;
    private RadioButton radioLow, radioMedium, radioHigh;
    private SwitchMaterial switchReminder;
    private Button btnSaveTask;

    private TaskController taskController;
    private long taskId = -1;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        taskController = new TaskController(this);

        Toolbar toolbar = findViewById(R.id.toolbarTaskForm);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupSpinner();

        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId > 0) {
            setTitle("Edit Task");
            loadTaskData();
        } else {
            setTitle("Create New Task");
            currentTask = new Task();
        }

        setupListeners();
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTaskTitle);
        editDescription = findViewById(R.id.editTaskDescription);
        editDeadline = findViewById(R.id.editTaskDeadline);
        editDuration = findViewById(R.id.editTaskDuration);
        editReminderTime = findViewById(R.id.editReminderTime);
        layoutReminderTime = findViewById(R.id.layoutReminderTime);
        spinnerCategory = findViewById(R.id.spinnerTaskCategory);
        radioGroupPriority = findViewById(R.id.radioGroupPriority);
        radioLow = findViewById(R.id.radioLow);
        radioMedium = findViewById(R.id.radioMedium);
        radioHigh = findViewById(R.id.radioHigh);
        switchReminder = findViewById(R.id.switchReminder);
        btnSaveTask = findViewById(R.id.btnSaveTask);
    }

    private void setupSpinner() {
        String[] categories = {"UNIVERSITY", "PERSONAL", "PROJECT", "EXAM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        editDeadline.setOnClickListener(v -> showDatePicker());
        editReminderTime.setOnClickListener(v -> showTimePicker());

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void loadTaskData() {
        currentTask = taskController.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTitle.setText(currentTask.getTitle());
        editDescription.setText(currentTask.getDescription());
        editDeadline.setText(currentTask.getDeadline());
        editDuration.setText(String.valueOf(currentTask.getDuration()));

        if (currentTask.getCategory() != null) {
            int pos = ((ArrayAdapter<String>) spinnerCategory.getAdapter()).getPosition(currentTask.getCategory().name());
            if (pos >= 0) spinnerCategory.setSelection(pos);
        }

        if (currentTask.getPriority() == Task.Priority.HIGH) {
            radioHigh.setChecked(true);
        } else if (currentTask.getPriority() == Task.Priority.LOW) {
            radioLow.setChecked(true);
        } else {
            radioMedium.setChecked(true);
        }

        switchReminder.setChecked(currentTask.isHasReminder());
        layoutReminderTime.setVisibility(currentTask.isHasReminder() ? View.VISIBLE : View.GONE);
        editReminderTime.setText(currentTask.getReminderTime() != null ? currentTask.getReminderTime() : "09:00");
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editDeadline.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
            editReminderTime.setText(selectedTime);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void saveTask() {
        String title = editTitle.getText() != null ? editTitle.getText().toString() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString() : "";
        String deadline = editDeadline.getText() != null ? editDeadline.getText().toString() : "";
        String durationStr = editDuration.getText() != null ? editDuration.getText().toString() : "";
        String reminderTime = editReminderTime.getText() != null ? editReminderTime.getText().toString() : "09:00";

        // Validate via Controller
        String error = taskController.validateTaskInput(title, deadline, durationStr);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentTask == null) currentTask = new Task();
        currentTask.setTitle(title.trim());
        currentTask.setDescription(description.trim());
        currentTask.setDeadline(deadline.trim());
        currentTask.setDuration(Integer.parseInt(durationStr.trim()));

        String selectedCat = (String) spinnerCategory.getSelectedItem();
        try { currentTask.setCategory(Task.Category.valueOf(selectedCat)); } catch (Exception ignored) {}

        if (radioHigh.isChecked()) {
            currentTask.setPriority(Task.Priority.HIGH);
        } else if (radioLow.isChecked()) {
            currentTask.setPriority(Task.Priority.LOW);
        } else {
            currentTask.setPriority(Task.Priority.MEDIUM);
        }

        currentTask.setHasReminder(switchReminder.isChecked());
        currentTask.setReminderTime(switchReminder.isChecked() ? reminderTime.trim() : null);

        boolean success = taskController.saveTask(currentTask);
        if (success) {
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
