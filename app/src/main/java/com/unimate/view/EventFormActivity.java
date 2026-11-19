package com.unimate.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.unimate.R;
import com.unimate.controller.PlannerController;
import com.unimate.model.Event;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;

public class EventFormActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_EVENT_DATE = "extra_event_date";

    private TextInputEditText editTitle, editDate, editStartTime, editEndTime, editLocation;
    private Spinner spinnerType;
    private Button btnSaveEvent;

    private PlannerController plannerController;
    private long eventId = -1;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        plannerController = new PlannerController(this);

        Toolbar toolbar = findViewById(R.id.toolbarEventForm);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupSpinner();

        eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        String prefilledDate = getIntent().getStringExtra(EXTRA_EVENT_DATE);

        if (eventId > 0) {
            setTitle("Edit Event");
            // Fetch and load
            currentEvent = new Event(); // or controller get
        } else {
            setTitle("Create New Event");
            currentEvent = new Event();
            if (prefilledDate != null && !prefilledDate.isEmpty()) {
                editDate.setText(prefilledDate);
            } else {
                editDate.setText(LocalDate.now().toString());
            }
        }

        setupListeners();
    }

    private void initViews() {
        editTitle = findViewById(R.id.editEventTitle);
        editDate = findViewById(R.id.editEventDate);
        editStartTime = findViewById(R.id.editEventStartTime);
        editEndTime = findViewById(R.id.editEventEndTime);
        editLocation = findViewById(R.id.editEventLocation);
        spinnerType = findViewById(R.id.spinnerEventType);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
    }

    private void setupSpinner() {
        String[] types = {"LECTURE", "PRACTICAL", "EXAM", "PERSONAL", "STUDY"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupListeners() {
        editDate.setOnClickListener(v -> showDatePicker());
        editStartTime.setOnClickListener(v -> showTimePicker(true));
        editEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
            if (isStart) {
                editStartTime.setText(selectedTime);
            } else {
                editEndTime.setText(selectedTime);
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void saveEvent() {
        String title = editTitle.getText() != null ? editTitle.getText().toString() : "";
        String date = editDate.getText() != null ? editDate.getText().toString() : "";
        String startTime = editStartTime.getText() != null ? editStartTime.getText().toString() : "";
        String endTime = editEndTime.getText() != null ? editEndTime.getText().toString() : "";
        String location = editLocation.getText() != null ? editLocation.getText().toString() : "";

        String error = plannerController.validateEventInput(title, startTime, endTime);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentEvent == null) currentEvent = new Event();
        currentEvent.setTitle(title.trim());
        currentEvent.setDate(date.trim());
        currentEvent.setStartTime(startTime.trim());
        currentEvent.setEndTime(endTime.trim());
        currentEvent.setLocation(location.trim());

        String selectedType = (String) spinnerType.getSelectedItem();
        try { currentEvent.setType(Event.EventType.valueOf(selectedType)); } catch (Exception ignored) {}

        boolean success = plannerController.saveEvent(currentEvent);
        if (success) {
            Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
