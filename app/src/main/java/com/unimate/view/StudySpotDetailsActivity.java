package com.unimate.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.unimate.R;
import com.unimate.controller.StudySpotController;
import com.unimate.dto.ScoredPlace;
import com.unimate.model.StudyPlace;
import com.unimate.view.ui.UiKit;

import java.util.List;
import java.util.Locale;

public class StudySpotDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "extra_place_id";

    private TextView textName, textAddress, textHours, textPrice, textNoise;
    private TextView textCategory, textWifi, textOutlets, textScore;
    private LinearProgressIndicator progressScore;
    private View cardMatch, bottomBar;
    private ImageButton btnFavorite;
    private View btnOpenMaps, btnCompare;

    private StudySpotController spotController;
    private long placeId = -1;
    private StudyPlace place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_spot_details);
        UiKit.setupSecondaryScreen(this, R.id.toolbarSpotDetails, "");

        spotController = new StudySpotController(this);

        initViews();

        placeId = getIntent().getLongExtra(EXTRA_PLACE_ID, -1);
        if (placeId > 0) {
            loadPlaceDetails();
        }
        if (place == null) {
            Toast.makeText(this, "Place not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
    }

    private void initViews() {
        textName = findViewById(R.id.textDetailsName);
        textAddress = findViewById(R.id.textDetailsAddress);
        textHours = findViewById(R.id.textDetailsHours);
        textPrice = findViewById(R.id.textDetailsPrice);
        textNoise = findViewById(R.id.textDetailsNoise);
        textCategory = findViewById(R.id.textDetailsCategory);
        textWifi = findViewById(R.id.textDetailsWifi);
        textOutlets = findViewById(R.id.textDetailsOutlets);
        textScore = findViewById(R.id.textDetailsScore);
        progressScore = findViewById(R.id.progressDetailsScore);
        cardMatch = findViewById(R.id.cardDetailsMatch);
        btnFavorite = findViewById(R.id.btnDetailsFavorite);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);
        btnCompare = findViewById(R.id.btnCompareSpot);
        bottomBar = findViewById(R.id.bottomBarSpotDetails);
        UiKit.padForSystemBars(bottomBar, false, true);
    }

    private void loadPlaceDetails() {
        place = spotController.getPlaceById(placeId);
        if (place == null) return;

        textName.setText(place.getName());
        textAddress.setText(place.getAddress());
        UiKit.setLeadingIcon(textAddress, R.drawable.ic_location, R.color.text_tertiary, 16);
        textAddress.setCompoundDrawablePadding(UiKit.dp(this, 6));

        textCategory.setText(UiKit.label(place.getCategory()));
        UiKit.styleTag(textCategory, R.color.brand);

        String hours = place.getOpeningHours() != null ? place.getOpeningHours() : "08:00 - 20:00";
        textHours.setText(hours.replace(" - ", " – "));
        textPrice.setText(UiKit.priceLabel(place.getPriceLevel()));
        textNoise.setText(String.format(Locale.getDefault(), "%s · %d/5",
                UiKit.noiseLabel(place.getNoiseLevel()), place.getNoiseLevel()));

        bindAmenity(textWifi, place.isWifi(), R.drawable.ic_wifi, "Wi-Fi", "No Wi-Fi");
        bindAmenity(textOutlets, place.isOutlets(), R.drawable.ic_power, "Power outlets", "No outlets");

        bindMatchScore();
        updateFavoriteIcon();
    }

    private void bindAmenity(TextView view, boolean available, int icon, String yes, String no) {
        view.setText(available ? yes : no);
        int hue = available ? R.color.success : R.color.hue_gray;
        UiKit.styleTag(view, hue);
        UiKit.setLeadingIcon(view, icon, hue, 14);
    }

    /** Uses the same ranking engine as Discover so the number matches what the list shows. */
    private void bindMatchScore() {
        List<ScoredPlace> ranked = spotController.getRankedPlaces(null, "All");
        if (ranked == null) return;
        for (ScoredPlace sp : ranked) {
            if (sp.getPlace() != null && sp.getPlace().getId() == placeId) {
                int score = (int) Math.round(sp.getScore());
                textScore.setText(String.valueOf(score));
                progressScore.setProgressCompat(Math.max(0, Math.min(100, score)), false);
                cardMatch.setVisibility(View.VISIBLE);
                return;
            }
        }
    }

    private void updateFavoriteIcon() {
        boolean isFav = spotController.isFavorite(placeId);
        btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        btnFavorite.setImageTintList(UiKit.colorList(this, isFav ? R.color.favorite : R.color.text_secondary));
        btnFavorite.setContentDescription(isFav ? "Remove from favorites" : "Save to favorites");
    }

    private void setupListeners() {
        btnFavorite.setOnClickListener(v -> {
            boolean isNowFav = spotController.toggleFavorite(placeId);
            updateFavoriteIcon();
            showMessage(isNowFav ? "Saved to favorites" : "Removed from favorites");
        });

        btnOpenMaps.setOnClickListener(v -> {
            if (place == null) return;
            // Geo intent (no API key needed). Prefer Google Maps, fall back to any map app.
            String geoUri = String.format(Locale.US, "geo:%f,%f?q=%s",
                    place.getLatitude(), place.getLongitude(), Uri.encode(place.getName()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (Exception e) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)));
                } catch (Exception none) {
                    showMessage("No maps app installed");
                }
            }
        });

        btnCompare.setOnClickListener(v -> {
            Intent intent = new Intent(this, CompareStudySpotsActivity.class);
            intent.putExtra(CompareStudySpotsActivity.EXTRA_SPOT_1_ID, placeId);
            startActivity(intent);
        });
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAnchorView(bottomBar)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
