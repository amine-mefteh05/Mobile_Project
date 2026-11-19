package com.unimate.controller;

import android.content.Context;
import android.util.Log;

import com.unimate.intelligence.TaskPrioritizer;
import com.unimate.model.Task;

import com.unimate.receiver.ReminderScheduler;
import com.unimate.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskController {

    private static final String TAG = "UniMate_TaskCtrl";
    private final TaskRepository taskRepo;
    private final Context context;

    public TaskController(Context context) {
        this.context = context.getApplicationContext();
        this.taskRepo = new TaskRepository(this.context);
    }

    public String validateTaskInput(String title, String deadline, String durationStr) {
        if (title == null || title.trim().isEmpty()) {
            return "Task title cannot be empty.";
        }
        if (deadline == null || deadline.trim().isEmpty()) {
            return "Please select a valid deadline date.";
        }
        try {
            int duration = Integer.parseInt(durationStr.trim());
            if (duration <= 0) {
                return "Duration must be greater than 0 minutes.";
            }
        } catch (NumberFormatException e) {
            return "Please enter a valid duration in minutes.";
        }
        return null; // Valid
    }

    public boolean saveTask(Task task) {
        if (task == null) return false;
        try {
            boolean success;
            if (task.getId() <= 0) {
                long id = taskRepo.insert(task);
                success = id > 0;
            } else {
                success = taskRepo.update(task);
            }

            if (success && task.isHasReminder() && task.getReminderTime() != null) {
                ReminderScheduler.scheduleReminder(context, task);
            } else if (success && (!task.isHasReminder() || task.getStatus() == Task.Status.DONE)) {
                ReminderScheduler.cancelReminder(context, task.getId());
            }

            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error saving task: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean markTaskDone(long taskId) {
        try {
            boolean success = taskRepo.markStatus(taskId, Task.Status.DONE);
            if (success) {
                ReminderScheduler.cancelReminder(context, taskId);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error marking task done: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteTask(long taskId) {
        try {
            ReminderScheduler.cancelReminder(context, taskId);
            return taskRepo.delete(taskId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting task: " + e.getMessage(), e);
            return false;
        }
    }

    public Task getTaskById(long id) {
        return taskRepo.getById(id);
    }

    public List<Task> getFilteredAndSortedTasks(String categoryFilter, String statusFilter, String sortBy) {
        List<Task> all = taskRepo.getAllTasks();
        List<Task> filtered = new ArrayList<>();

        for (Task t : all) {
            boolean matchesCat = (categoryFilter == null || "ALL".equalsIgnoreCase(categoryFilter)
                    || (t.getCategory() != null && t.getCategory().name().equalsIgnoreCase(categoryFilter)));

            boolean matchesStatus = true;
            if ("PENDING".equalsIgnoreCase(statusFilter)) {
                matchesStatus = (t.getStatus() == Task.Status.PENDING);
            } else if ("DONE".equalsIgnoreCase(statusFilter)) {
                matchesStatus = (t.getStatus() == Task.Status.DONE);
            } else if ("OVERDUE".equalsIgnoreCase(statusFilter)) {
                matchesStatus = t.isOverdue();
            }

            if (matchesCat && matchesStatus) {
                filtered.add(t);
            }
        }

        if ("Priority".equalsIgnoreCase(sortBy)) {
            return TaskPrioritizer.sortTasksByPriority(filtered);
        } else if ("Title".equalsIgnoreCase(sortBy)) {
            Collections.sort(filtered, new Comparator<Task>() {
                @Override
                public int compare(Task o1, Task o2) {
                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                }
            });
        }
        // Default: sorted by deadline ASC (handled by DB query)
        return filtered;
    }

    public List<Task> getPendingTasks() {
        return taskRepo.getPendingTasks();
    }

    /**
     * Facade method for Home screen: returns progress string (e.g. "3/5 Completed").
     */
    public String getTodayProgress() {
        try {
            List<Task> all = taskRepo.getAllTasks();
            int total = all.size();
            int done = 0;
            for (Task t : all) {
                if (t.getStatus() == Task.Status.DONE) done++;
            }
            return String.format("%d/%d completed", done, total);
        } catch (Exception e) {
            return "0/0 completed";
        }
    }
}
