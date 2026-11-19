package com.unimate.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.unimate.R;
import com.unimate.model.Task;

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
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardView;
        CheckBox checkBoxDone;
        TextView textTitle;
        TextView textDescription;
        TextView textDeadline;
        TextView textDuration;
        Chip chipCategory;
        Chip chipPriority;
        TextView textOverdueBadge;
        ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTask);
            checkBoxDone = itemView.findViewById(R.id.checkBoxTaskDone);
            textTitle = itemView.findViewById(R.id.textTaskTitle);
            textDescription = itemView.findViewById(R.id.textTaskDescription);
            textDeadline = itemView.findViewById(R.id.textTaskDeadline);
            textDuration = itemView.findViewById(R.id.textTaskDuration);
            chipCategory = itemView.findViewById(R.id.chipTaskCategory);
            chipPriority = itemView.findViewById(R.id.chipTaskPriority);
            textOverdueBadge = itemView.findViewById(R.id.textTaskOverdue);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
        }

        public void bind(Task task) {
            textTitle.setText(task.getTitle());
            
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                textDescription.setVisibility(View.VISIBLE);
                textDescription.setText(task.getDescription());
            } else {
                textDescription.setVisibility(View.GONE);
            }

            textDeadline.setText("Due: " + (task.getDeadline() != null ? task.getDeadline() : "None"));
            textDuration.setText(task.getDuration() + " mins");

            chipCategory.setText(task.getCategory() != null ? task.getCategory().name() : "GENERAL");
            chipPriority.setText(task.getPriority() != null ? task.getPriority().name() : "MEDIUM");

            // Priority styling
            if (task.getPriority() == Task.Priority.HIGH) {
                chipPriority.setChipBackgroundColorResource(R.color.priority_high);
            } else if (task.getPriority() == Task.Priority.LOW) {
                chipPriority.setChipBackgroundColorResource(R.color.priority_low);
            } else {
                chipPriority.setChipBackgroundColorResource(R.color.priority_medium);
            }

            // Checkbox status
            checkBoxDone.setOnCheckedChangeListener(null);
            checkBoxDone.setChecked(task.getStatus() == Task.Status.DONE);

            if (task.getStatus() == Task.Status.DONE) {
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                cardView.setAlpha(0.7f);
                textOverdueBadge.setVisibility(View.GONE);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                cardView.setAlpha(1.0f);

                // Overdue highlight
                if (task.isOverdue()) {
                    textOverdueBadge.setVisibility(View.VISIBLE);
                    cardView.setStrokeColor(ContextCompat.getColor(context, R.color.danger_red));
                    cardView.setStrokeWidth(4);
                } else {
                    textOverdueBadge.setVisibility(View.GONE);
                    cardView.setStrokeColor(Color.TRANSPARENT);
                    cardView.setStrokeWidth(0);
                }
            }

            checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskStatusChanged(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskDelete(task);
                }
            });
        }
    }
}
