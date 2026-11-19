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
import com.unimate.dto.ScoredPlace;
import com.unimate.model.StudyPlace;

import java.util.ArrayList;
import java.util.List;

public class StudySpotAdapter extends RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder> {

    public interface OnSpotActionListener {
        void onSpotClick(StudyPlace place);
        void onSpotFavoriteToggle(StudyPlace place);
    }

    private final Context context;
    private List<ScoredPlace> scoredPlaces;
    private final List<Long> favoriteIds;
    private final OnSpotActionListener listener;

    public StudySpotAdapter(Context context, List<ScoredPlace> scoredPlaces, List<Long> favoriteIds, OnSpotActionListener listener) {
        this.context = context;
        this.scoredPlaces = scoredPlaces != null ? scoredPlaces : new ArrayList<>();
        this.favoriteIds = favoriteIds != null ? favoriteIds : new ArrayList<>();
        this.listener = listener;
    }

    public void setPlaces(List<ScoredPlace> scoredPlaces, List<Long> favoriteIds) {
        this.scoredPlaces = scoredPlaces != null ? scoredPlaces : new ArrayList<>();
        if (favoriteIds != null) {
            this.favoriteIds.clear();
            this.favoriteIds.addAll(favoriteIds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_study_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        ScoredPlace item = scoredPlaces.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return scoredPlaces.size();
    }

    class SpotViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        TextView textAddress;
        TextView textScore;
        TextView textBreakdown;
        Chip chipCategory;
        Chip chipWifi;
        Chip chipOutlets;
        ImageButton btnFavorite;

        public SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textSpotName);
            textAddress = itemView.findViewById(R.id.textSpotAddress);
            textScore = itemView.findViewById(R.id.textSpotScore);
            textBreakdown = itemView.findViewById(R.id.textSpotBreakdown);
            chipCategory = itemView.findViewById(R.id.chipSpotCategory);
            chipWifi = itemView.findViewById(R.id.chipSpotWifi);
            chipOutlets = itemView.findViewById(R.id.chipSpotOutlets);
            btnFavorite = itemView.findViewById(R.id.btnFavSpot);
        }

        public void bind(ScoredPlace item) {
            StudyPlace place = item.getPlace();
            if (place == null) return;

            textName.setText(place.getName());
            textAddress.setText(place.getAddress());
            textScore.setText(String.format("%.0f/100", item.getScore()));
            textBreakdown.setText(item.getBreakdown());
            chipCategory.setText(place.getCategory());

            chipWifi.setVisibility(place.isWifi() ? View.VISIBLE : View.GONE);
            chipOutlets.setVisibility(place.isOutlets() ? View.VISIBLE : View.GONE);

            boolean isFav = favoriteIds.contains(place.getId());
            btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSpotClick(place);
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onSpotFavoriteToggle(place);
            });
        }
    }
}
