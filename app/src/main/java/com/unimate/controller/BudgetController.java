package com.unimate.controller;

import android.content.Context;
import android.util.Log;

import com.unimate.database.PreferencesManager;
import com.unimate.intelligence.BudgetAnalyzer;
import com.unimate.model.Expense;
import com.unimate.repository.ExpenseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BudgetController {

    private static final String TAG = "UniMate_BudgetCtrl";
    private final ExpenseRepository expenseRepo;
    private final PreferencesManager prefsManager;

    public BudgetController(Context context) {
        Context appContext = context.getApplicationContext();
        this.expenseRepo = new ExpenseRepository(appContext);
        this.prefsManager = PreferencesManager.getInstance(appContext);
    }

    public String validateExpenseInput(String amountStr, String description) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return "Please enter an expense amount.";
        }
        try {
            double amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) {
                return "Expense amount must be greater than 0.";
            }
        } catch (NumberFormatException e) {
            return "Please enter a valid numeric amount.";
        }
        if (description == null || description.trim().isEmpty()) {
            return "Please enter a brief description.";
        }
        return null;
    }

    public boolean addExpense(Expense expense) {
        if (expense == null) return false;
        try {
            if (expense.getDate() == null || expense.getDate().trim().isEmpty()) {
                expense.setDate(LocalDate.now().toString());
            }
            return expenseRepo.insert(expense) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error adding expense: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteExpense(long expenseId) {
        return expenseRepo.delete(expenseId);
    }

    public List<Expense> getCurrentWeekExpenses() {
        return expenseRepo.getCurrentWeekExpenses();
    }

    public double getWeeklyBudgetLimit() {
        return prefsManager.getWeeklyBudget();
    }

    public void setWeeklyBudgetLimit(double limit) {
        prefsManager.setWeeklyBudget(limit);
    }

    public double getCurrentWeekSpent() {
        List<Expense> expenses = getCurrentWeekExpenses();
        return BudgetAnalyzer.calculateWeeklySpent(expenses);
    }

    public double getRemainingBudget() {
        double limit = getWeeklyBudgetLimit();
        double spent = getCurrentWeekSpent();
        return BudgetAnalyzer.calculateRemaining(limit, spent);
    }

    public boolean isBudgetTight() {
        double limit = getWeeklyBudgetLimit();
        double spent = getCurrentWeekSpent();
        return BudgetAnalyzer.isBudgetTight(limit, spent);
    }

    public Map<Expense.Category, Double> getCategoryBreakdown() {
        List<Expense> expenses = getCurrentWeekExpenses();
        return BudgetAnalyzer.calculateCategoryTotals(expenses);
    }
}
