package com.unimate.intelligence;

import com.unimate.dto.WeeklySummary;
import com.unimate.model.Event;
import com.unimate.model.Expense;
import com.unimate.model.Task;

import java.util.List;

public class ProductivityAnalyzer {

    public static WeeklySummary generateWeeklySummary(List<Task> tasks, List<Event> events, List<Expense> expenses) {
        int totalTasks = tasks != null ? tasks.size() : 0;
        int completedTasks = 0;

        if (tasks != null) {
            for (Task t : tasks) {
                if (t.getStatus() == Task.Status.DONE) {
                    completedTasks++;
                }
            }
        }

        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;

        double totalStudyMinutes = 0.0;
        if (events != null) {
            for (Event e : events) {
                if (e.getType() == Event.EventType.STUDY) {
                    totalStudyMinutes += e.getDurationMinutes();
                }
            }
        }
        double totalStudyHours = totalStudyMinutes / 60.0;

        double totalSpent = BudgetAnalyzer.calculateWeeklySpent(expenses);
        String topCategory = BudgetAnalyzer.getTopSpendingCategory(expenses);

        return new WeeklySummary(totalTasks, completedTasks, completionRate, totalStudyHours, totalSpent, topCategory);
    }
}
