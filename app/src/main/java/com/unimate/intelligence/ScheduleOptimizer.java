package com.unimate.intelligence;

import com.unimate.dto.PlanProposal;
import com.unimate.dto.TimeSlot;
import com.unimate.model.Event;
import com.unimate.model.Task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScheduleOptimizer {

    private static final int MAX_STUDY_BLOCK_MINUTES = 90;
    private static final int BREAK_MINUTES = 15;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Checks if two events on the same date overlap in time.
     */
    public static boolean hasOverlap(Event e1, Event e2) {
        if (e1 == null || e2 == null) return false;
        if (e1.getDate() == null || !e1.getDate().equals(e2.getDate())) return false;

        try {
            LocalTime start1 = LocalTime.parse(e1.getStartTime());
            LocalTime end1 = LocalTime.parse(e1.getEndTime());
            LocalTime start2 = LocalTime.parse(e2.getStartTime());
            LocalTime end2 = LocalTime.parse(e2.getEndTime());

            return start1.isBefore(end2) && start2.isBefore(end1);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculates free slots on a given date between dayStart and dayEnd.
     */
    public static List<TimeSlot> calculateFreeSlots(List<Event> dayEvents, String date,
                                                    String dayStartStr, String dayEndStr) {
        List<TimeSlot> freeSlots = new ArrayList<>();
        try {
            LocalTime dayStart = LocalTime.parse(dayStartStr);
            LocalTime dayEnd = LocalTime.parse(dayEndStr);

            if (dayEnd.isBefore(dayStart)) return freeSlots;

            // Sort day events by start time
            List<Event> sortedEvents = new ArrayList<>();
            if (dayEvents != null) {
                for (Event e : dayEvents) {
                    if (date.equals(e.getDate())) {
                        sortedEvents.add(e);
                    }
                }
            }
            Collections.sort(sortedEvents, new Comparator<Event>() {
                @Override
                public int compare(Event o1, Event o2) {
                    return o1.getStartTime().compareTo(o2.getStartTime());
                }
            });

            LocalTime currentPointer = dayStart;

            for (Event event : sortedEvents) {
                try {
                    LocalTime eStart = LocalTime.parse(event.getStartTime());
                    LocalTime eEnd = LocalTime.parse(event.getEndTime());

                    if (eStart.isAfter(currentPointer)) {
                        LocalTime slotEnd = eStart.isBefore(dayEnd) ? eStart : dayEnd;
                        if (slotEnd.isAfter(currentPointer)) {
                            freeSlots.add(new TimeSlot(date, currentPointer.format(TIME_FORMATTER),
                                    slotEnd.format(TIME_FORMATTER)));
                        }
                    }

                    if (eEnd.isAfter(currentPointer)) {
                        currentPointer = eEnd;
                    }
                } catch (Exception ignored) {
                }
            }

            if (currentPointer.isBefore(dayEnd)) {
                freeSlots.add(new TimeSlot(date, currentPointer.format(TIME_FORMATTER),
                        dayEnd.format(TIME_FORMATTER)));
            }

        } catch (Exception e) {
            // Fallback
        }

        return freeSlots;
    }

    /**
     * Algorithm: Plan my week.
     * Proposes study sessions for pending tasks without moving existing events.
     * Caps study blocks at 90 mins and adds a 15-min break after full blocks.
     */
    public static PlanProposal planWeek(List<Task> pendingTasks, List<Event> existingEvents,
                                        String dayStart, String dayEnd) {
        List<Event> proposedEvents = new ArrayList<>();
        List<Task> unscheduledTasks = new ArrayList<>();

        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return new PlanProposal(proposedEvents, unscheduledTasks, "No pending tasks to schedule.");
        }

        // Rank tasks by priority
        List<Task> sortedTasks = TaskPrioritizer.sortTasksByPriority(pendingTasks);

        LocalDate today = LocalDate.now();
        // Determine week dates from Monday to Sunday
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            if (!d.isBefore(today)) { // Only schedule for today and future days of week
                weekDates.add(d);
            }
        }

        // Keep track of all events (existing + newly scheduled) to check conflicts
        List<Event> allEvents = new ArrayList<>();
        if (existingEvents != null) {
            allEvents.addAll(existingEvents);
        }

        for (Task task : sortedTasks) {
            if (task.getStatus() == Task.Status.DONE) continue;

            int remainingDuration = task.getDuration() > 0 ? task.getDuration() : 60;
            boolean scheduledAny = false;

            for (LocalDate date : weekDates) {
                if (remainingDuration <= 0) break;

                String dateStr = date.toString();

                // Check if task deadline is before date
                if (task.getDeadline() != null && !task.getDeadline().trim().isEmpty()) {
                    try {
                        LocalDate deadline = LocalDate.parse(task.getDeadline());
                        if (date.isAfter(deadline)) {
                            continue; // Skip dates after task deadline
                        }
                    } catch (Exception ignored) {
                    }
                }

                // Get free slots for this date
                List<TimeSlot> freeSlots = calculateFreeSlots(allEvents, dateStr, dayStart, dayEnd);

                for (TimeSlot slot : freeSlots) {
                    if (remainingDuration <= 0) break;

                    int slotDuration = slot.getDurationMinutes();
                    if (slotDuration < 30) continue; // Skip slots shorter than 30 mins

                    LocalTime slotStart = LocalTime.parse(slot.getStartTime());

                    int sessionDuration = Math.min(remainingDuration, Math.min(slotDuration, MAX_STUDY_BLOCK_MINUTES));
                    LocalTime sessionEnd = slotStart.plusMinutes(sessionDuration);

                    Event proposed = new Event();
                    proposed.setStudentId(task.getStudentId());
                    proposed.setTitle("Study: " + task.getTitle());
                    proposed.setDate(dateStr);
                    proposed.setStartTime(slotStart.format(TIME_FORMATTER));
                    proposed.setEndTime(sessionEnd.format(TIME_FORMATTER));
                    proposed.setType(Event.EventType.STUDY);
                    proposed.setTaskId(task.getId());
                    proposed.setLocation("Study Area");

                    proposedEvents.add(proposed);
                    allEvents.add(proposed);

                    remainingDuration -= sessionDuration;
                    scheduledAny = true;

                    // Apply 15 minute break if full block was used
                    if (sessionDuration >= MAX_STUDY_BLOCK_MINUTES) {
                        LocalTime breakEnd = sessionEnd.plusMinutes(BREAK_MINUTES);
                        if (breakEnd.isBefore(LocalTime.parse(dayEnd))) {
                            Event breakEvent = new Event();
                            breakEvent.setStudentId(task.getStudentId());
                            breakEvent.setTitle("Break");
                            breakEvent.setDate(dateStr);
                            breakEvent.setStartTime(sessionEnd.format(TIME_FORMATTER));
                            breakEvent.setEndTime(breakEnd.format(TIME_FORMATTER));
                            breakEvent.setType(Event.EventType.PERSONAL);
                            breakEvent.setLocation("Rest");

                            allEvents.add(breakEvent);
                        }
                    }
                }
            }

            if (remainingDuration > 0) {
                unscheduledTasks.add(task);
            }
        }

        String explanation = String.format("Proposed %d study sessions. %d tasks could not be fully scheduled.",
                proposedEvents.size(), unscheduledTasks.size());

        return new PlanProposal(proposedEvents, unscheduledTasks, explanation);
    }
}
