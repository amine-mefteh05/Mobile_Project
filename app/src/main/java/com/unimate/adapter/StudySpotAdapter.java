package com.unimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.unimate.R;
import com.unimate.dto.ScoredPlace;
import com.unimate.model.StudyPlace;
import com.unimate.view.ui.UiKit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        this.favoriteIds = favoriteIds != null ? new ArrayList<>(favoriteIds) : new ArrayList<>();
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
        holder.bind(scoredPlaces.get(position));
    }

    @Override
    public int getItemCount() {
        return scoredPlaces.size();
    }

    class SpotViewHolder extends RecyclerView.ViewHolder {

        final TextView textName, textAddress, textScore, textBreakdown;
        final TextView textWifi, textOutlets, textNoise, textPrice;
        final LinearProgressIndicator progressScore;
        final ImageButton btnFavorite;

        SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textSpotName);
            textAddress = itemView.findViewById(R.id.textSpotAddress);
            textScore = itemView.findViewById(R.id.textSpotScore);
            textBreakdown = itemView.findViewById(R.id.textSpotBreakdown);
            textWifi = itemView.findViewById(R.id.textSpotWifi);
            textOutlets = itemView.findViewById(R.id.textSpotOutlets);
            textNoise = itemView.findViewById(R.id.textSpotNoise);
            textPrice = itemView.findViewById(R.id.textSpotPrice);
            progressScore = itemView.findViewById(R.id.progressSpotScore);
            btnFavorite = itemView.findViewById(R.id.btnFavSpot);

            UiKit.setLeadingIcon(textWifi, R.drawable.ic_wifi, R.color.text_secondary, 14);
            UiKit.setLeadingIcon(textOutlets, R.drawable.ic_power, R.color.text_secondary, 14);
            UiKit.setLeadingIcon(textNoise, R.drawable.ic_volume, R.color.text_secondary, 14);
            UiKit.setLeadingIcon(textPrice, R.drawable.ic_budget, R.color.text_secondary, 14);
        }

        void bind(ScoredPlace item) {
            StudyPlace place = item.getPlace();
            if (place == null) return;

            textName.setText(place.getName());
            String category = place.getCategory() != null ? place.getCategory() : "Study spot";
            String address = place.getAddress() != null ? place.getAddress() : "";
            textAddress.setText(address.isEmpty() ? category : category + " · " + address);

            int score = (int) Math.round(Math.max(0, Math.min(100, item.getScore())));
            textScore.setText(String.format(Locale.getDefault(), "%d", score));
            progressScore.setProgressCompat(score, false);

            String breakdown = item.getBreakdown();
            // The engine's raw breakdown duplicates the score; keep it for accessibility only.
            textBreakdown.setVisibility(View.GONE);
            textBreakdown.setText(breakdown);

            textWifi.setVisibility(place.isWifi() ? View.VISIBLE : View.GONE);
            textOutlets.setVisibility(place.isOutlets() ? View.VISIBLE : View.GONE);
            textNoise.setText(UiKit.noiseLabel(place.getNoiseLevel()));
            textPrice.setText(UiKit.priceLabel(place.getPriceLevel()));

            boolean isFav = favoriteIds.contains(place.getId());
            btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            btnFavorite.setImageTintList(isFav ? null : UiKit.colorList(context, R.color.text_tertiary));
            btnFavorite.setContentDescription(isFav ? "Remove " + place.getName() + " from saved" : "Save " + place.getName());

            itemView.setContentDescription(place.getName() + ", match score " + score);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSpotClick(place);
            });
            btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onSpotFavoriteToggle(place);
            });
        }
    }
}
