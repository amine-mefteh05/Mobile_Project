package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.unimate.R;
import com.unimate.adapter.TaskAdapter;
import com.unimate.controller.TaskController;
import com.unimate.model.Task;
import com.unimate.view.ui.UiKit;

import java.util.List;

public class TasksFragment extends Fragment {

    private static final String[] SORT_OPTIONS = {"Deadline", "Priority", "Title"};

    private ChipGroup chipGroupCategories;
    private MaterialButtonToggleGroup toggleStatus;
    private MaterialButton btnSort;
    private RecyclerView recyclerViewTasks;
    private View emptyTasks;
    private TextView textSummary;
    private ExtendedFloatingActionButton fabAddTask;

    private TaskController taskController;
    private TaskAdapter adapter;
    // Values passed to TaskController (it expects enum-style names / "ALL").
    private String selectedCategory = "ALL";
    private String selectedStatus = "ALL";
    private String selectedSort = "Deadline";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        taskController = new TaskController(requireContext());

        initViews(view);
        setupFiltersAndSort();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasksData();
    }

    private void initViews(View view) {
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        toggleStatus = view.findViewById(R.id.toggleStatus);
        btnSort = view.findViewById(R.id.btnSortTasks);
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        emptyTasks = view.findViewById(R.id.emptyTasks);
        textSummary = view.findViewById(R.id.textTasksSummary);
        fabAddTask = view.findViewById(R.id.fabAddTask);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        UiKit.addDividers(recyclerViewTasks);
        adapter = new TaskAdapter(requireContext(), null, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(requireContext(), TaskFormActivity.class);
                intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, task.getId());
                startActivity(intent);
            }

            @Override
            public void onTaskStatusChanged(Task task, boolean isDone) {
                if (isDone) {
                    taskController.markTaskDone(task.getId());
                } else {
                    taskController.markTaskPending(task.getId());
                }
                // Post so the checkbox animation is not cut off by the rebind.
                recyclerViewTasks.post(TasksFragment.this::loadTasksData);
            }

            @Override
            public void onTaskDelete(Task task) {
                confirmDelete(task);
            }
        });
        recyclerViewTasks.setAdapter(adapter);

        recyclerViewTasks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 4 && fabAddTask.isExtended()) fabAddTask.shrink();
                else if (dy < -4 && !fabAddTask.isExtended()) fabAddTask.extend();
            }
        });
    }

    private void setupFiltersAndSort() {
        btnSort.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), btnSort);
            for (int i = 0; i < SORT_OPTIONS.length; i++) {
                menu.getMenu().add(0, i, i, "Sort by " + SORT_OPTIONS[i].toLowerCase());
            }
            menu.setOnMenuItemClickListener(item -> {
                selectedSort = SORT_OPTIONS[item.getItemId()];
                btnSort.setText(selectedSort);
                loadTasksData();
                return true;
            });
            menu.show();
        });

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedCategory = "ALL";
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null && chip.getTag() != null) selectedCategory = chip.getTag().toString();
            }
            loadTasksData();
        });

        toggleStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.segPending) selectedStatus = "PENDING";
            else if (checkedId == R.id.segDone) selectedStatus = "DONE";
            else if (checkedId == R.id.segOverdue) selectedStatus = "OVERDUE";
            else selectedStatus = "ALL";
            loadTasksData();
        });
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> startActivity(new Intent(requireContext(), TaskFormActivity.class)));
    }

    private void updateSummary() {
        List<Task> all = taskController.getFilteredAndSortedTasks("ALL", "ALL", "Deadline");
        int pending = 0, overdue = 0;
        if (all != null) {
            for (Task t : all) {
                if (t.getStatus() != Task.Status.DONE) {
                    pending++;
                    if (t.isOverdue()) overdue++;
                }
            }
        }
        String summary = pending + " to do";
        if (overdue > 0) summary += " · " + overdue + " overdue";
        textSummary.setText(summary);
    }

    private void loadTasksData() {
        if (!isAdded()) return;
        updateSummary();

        List<Task> tasks = taskController.getFilteredAndSortedTasks(selectedCategory, selectedStatus, selectedSort);

        if (tasks == null || tasks.isEmpty()) {
            boolean filtered = !"ALL".equals(selectedCategory) || !"ALL".equals(selectedStatus);
            UiKit.bindEmptyState(emptyTasks, R.drawable.ic_tasks,
                    filtered ? "No matching tasks" : "No tasks yet",
                    filtered ? "Try another filter." : "Add your first assignment, exam or project.");
            emptyTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            emptyTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
            adapter.setTasks(tasks);
        }
    }

    private void confirmDelete(Task task) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete task?")
                .setMessage("“" + task.getTitle() + "” and any study blocks planned for it will be removed.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    boolean success = taskController.deleteTask(task.getId());
                    if (success && getView() != null) {
                        Snackbar.make(getView(), "Task deleted", Snackbar.LENGTH_SHORT).show();
                        loadTasksData();
                    }
                })
                .show();
    }
}
