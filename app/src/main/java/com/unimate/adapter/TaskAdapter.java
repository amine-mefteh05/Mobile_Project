package com.unimate.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unimate.R;
import com.unimate.model.Task;
import com.unimate.view.ui.UiKit;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onTaskClick(Task task);
        void onTaskStatusChanged(Task task, boolean isDone);
        void onTaskDelete(Task task);
    }

    private final Context context;
    private List<Task> tasks;
    private final OnTaskActionListener listener;

    public TaskAdapter(Context context, List<Task> tasks, OnTaskActionListener listener) {
        this.context = context;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        final View cardView;
        final CheckBox checkBoxDone;
        final TextView textTitle, textDescription, textDeadline, textDuration;
        final TextView textPriority, textCategory, textOverdueBadge;
        final ImageView iconDue;
        final ImageButton btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTask);
            checkBoxDone = itemView.findViewById(R.id.checkBoxTaskDone);
            textTitle = itemView.findViewById(R.id.textTaskTitle);
            textDescription = itemView.findViewById(R.id.textTaskDescription);
            textDeadline = itemView.findViewById(R.id.textTaskDeadline);
            textDuration = itemView.findViewById(R.id.textTaskDuration);
            textPriority = itemView.findViewById(R.id.textTaskPriority);
            textCategory = itemView.findViewById(R.id.textTaskCategory);
            textOverdueBadge = itemView.findViewById(R.id.textTaskOverdue);
            iconDue = itemView.findViewById(R.id.iconTaskDue);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
        }

        void bind(Task task) {
            boolean isDone = task.getStatus() == Task.Status.DONE;
            boolean isOverdue = !isDone && task.isOverdue();

            textTitle.setText(task.getTitle());

            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                textDescription.setVisibility(View.VISIBLE);
                textDescription.setText(task.getDescription());
            } else {
                textDescription.setVisibility(View.GONE);
            }

            textDeadline.setText(isDone ? "Completed" : UiKit.relativeDue(task.getDeadline()));
            textDuration.setText(task.getDuration() + " min");

            Task.Priority priority = task.getPriority() != null ? task.getPriority() : Task.Priority.MEDIUM;
            // Priority reads as a coloured dot + word; category stays plain text. No pills.
            int priorityHue = UiKit.priorityColor(priority);
            textPriority.setText(UiKit.label(priority));
            textPriority.setTextColor(UiKit.color(context, priorityHue));
            UiKit.setLeadingIcon(textPriority, R.drawable.ic_dot, priorityHue, 8);

            textCategory.setText(task.getCategory() != null ? UiKit.label(task.getCategory()) : "General");

            // Done: quiet the row. Overdue: red due-date + badge, no heavy border.
            if (isDone) {
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setTextColor(UiKit.color(context, R.color.text_tertiary));
                cardView.setAlpha(0.75f);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textTitle.setTextColor(UiKit.color(context, R.color.text_primary));
                cardView.setAlpha(1.0f);
            }
            textOverdueBadge.setVisibility(isOverdue ? View.VISIBLE : View.GONE);
            int dueColor = isOverdue ? R.color.danger : R.color.text_secondary;
            textDeadline.setTextColor(UiKit.color(context, dueColor));
            iconDue.setImageTintList(UiKit.colorList(context, isOverdue ? R.color.danger : R.color.text_tertiary));

            checkBoxDone.setOnCheckedChangeListener(null);
            checkBoxDone.setChecked(isDone);
            checkBoxDone.setContentDescription((isDone ? "Mark as not done: " : "Mark as done: ") + task.getTitle());
            checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onTaskStatusChanged(task, isChecked);
            });

            btnDelete.setContentDescription("Delete " + task.getTitle());
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(task);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onTaskDelete(task);
            });
        }
    }
}
