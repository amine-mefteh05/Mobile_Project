package com.unimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unimate.R;
import com.unimate.model.Expense;
import com.unimate.view.ui.UiKit;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onExpenseDelete(Expense expense);
    }

    private final Context context;
    private List<Expense> expenses;
    private final OnExpenseActionListener listener;

    public ExpenseAdapter(Context context, List<Expense> expenses, OnExpenseActionListener listener) {
        this.context = context;
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        final TextView textDescription, textDate, textAmount;
        final ImageView iconCategory;
        final ImageButton btnDelete;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.textExpenseDesc);
            textDate = itemView.findViewById(R.id.textExpenseDate);
            textAmount = itemView.findViewById(R.id.textExpenseAmount);
            iconCategory = itemView.findViewById(R.id.iconExpenseCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteExpense);
        }

        void bind(Expense expense) {
            Expense.Category category = expense.getCategory() != null ? expense.getCategory() : Expense.Category.OTHER;
            int hue = UiKit.expenseColor(category);

            textDescription.setText(expense.getDescription());
            textDate.setText(UiKit.label(category) + " · " + UiKit.prettyDate(expense.getDate()));
            textAmount.setText(UiKit.money(expense.getAmount()));

            iconCategory.setImageResource(UiKit.expenseIcon(category));
            iconCategory.setImageTintList(UiKit.colorList(context, hue));
            iconCategory.setBackgroundTintList(UiKit.softTint(context, hue));

            btnDelete.setContentDescription("Delete " + expense.getDescription());
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onExpenseDelete(expense);
            });
        }
    }
}
