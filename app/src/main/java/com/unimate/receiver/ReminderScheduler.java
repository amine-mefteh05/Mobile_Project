package com.unimate.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.unimate.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ReminderScheduler {

    private static final String TAG = "UniMate_ReminderSch";

    public static void scheduleReminder(Context context, Task task) {
        if (task == null || !task.isHasReminder() || task.getDeadline() == null || task.getReminderTime() == null) {
            return;
        }

        try {
            LocalDate deadlineDate = LocalDate.parse(task.getDeadline());
            LocalTime reminderTime = LocalTime.parse(task.getReminderTime());
            LocalDateTime reminderDateTime = LocalDateTime.of(deadlineDate, reminderTime);

            long triggerMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if (triggerMillis <= System.currentTimeMillis()) {
                Log.i(TAG, "Reminder time is in the past, skipping alarm for task: " + task.getId());
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra(ReminderReceiver.EXTRA_TASK_ID, task.getId());
            intent.putExtra(ReminderReceiver.EXTRA_TASK_TITLE, task.getTitle());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) task.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            Log.i(TAG, "Scheduled reminder for task " + task.getId() + " at " + reminderDateTime);

        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule reminder: " + e.getMessage(), e);
        }
    }

    public static void cancelReminder(Context context, long taskId) {
        if (taskId <= 0) return;
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) taskId,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.i(TAG, "Cancelled reminder for task " + taskId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel reminder: " + e.getMessage(), e);
        }
    }
}
