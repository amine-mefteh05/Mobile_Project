package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unimate.R;
import com.unimate.adapter.TaskAdapter;
import com.unimate.controller.TaskController;
import com.unimate.model.Task;

import java.util.List;

public class TasksFragment extends Fragment {

    private ChipGroup chipGroupCategories, chipGroupStatus;
    private Spinner spinnerSort;
    private RecyclerView recyclerViewTasks;
    private TextView textEmptyTasks;
    private FloatingActionButton fabAddTask;

    private TaskController taskController;
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
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);
        spinnerSort = view.findViewById(R.id.spinnerSortTasks);
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        textEmptyTasks = view.findViewById(R.id.textEmptyTasks);
        fabAddTask = view.findViewById(R.id.fabAddTask);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupFiltersAndSort() {
        String[] sortOptions = {"Deadline", "Priority", "Title"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sortOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = sortOptions[position];
                loadTasksData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = "ALL";
            } else {
                int id = checkedIds.get(0);
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                }
            }
            loadTasksData();
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedStatus = "ALL";
            } else {
                int id = checkedIds.get(0);
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    selectedStatus = chip.getText().toString();
                }
            }
            loadTasksData();
        });
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TaskFormActivity.class);
            startActivity(intent);
        });
    }

    private void loadTasksData() {
        List<Task> tasks = taskController.getFilteredAndSortedTasks(selectedCategory, selectedStatus, selectedSort);

        if (tasks == null || tasks.isEmpty()) {
            textEmptyTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textEmptyTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);

            TaskAdapter adapter = new TaskAdapter(requireContext(), tasks, new TaskAdapter.OnTaskActionListener() {
                @Override
                public void onTaskClick(Task task) {
                    Intent intent = new Intent(requireContext(), TaskFormActivity.class);
                    intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, task.getId());
                    startActivity(intent);
                }

                @Override
                public void onTaskStatusChanged(Task task, boolean isDone) {
                    taskController.markTaskDone(task.getId());
                    loadTasksData();
                }

                @Override
                public void onTaskDelete(Task task) {
                    boolean success = taskController.deleteTask(task.getId());
                    if (success) {
                        Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                        loadTasksData();
                    }
                }
            });
            recyclerViewTasks.setAdapter(adapter);
        }
    }
}
