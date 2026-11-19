package com.unimate.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unimate.R;
import com.unimate.adapter.ExpenseAdapter;
import com.unimate.controller.BudgetController;
import com.unimate.model.Expense;

import java.util.List;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {

    private TextView textLimit, textSpent, textRemaining;
    private ProgressBar progressBudget;
    private MaterialCardView cardWarning;
    private RecyclerView recyclerViewExpenses;
    private TextView textEmptyExpenses;
    private FloatingActionButton fabAddExpense;

    private BudgetController budgetController;
    private ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        budgetController = new BudgetController(this);

        Toolbar toolbar = findViewById(R.id.toolbarBudget);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Weekly Budget");
        }

        initViews();
        setupRecyclerView();

        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        loadBudgetData();
    }

    private void initViews() {
        textLimit = findViewById(R.id.textBudgetLimit);
        textSpent = findViewById(R.id.textBudgetSpent);
        textRemaining = findViewById(R.id.textBudgetRemaining);
        progressBudget = findViewById(R.id.progressBudget);
        cardWarning = findViewById(R.id.cardBudgetWarning);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        textEmptyExpenses = findViewById(R.id.textEmptyExpenses);
        fabAddExpense = findViewById(R.id.fabAddExpense);
    }

    private void setupRecyclerView() {
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, null, expense -> {
            boolean success = budgetController.deleteExpense(expense.getId());
            if (success) {
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                loadBudgetData();
            }
        });
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void loadBudgetData() {
        double limit = budgetController.getWeeklyBudgetLimit();
        double spent = budgetController.getCurrentWeekSpent();
        double remaining = budgetController.getRemainingBudget();
        boolean isTight = budgetController.isBudgetTight();

        textLimit.setText(String.format(Locale.getDefault(), "Limit: %.2f TND", limit));
        textSpent.setText(String.format(Locale.getDefault(), "Spent: %.2f TND", spent));
        textRemaining.setText(String.format(Locale.getDefault(), "Remaining: %.2f TND", remaining));

        int progress = limit > 0 ? (int) Math.min(100, (spent * 100 / limit)) : 0;
        progressBudget.setProgress(progress);

        // Warning banner when spent reaches 80%
        cardWarning.setVisibility(isTight ? View.VISIBLE : View.GONE);

        List<Expense> expenses = budgetController.getCurrentWeekExpenses();
        if (expenses == null || expenses.isEmpty()) {
            textEmptyExpenses.setVisibility(View.VISIBLE);
            recyclerViewExpenses.setVisibility(View.GONE);
        } else {
            textEmptyExpenses.setVisibility(View.GONE);
            recyclerViewExpenses.setVisibility(View.VISIBLE);
            expenseAdapter.setExpenses(expenses);
        }
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        EditText editAmount = dialogView.findViewById(R.id.editExpenseAmount);
        EditText editDesc = dialogView.findViewById(R.id.editExpenseDescription);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerExpenseCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSaveExpense);

        String[] categories = {"FOOD", "TRANSPORT", "STUDY", "OTHER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString();
            String desc = editDesc.getText().toString();

            String error = budgetController.validateExpenseInput(amountStr, desc);
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }

            Expense expense = new Expense();
            expense.setAmount(Double.parseDouble(amountStr.trim()));
            expense.setDescription(desc.trim());
            String catStr = (String) spinnerCat.getSelectedItem();
            try { expense.setCategory(Expense.Category.valueOf(catStr)); } catch (Exception ignored) {}

            boolean success = budgetController.addExpense(expense);
            if (success) {
                Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadBudgetData();
            } else {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
