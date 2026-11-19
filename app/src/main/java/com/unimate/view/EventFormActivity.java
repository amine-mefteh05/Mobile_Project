package com.unimate.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unimate.R;
import com.unimate.controller.PlannerController;
import com.unimate.model.Event;
import com.unimate.view.ui.UiKit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

public class EventFormActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_EVENT_DATE = "extra_event_date";

    private TextInputEditText editTitle, editDate, editStartTime, editEndTime, editLocation;
    private TextInputLayout layoutTitle, layoutStart, layoutEnd;
    private ChipGroup chipGroupType;
    private View btnSaveEvent;

    private PlannerController plannerController;
    private long eventId = -1;
    private Event currentEvent;
    /** ISO yyyy-MM-dd; the field shows a friendly date. */
    private String dateIso = LocalDate.now().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        plannerController = new PlannerController(this);
        eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);

        UiKit.setupSecondaryScreen(this, R.id.toolbarEventForm,
                eventId > 0 ? "Edit event" : "New event", R.drawable.ic_close, "Close");
        UiKit.padForSystemBars(findViewById(R.id.bottomBarEventForm), false, true);

        initViews();

        if (eventId > 0) {
            // Load the stored event so saving updates it instead of creating a copy.
            currentEvent = plannerController.getEventById(eventId);
            if (currentEvent == null) {
                Toast.makeText(this, "This event no longer exists", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            bindEvent(currentEvent);
        } else {
            currentEvent = new Event();
            String prefilledDate = getIntent().getStringExtra(EXTRA_EVENT_DATE);
            setDate(prefilledDate != null && !prefilledDate.isEmpty() ? prefilledDate : LocalDate.now().toString());
            // Sensible default slot: next full hour, one hour long.
            LocalTime start = LocalTime.now().plusHours(1).withMinute(0);
            if (start.getHour() == 0) start = LocalTime.of(9, 0);
            editStartTime.setText(fmt(start));
            editEndTime.setText(fmt(start.getHour() >= 23 ? LocalTime.of(23, 59) : start.plusHours(1)));
        }

        setupListeners();
    }

    private static String fmt(LocalTime t) {
        return String.format(Locale.US, "%02d:%02d", t.getHour(), t.getMinute());
    }

    private void initViews() {
        editTitle = findViewById(R.id.editEventTitle);
        editDate = findViewById(R.id.editEventDate);
        editStartTime = findViewById(R.id.editEventStartTime);
        editEndTime = findViewById(R.id.editEventEndTime);
        editLocation = findViewById(R.id.editEventLocation);
        layoutTitle = findViewById(R.id.layoutEventTitle);
        layoutStart = findViewById(R.id.layoutEventStart);
        layoutEnd = findViewById(R.id.layoutEventEnd);
        chipGroupType = findViewById(R.id.chipGroupEventType);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
    }

    private void bindEvent(Event event) {
        editTitle.setText(event.getTitle());
        setDate(event.getDate() != null ? event.getDate() : LocalDate.now().toString());
        editStartTime.setText(event.getStartTime());
        editEndTime.setText(event.getEndTime());
        editLocation.setText(event.getLocation());
        if (event.getType() != null) {
            for (int i = 0; i < chipGroupType.getChildCount(); i++) {
                View child = chipGroupType.getChildAt(i);
                if (child instanceof Chip && event.getType().name().equals(child.getTag())) {
                    chipGroupType.check(child.getId());
                    break;
                }
            }
        }
    }

    private void setDate(String iso) {
        dateIso = iso;
        editDate.setText(UiKit.prettyDate(iso));
    }

    private void setupListeners() {
        editDate.setOnClickListener(v -> UiKit.pickDate(this, "Date", dateIso, this::setDate));
        editStartTime.setOnClickListener(v -> UiKit.pickTime(this, "Starts", textOf(editStartTime), t -> {
            editStartTime.setText(t);
            layoutStart.setError(null);
            layoutEnd.setError(null);
        }));
        editEndTime.setOnClickListener(v -> UiKit.pickTime(this, "Ends", textOf(editEndTime), t -> {
            editEndTime.setText(t);
            layoutStart.setError(null);
            layoutEnd.setError(null);
        }));
        UiKit.clearErrorOnEdit(layoutTitle);
        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private static String textOf(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString() : "";
    }

    private void saveEvent() {
        String title = textOf(editTitle);
        String startTime = textOf(editStartTime);
        String endTime = textOf(editEndTime);
        String location = textOf(editLocation);

        layoutTitle.setError(null);
        layoutStart.setError(null);
        layoutEnd.setError(null);

        String error = plannerController.validateEventInput(title, startTime, endTime);
        if (error != null) {
            if (error.toLowerCase(Locale.ROOT).contains("title")) {
                layoutTitle.setError(error);
                editTitle.requestFocus();
            } else {
                layoutEnd.setError(error);
            }
            return;
        }

        if (currentEvent == null) currentEvent = new Event();
        currentEvent.setTitle(title.trim());
        currentEvent.setDate(dateIso.trim());
        currentEvent.setStartTime(startTime.trim());
        currentEvent.setEndTime(endTime.trim());
        currentEvent.setLocation(location.trim());

        Chip checked = chipGroupType.findViewById(chipGroupType.getCheckedChipId());
        if (checked != null && checked.getTag() != null) {
            try { currentEvent.setType(Event.EventType.valueOf(checked.getTag().toString())); } catch (Exception ignored) {}
        }

        boolean success = plannerController.saveEvent(currentEvent);
        if (success) {
            Toast.makeText(this, eventId > 0 ? "Event updated" : "Event added", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Couldn't save the event. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
