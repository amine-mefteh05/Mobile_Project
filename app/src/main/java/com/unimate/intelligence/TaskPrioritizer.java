package com.unimate.intelligence;

import com.unimate.model.Task;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskPrioritizer {

    public static double calculateScore(Task task) {
        if (task == null) return 0.0;

        double score = 0.0;

        // 1. Priority score
        if (task.getPriority() != null) {
            switch (task.getPriority()) {
                case HIGH:
                    score += 30.0;
                    break;
                case MEDIUM:
                    score += 20.0;
                    break;
                case LOW:
                    score += 10.0;
                    break;
            }
        }

        // 2. Category score
        if (task.getCategory() != null) {
            switch (task.getCategory()) {
                case EXAM:
                    score += 25.0;
                    break;
                case PROJECT:
                    score += 20.0;
                    break;
                case UNIVERSITY:
                    score += 15.0;
                    break;
                case PERSONAL:
                    score += 5.0;
                    break;
            }
        }

        // 3. Overdue boost
        if (task.isOverdue()) {
            score += 50.0;
        }

        // 4. Deadline urgency
        if (task.getDeadline() != null && !task.getDeadline().trim().isEmpty()) {
            try {
                LocalDate today = LocalDate.now();
                LocalDate deadline = LocalDate.parse(task.getDeadline());
                long daysUntil = ChronoUnit.DAYS.between(today, deadline);

                if (daysUntil <= 0) {
                    score += 40.0; // Due today or overdue
                } else if (daysUntil <= 1) {
                    score += 35.0;
                } else if (daysUntil <= 3) {
                    score += 25.0;
                } else if (daysUntil <= 7) {
                    score += 15.0;
                } else {
                    score += 5.0;
                }
            } catch (Exception ignored) {
            }
        }

        // 5. Manageable duration boost
        if (task.getDuration() > 0 && task.getDuration() <= 60) {
            score += 5.0;
        }

        return score;
    }

    public static List<Task> sortTasksByPriority(List<Task> tasks) {
        if (tasks == null) return new ArrayList<>();

        List<Task> sorted = new ArrayList<>(tasks);
        Collections.sort(sorted, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                double s1 = calculateScore(t1);
                double s2 = calculateScore(t2);
                return Double.compare(s2, s1); // Descending
            }
        });
        return sorted;
    }
}
