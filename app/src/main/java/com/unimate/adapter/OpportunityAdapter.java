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
import com.unimate.model.Opportunity;

import java.util.ArrayList;
import java.util.List;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.OppViewHolder> {

    public interface OnOppActionListener {
        void onOppClick(Opportunity opp);
        void onOppFavoriteToggle(Opportunity opp);
    }

    private final Context context;
    private List<Opportunity> opportunities;
    private final List<Long> favoriteIds;
    private final OnOppActionListener listener;

    public OpportunityAdapter(Context context, List<Opportunity> opportunities, List<Long> favoriteIds, OnOppActionListener listener) {
        this.context = context;
        this.opportunities = opportunities != null ? opportunities : new ArrayList<>();
        this.favoriteIds = favoriteIds != null ? favoriteIds : new ArrayList<>();
        this.listener = listener;
    }

    public void setOpportunities(List<Opportunity> opportunities, List<Long> favoriteIds) {
        this.opportunities = opportunities != null ? opportunities : new ArrayList<>();
        if (favoriteIds != null) {
            this.favoriteIds.clear();
            this.favoriteIds.addAll(favoriteIds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_opportunity, parent, false);
        return new OppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OppViewHolder holder, int position) {
        Opportunity opp = opportunities.get(position);
        holder.bind(opp);
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    class OppViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textOrg;
        TextView textDeadline;
        Chip chipCategory;
        ImageButton btnFavorite;

        public OppViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textOppTitle);
            textOrg = itemView.findViewById(R.id.textOppOrg);
            textDeadline = itemView.findViewById(R.id.textOppDeadline);
            chipCategory = itemView.findViewById(R.id.chipOppCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavOpp);
        }

        public void bind(Opportunity opp) {
            textTitle.setText(opp.getTitle());
            textOrg.setText(opp.getOrganization() != null ? opp.getOrganization() : "University Opportunity");
            textDeadline.setText("Deadline: " + (opp.getDeadline() != null ? opp.getDeadline() : "Open"));

            chipCategory.setText(opp.getCategory() != null ? opp.getCategory().name() : "EVENT");

            boolean isFav = favoriteIds.contains(opp.getId());
            btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOppClick(opp);
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onOppFavoriteToggle(opp);
            });
        }
    }
}
