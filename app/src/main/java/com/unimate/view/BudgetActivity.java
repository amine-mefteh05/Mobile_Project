package com.unimate.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unimate.R;
import com.unimate.adapter.ExpenseAdapter;
import com.unimate.controller.BudgetController;
import com.unimate.model.Expense;
import com.unimate.view.ui.UiKit;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity {

    private TextView textLimit, textSpent, textRemaining, textExpenseCount;
    private LinearProgressIndicator progressBudget;
    private MaterialCardView cardWarning;
    private TextView textWarningTitle, textWarningBody;
    private View sectionBreakdown;
    private LinearLayout layoutBreakdown;
    private RecyclerView recyclerViewExpenses;
    private View emptyExpenses;
    private ExtendedFloatingActionButton fabAddExpense;

    private BudgetController budgetController;
    private ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        UiKit.setupSecondaryScreen(this, R.id.toolbarBudget, "Weekly budget");

        budgetController = new BudgetController(this);

        initViews();
        setupRecyclerView();

        fabAddExpense.setOnClickListener(v -> showAddExpenseSheet());
        findViewById(R.id.btnEditBudgetLimit).setOnClickListener(v -> showEditLimitDialog());

        loadBudgetData();
    }

    private void initViews() {
        textLimit = findViewById(R.id.textBudgetLimit);
        textSpent = findViewById(R.id.textBudgetSpent);
        textRemaining = findViewById(R.id.textBudgetRemaining);
        textExpenseCount = findViewById(R.id.textExpenseCount);
        progressBudget = findViewById(R.id.progressBudget);
        cardWarning = findViewById(R.id.cardBudgetWarning);
        textWarningTitle = findViewById(R.id.textBudgetWarningTitle);
        textWarningBody = findViewById(R.id.textBudgetWarningBody);
        sectionBreakdown = findViewById(R.id.sectionBreakdown);
        layoutBreakdown = findViewById(R.id.layoutCategoryBreakdown);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        emptyExpenses = findViewById(R.id.emptyExpenses);
        fabAddExpense = findViewById(R.id.fabAddExpense);

        NestedScrollView scroll = findViewById(R.id.scrollBudget);
        UiKit.padForSystemBars(scroll, false, true);
        scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, x, y, ox, oy) -> {
            if (y > oy + 4 && fabAddExpense.isExtended()) fabAddExpense.shrink();
            else if (y < oy - 4 && !fabAddExpense.isExtended()) fabAddExpense.extend();
        });
        applyFabInset();
    }

    /** Keeps the FAB above the gesture bar when drawing edge-to-edge. */
    private void applyFabInset() {
        final int base = ((ViewGroup.MarginLayoutParams) fabAddExpense.getLayoutParams()).bottomMargin;
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(fabAddExpense, (v, insets) -> {
            int bottom = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = base + bottom;
            v.setLayoutParams(lp);
            return insets;
        });
    }

    private void setupRecyclerView() {
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        UiKit.addDividers(recyclerViewExpenses);
        expenseAdapter = new ExpenseAdapter(this, null, this::confirmDelete);
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void loadBudgetData() {
        double limit = budgetController.getWeeklyBudgetLimit();
        double spent = budgetController.getCurrentWeekSpent();
        double remaining = budgetController.getRemainingBudget();
        boolean isTight = budgetController.isBudgetTight();

        textSpent.setText(UiKit.money(spent));
        textLimit.setText(String.format(Locale.getDefault(), "of %.2f TND · resets Monday", limit));

        int progress = limit > 0 ? (int) Math.min(100, Math.round(spent * 100 / limit)) : 0;
        progressBudget.setProgressCompat(progress, true);

        @ColorRes int tone = spent > limit ? R.color.danger : isTight ? R.color.warning : R.color.brand;
        progressBudget.setIndicatorColor(UiKit.color(this, tone));
        progressBudget.setTrackColor(UiKit.color(this, R.color.surface_muted));

        // The controller clamps "remaining" at 0, so derive overspend from spent vs. limit.
        boolean over = spent > limit;
        if (over) {
            textRemaining.setText(UiKit.money(spent - limit) + " over limit");
            textRemaining.setTextColor(UiKit.color(this, R.color.danger));
        } else {
            textRemaining.setText(UiKit.money(Math.max(0, remaining)) + " left");
            textRemaining.setTextColor(UiKit.color(this, isTight ? R.color.warning : R.color.success));
        }

        cardWarning.setVisibility(isTight || over ? View.VISIBLE : View.GONE);
        textWarningTitle.setText(over ? "You're over budget" : "Budget is getting tight");
        textWarningBody.setText(over
                ? "You've spent more than this week's limit. Free study spots will rank higher."
                : "You've used 80% or more of this week's limit. Free study spots will rank higher.");

        bindBreakdown(spent);

        List<Expense> expenses = budgetController.getCurrentWeekExpenses();
        int count = expenses == null ? 0 : expenses.size();
        textExpenseCount.setText(count == 0 ? "" : UiKit.plural(count, "item") + " this week");
        if (count == 0) {
            UiKit.bindEmptyState(emptyExpenses, R.drawable.ic_budget, "No expenses yet",
                    "Track what you spend this week to stay within your limit.");
            emptyExpenses.setVisibility(View.VISIBLE);
            recyclerViewExpenses.setVisibility(View.GONE);
        } else {
            emptyExpenses.setVisibility(View.GONE);
            recyclerViewExpenses.setVisibility(View.VISIBLE);
            expenseAdapter.setExpenses(expenses);
        }
    }

    private void bindBreakdown(double totalSpent) {
        layoutBreakdown.removeAllViews();
        Map<Expense.Category, Double> breakdown = budgetController.getCategoryBreakdown();
        if (breakdown == null || totalSpent <= 0) {
            sectionBreakdown.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int rows = 0;
        for (Expense.Category category : Expense.Category.values()) {
            Double amount = breakdown.get(category);
            if (amount == null || amount <= 0) continue;

            View row = inflater.inflate(R.layout.item_category_breakdown, layoutBreakdown, false);
            int hue = UiKit.expenseColor(category);

            ImageView icon = row.findViewById(R.id.iconBreakdown);
            icon.setImageResource(UiKit.expenseIcon(category));
            icon.setImageTintList(UiKit.colorList(this, hue));
            icon.setBackgroundTintList(UiKit.softTint(this, hue));

            ((TextView) row.findViewById(R.id.textBreakdownName)).setText(UiKit.label(category));
            ((TextView) row.findViewById(R.id.textBreakdownAmount)).setText(UiKit.money(amount));

            LinearProgressIndicator bar = row.findViewById(R.id.progressBreakdown);
            bar.setIndicatorColor(UiKit.color(this, hue));
            bar.setTrackColor(UiKit.color(this, R.color.surface_muted));
            bar.setProgressCompat((int) Math.round(amount * 100 / totalSpent), false);

            layoutBreakdown.addView(row);
            rows++;
        }
        sectionBreakdown.setVisibility(rows > 0 ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Expense expense) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete expense?")
                .setMessage("“" + expense.getDescription() + "” (" + UiKit.money(expense.getAmount())
                        + ") will be removed from this week.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    if (budgetController.deleteExpense(expense.getId())) {
                        loadBudgetData();
                        showMessage("Expense deleted");
                    }
                })
                .show();
    }

    private void showAddExpenseSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        sheet.setContentView(content);
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
        if (sheet.getWindow() != null) {
            sheet.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        TextInputLayout layoutAmount = content.findViewById(R.id.layoutExpenseAmount);
        TextInputLayout layoutDesc = content.findViewById(R.id.layoutExpenseDescription);
        TextInputEditText editAmount = content.findViewById(R.id.editExpenseAmount);
        TextInputEditText editDesc = content.findViewById(R.id.editExpenseDescription);
        ChipGroup chipGroup = content.findViewById(R.id.chipGroupExpenseCategory);

        UiKit.clearErrorOnEdit(layoutAmount, layoutDesc);
        editAmount.requestFocus();

        content.findViewById(R.id.btnSaveExpense).setOnClickListener(v -> {
            String amountStr = editAmount.getText() != null ? editAmount.getText().toString() : "";
            String desc = editDesc.getText() != null ? editDesc.getText().toString() : "";

            layoutAmount.setError(null);
            layoutDesc.setError(null);
            String error = budgetController.validateExpenseInput(amountStr, desc);
            if (error != null) {
                // Point the message at the field it concerns.
                boolean aboutDescription = error.toLowerCase(Locale.ROOT).contains("description");
                (aboutDescription ? layoutDesc : layoutAmount).setError(error);
                return;
            }

            Expense expense = new Expense();
            expense.setAmount(Double.parseDouble(amountStr.trim()));
            expense.setDescription(desc.trim());
            Chip checked = chipGroup.findViewById(chipGroup.getCheckedChipId());
            String catStr = checked != null && checked.getTag() != null ? checked.getTag().toString() : "OTHER";
            try { expense.setCategory(Expense.Category.valueOf(catStr)); } catch (Exception ignored) {}

            if (budgetController.addExpense(expense)) {
                sheet.dismiss();
                loadBudgetData();
                showMessage("Expense added");
            } else {
                layoutAmount.setError("Couldn't save this expense. Try again.");
            }
        });

        sheet.show();
    }

    private void showEditLimitDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_budget_limit, null);
        TextInputLayout layout = content.findViewById(R.id.layoutBudgetLimit);
        TextInputEditText edit = content.findViewById(R.id.editBudgetLimit);
        edit.setText(String.format(Locale.US, "%.2f", budgetController.getWeeklyBudgetLimit()));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Weekly limit")
                .setMessage("How much do you plan to spend from Monday to Sunday?")
                .setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        // Validate before closing.
        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String raw = edit.getText() != null ? edit.getText().toString().trim() : "";
                    double value;
                    try {
                        value = Double.parseDouble(raw);
                    } catch (NumberFormatException e) {
                        value = -1;
                    }
                    if (value <= 0) {
                        layout.setError("Enter an amount greater than 0");
                        return;
                    }
                    budgetController.setWeeklyBudgetLimit(value);
                    dialog.dismiss();
                    loadBudgetData();
                    showMessage("Weekly limit updated");
                }));
        dialog.show();
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAnchorView(fabAddExpense)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
