package com.unimate.controller;

import android.content.Context;
import android.util.Log;

import com.unimate.dto.PlanProposal;
import com.unimate.intelligence.RecommendationEngine;

import com.unimate.database.PreferencesManager;
import com.unimate.model.Event;
import com.unimate.model.Task;

import com.unimate.repository.EventRepository;
import com.unimate.repository.TaskRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlannerController {

    private static final String TAG = "UniMate_PlannerCtrl";
    private final EventRepository eventRepo;
    private final TaskRepository taskRepo;
    private final PreferencesManager prefsManager;

    public PlannerController(Context context) {
        Context appContext = context.getApplicationContext();
        this.eventRepo = new EventRepository(appContext);
        this.taskRepo = new TaskRepository(appContext);
        this.prefsManager = PreferencesManager.getInstance(appContext);
    }

    public String validateEventInput(String title, String startTimeStr, String endTimeStr) {
        if (title == null || title.trim().isEmpty()) {
            return "Event title cannot be empty.";
        }
        try {
            LocalTime start = LocalTime.parse(startTimeStr);
            LocalTime end = LocalTime.parse(endTimeStr);
            if (!end.isAfter(start)) {
                return "End time must be after start time.";
            }
        } catch (Exception e) {
            return "Please enter valid start and end times (HH:mm).";
        }
        return null;
    }

    public boolean saveEvent(Event event) {
        if (event == null) return false;
        try {
            if (event.getId() <= 0) {
                return eventRepo.insert(event) > 0;
            } else {
                return eventRepo.update(event);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving event: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteEvent(long eventId) {
        return eventRepo.delete(eventId);
    }

    public List<Event> getEventsForDate(String date) {
        return eventRepo.getEventsByDate(date);
    }

    public List<Event> getTodayAgenda() {
        String today = LocalDate.now().toString();
        return eventRepo.getEventsByDate(today);
    }

    public List<Event> getAllEvents() {
        return eventRepo.getAllEvents();
    }

    /** Looks up a single event (used when editing). Returns null if it no longer exists. */
    public Event getEventById(long id) {
        List<Event> all = eventRepo.getAllEvents();
        if (all == null) return null;
        for (Event event : all) {
            if (event.getId() == id) return event;
        }
        return null;
    }

    /**
     * Proposes a weekly study plan. Does NOT save anything until approved.
     */
    public PlanProposal proposeWeeklyPlan() {
        List<Task> pendingTasks = taskRepo.getPendingTasks();
        List<Event> existingEvents = eventRepo.getAllEvents();
        String dayStart = prefsManager.getDayStart();
        String dayEnd = prefsManager.getDayEnd();

        return RecommendationEngine.planWeek(pendingTasks, existingEvents, dayStart, dayEnd);
    }

    /**
     * Saves approved proposed study sessions.
     */
    public int approveAndSavePlan(List<Event> approvedEvents) {
        if (approvedEvents == null || approvedEvents.isEmpty()) return 0;
        int savedCount = 0;
        for (Event event : approvedEvents) {
            try {
                long id = eventRepo.insert(event);
                if (id > 0) savedCount++;
            } catch (Exception e) {
                Log.e(TAG, "Error saving approved study session: " + e.getMessage(), e);
            }
        }
        Log.i(TAG, "Approved and saved " + savedCount + " study sessions.");
        return savedCount;
    }

    public List<String> getWeekDates(LocalDate referenceDate) {
        List<String> dates = new ArrayList<>();
        LocalDate monday = referenceDate.with(DayOfWeek.MONDAY);
        for (int i = 0; i < 7; i++) {
            dates.add(monday.plusDays(i).toString());
        }
        return dates;
    }
}
