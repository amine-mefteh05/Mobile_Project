package com.unimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.unimate.R;
import com.unimate.model.Expense;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView textDescription;
        TextView textDate;
        TextView textAmount;
        Chip chipCategory;
        ImageButton btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.textExpenseDesc);
            textDate = itemView.findViewById(R.id.textExpenseDate);
            textAmount = itemView.findViewById(R.id.textExpenseAmount);
            chipCategory = itemView.findViewById(R.id.chipExpenseCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteExpense);
        }

        public void bind(Expense expense) {
            textDescription.setText(expense.getDescription());
            textDate.setText(expense.getDate());
            textAmount.setText(String.format(Locale.getDefault(), "%.2f TND", expense.getAmount()));

            chipCategory.setText(expense.getCategory() != null ? expense.getCategory().name() : "OTHER");

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onExpenseDelete(expense);
            });
        }
    }
}
