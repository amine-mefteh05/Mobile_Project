package com.unimate.intelligence;

import com.unimate.model.Expense;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BudgetAnalyzer {

    public static double calculateWeeklySpent(List<Expense> expenses) {
        if (expenses == null) return 0.0;
        double total = 0.0;
        for (Expense e : expenses) {
            if (e != null && e.getAmount() > 0) {
                total += e.getAmount();
            }
        }
        return total;
    }

    public static double calculateRemaining(double weeklyBudget, double spent) {
        return Math.max(0.0, weeklyBudget - spent);
    }

    public static boolean isBudgetTight(double weeklyBudget, double spent) {
        if (weeklyBudget <= 0) return false;
        return spent >= (0.8 * weeklyBudget); // 80% threshold
    }

    public static Map<Expense.Category, Double> calculateCategoryTotals(List<Expense> expenses) {
        Map<Expense.Category, Double> totals = new EnumMap<>(Expense.Category.class);
        for (Expense.Category cat : Expense.Category.values()) {
            totals.put(cat, 0.0);
        }

        if (expenses == null) return totals;

        for (Expense e : expenses) {
            if (e != null && e.getCategory() != null) {
                double current = totals.getOrDefault(e.getCategory(), 0.0);
                totals.put(e.getCategory(), current + e.getAmount());
            }
        }
        return totals;
    }

    public static String getTopSpendingCategory(List<Expense> expenses) {
        Map<Expense.Category, Double> totals = calculateCategoryTotals(expenses);
        Expense.Category topCat = null;
        double maxSpent = 0.0;

        for (Map.Entry<Expense.Category, Double> entry : totals.entrySet()) {
            if (entry.getValue() > maxSpent) {
                maxSpent = entry.getValue();
                topCat = entry.getKey();
            }
        }
        return topCat != null ? topCat.name() : "None";
    }
}
