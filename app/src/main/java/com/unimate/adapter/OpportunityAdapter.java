package com.unimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unimate.R;
import com.unimate.model.Opportunity;
import com.unimate.view.ui.UiKit;

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
        this.favoriteIds = favoriteIds != null ? new ArrayList<>(favoriteIds) : new ArrayList<>();
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
        holder.bind(opportunities.get(position));
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    class OppViewHolder extends RecyclerView.ViewHolder {

        final TextView textTitle, textOrg, textDeadline, textCategory, textDay, textMonth;
        final ImageButton btnFavorite;

        OppViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textOppTitle);
            textOrg = itemView.findViewById(R.id.textOppOrg);
            textDeadline = itemView.findViewById(R.id.textOppDeadline);
            textCategory = itemView.findViewById(R.id.textOppCategory);
            textDay = itemView.findViewById(R.id.textOppDay);
            textMonth = itemView.findViewById(R.id.textOppMonth);
            btnFavorite = itemView.findViewById(R.id.btnFavOpp);
        }

        void bind(Opportunity opp) {
            textTitle.setText(opp.getTitle());
            textOrg.setText(opp.getOrganization() != null ? opp.getOrganization() : "University opportunity");

            // Category is a coloured small-caps word, not a pill.
            textCategory.setText(opp.getCategory() != null ? UiKit.label(opp.getCategory()) : "Event");
            textCategory.setTextColor(UiKit.color(context, UiKit.opportunityColor(opp.getCategory())));

            // Date column: day + month of the deadline, or an em dash for rolling ones.
            java.time.LocalDate deadline = null;
            try {
                if (opp.getDeadline() != null) deadline = java.time.LocalDate.parse(opp.getDeadline().trim());
            } catch (Exception ignored) { }
            textDay.setText(deadline != null ? String.valueOf(deadline.getDayOfMonth()) : "—");
            textMonth.setText(deadline != null
                    ? deadline.format(java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.getDefault()))
                    : "Open");

            // Urgency is communicated by color: <7 days is a warning, closed is muted.
            long days = UiKit.daysUntil(opp.getDeadline());
            int tone = days < 0 ? R.color.text_tertiary : days < 7 ? R.color.warning : R.color.text_secondary;
            textDeadline.setText(UiKit.relativeClosing(opp.getDeadline()));
            textDeadline.setTextColor(UiKit.color(context, tone));
            UiKit.setLeadingIcon(textDeadline, R.drawable.ic_flag, tone, 14);

            boolean isFav = favoriteIds.contains(opp.getId());
            btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            btnFavorite.setImageTintList(isFav ? null : UiKit.colorList(context, R.color.text_tertiary));
            btnFavorite.setContentDescription(isFav ? "Remove " + opp.getTitle() + " from saved" : "Save " + opp.getTitle());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOppClick(opp);
            });
            btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onOppFavoriteToggle(opp);
            });
        }
    }
}
