package com.unimate.intelligence;

import com.unimate.dto.PlanProposal;
import com.unimate.dto.Suggestion;
import com.unimate.dto.WeeklySummary;
import com.unimate.model.Event;
import com.unimate.model.Expense;
import com.unimate.model.StudyPlace;
import com.unimate.model.Task;

import java.time.LocalTime;
import java.util.List;

public class RecommendationEngine {

    /**
     * Algorithm: What should I do now?
     * Analyzes day window, current event, free time slot, and pending tasks.
     * Returns a clear Suggestion with explanation.
     */
    public static Suggestion whatNow(List<Task> pendingTasks, List<Event> todayEvents,
                                     String currentTimeStr, String dayStartStr, String dayEndStr,
                                     StudyPlace recommendedPlace) {
        try {
            LocalTime now = (currentTimeStr != null && !currentTimeStr.isEmpty()) ?
                    LocalTime.parse(currentTimeStr) : LocalTime.now();
            LocalTime dayStart = LocalTime.parse(dayStartStr != null ? dayStartStr : "08:00");
            LocalTime dayEnd = LocalTime.parse(dayEndStr != null ? dayEndStr : "20:00");

            // Check 1: Outside study day window
            if (now.isBefore(dayStart) || !now.isBefore(dayEnd)) {
                return new Suggestion(
                        "Rest and Recharge",
                        "You are outside your configured daily study window (" + dayStartStr + " - " + dayEndStr + ").",
                        "Taking time to relax helps prevent burnout and keeps your focus sharp for tomorrow.",
                        0,
                        null,
                        null
                );
            }

            // Check 2: Active event right now
            if (todayEvents != null) {
                for (Event event : todayEvents) {
                    try {
                        LocalTime eStart = LocalTime.parse(event.getStartTime());
                        LocalTime eEnd = LocalTime.parse(event.getEndTime());
                        if ((now.equals(eStart) || now.isAfter(eStart)) && now.isBefore(eEnd)) {
                            return new Suggestion(
                                    "Attend: " + event.getTitle(),
                                    "You have an ongoing " + event.getType().name().toLowerCase() +
                                            " at " + (event.getLocation() != null ? event.getLocation() : "Campus") + ".",
                                    "Current event in progress until " + event.getEndTime() + ".",
                                    0,
                                    null,
                                    null
                            );
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            // Check 3: Calculate remaining free slot from 'now' to next event or dayEnd
            LocalTime nextEventTime = dayEnd;
            if (todayEvents != null) {
                for (Event event : todayEvents) {
                    try {
                        LocalTime eStart = LocalTime.parse(event.getStartTime());
                        if (eStart.isAfter(now) && eStart.isBefore(nextEventTime)) {
                            nextEventTime = eStart;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            int freeSlotMinutes = (nextEventTime.getHour() * 60 + nextEventTime.getMinute()) -
                    (now.getHour() * 60 + now.getMinute());
            freeSlotMinutes = Math.max(0, freeSlotMinutes);

            // Check 4: Pending tasks available
            List<Task> sortedTasks = TaskPrioritizer.sortTasksByPriority(pendingTasks);
            Task bestTask = null;

            if (sortedTasks != null && !sortedTasks.isEmpty()) {
                for (Task t : sortedTasks) {
                    if (t.getStatus() == Task.Status.PENDING) {
                        bestTask = t; // Top priority task
                        break;
                    }
                }
            }

            if (bestTask != null) {
                String reason = String.format("Task '%s' is your highest priority pending work (%s priority, due %s). You have %d mins of free time.",
                        bestTask.getTitle(),
                        bestTask.getPriority().name(),
                        bestTask.getDeadline() != null ? bestTask.getDeadline() : "soon",
                        freeSlotMinutes);

                StudyPlace placeForSuggestion = (freeSlotMinutes >= 60) ? recommendedPlace : null;

                return new Suggestion(
                        "Work on: " + bestTask.getTitle(),
                        "Estimated duration: " + bestTask.getDuration() + " mins.",
                        reason,
                        freeSlotMinutes,
                        bestTask,
                        placeForSuggestion
                );
            }

            // Check 5: No pending tasks
            return new Suggestion(
                    "All Caught Up!",
                    "You have no pending tasks on your to-do list right now.",
                    "Enjoy your free time or use this slot to prepare for upcoming courses.",
                    freeSlotMinutes,
                    null,
                    recommendedPlace
            );

        } catch (Exception e) {
            return new Suggestion(
                    "Plan Your Session",
                    "Organize your study goals for the day.",
                    "Keep track of deadlines and schedule breaks.",
                    30,
                    null,
                    null
            );
        }
    }

    public static PlanProposal planWeek(List<Task> pendingTasks, List<Event> existingEvents,
                                        String dayStart, String dayEnd) {
        return ScheduleOptimizer.planWeek(pendingTasks, existingEvents, dayStart, dayEnd);
    }

    public static WeeklySummary weeklySummary(List<Task> tasks, List<Event> events, List<Expense> expenses) {
        return ProductivityAnalyzer.generateWeeklySummary(tasks, events, expenses);
    }
}
