package com.unimate.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.unimate.R;
import com.unimate.controller.StudySpotController;
import com.unimate.model.StudyPlace;

public class StudySpotDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "extra_place_id";

    private TextView textName, textAddress, textHours, textPrice, textNoise;
    private Chip chipCategory, chipWifi, chipOutlets;
    private ImageButton btnFavorite;
    private Button btnOpenMaps, btnCompare;

    private StudySpotController spotController;
    private long placeId = -1;
    private StudyPlace place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_spot_details);

        spotController = new StudySpotController(this);

        Toolbar toolbar = findViewById(R.id.toolbarSpotDetails);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        placeId = getIntent().getLongExtra(EXTRA_PLACE_ID, -1);
        if (placeId > 0) {
            loadPlaceDetails();
        } else {
            Toast.makeText(this, "Place not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void initViews() {
        textName = findViewById(R.id.textDetailsName);
        textAddress = findViewById(R.id.textDetailsAddress);
        textHours = findViewById(R.id.textDetailsHours);
        textPrice = findViewById(R.id.textDetailsPrice);
        textNoise = findViewById(R.id.textDetailsNoise);
        chipCategory = findViewById(R.id.chipDetailsCategory);
        chipWifi = findViewById(R.id.chipDetailsWifi);
        chipOutlets = findViewById(R.id.chipDetailsOutlets);
        btnFavorite = findViewById(R.id.btnDetailsFavorite);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);
        btnCompare = findViewById(R.id.btnCompareSpot);
    }

    private void loadPlaceDetails() {
        place = spotController.getPlaceById(placeId);
        if (place == null) return;

        setTitle(place.getName());
        textName.setText(place.getName());
        textAddress.setText(place.getAddress());
        textHours.setText("Hours: " + (place.getOpeningHours() != null ? place.getOpeningHours() : "08:00 - 20:00"));
        textPrice.setText("Price Level: " + place.getPriceLevel() + "/3");
        textNoise.setText("Noise Level: " + place.getNoiseLevel() + "/5 (" + getNoiseLabel(place.getNoiseLevel()) + ")");

        chipCategory.setText(place.getCategory());
        chipWifi.setText(place.isWifi() ? "Wi-Fi Available" : "No Wi-Fi");
        chipOutlets.setText(place.isOutlets() ? "Power Outlets Available" : "No Outlets");

        updateFavoriteIcon();
    }

    private String getNoiseLabel(int noise) {
        switch (noise) {
            case 1: return "Very Quiet";
            case 2: return "Quiet";
            case 3: return "Moderate";
            case 4: return "Busy";
            default: return "Loud";
        }
    }

    private void updateFavoriteIcon() {
        boolean isFav = spotController.isFavorite(placeId);
        btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    private void setupListeners() {
        btnFavorite.setOnClickListener(v -> {
            boolean isNowFav = spotController.toggleFavorite(placeId);
            Toast.makeText(this, isNowFav ? "Added to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
            updateFavoriteIcon();
        });

        btnOpenMaps.setOnClickListener(v -> {
            if (place != null) {
                // Geo intent specified in deployment diagram (no API key needed)
                String geoUri = String.format("geo:%f,%f?q=%s", place.getLatitude(), place.getLongitude(), Uri.encode(place.getName()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Intent genericMap = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                    startActivity(genericMap);
                }
            }
        });

        btnCompare.setOnClickListener(v -> {
            Intent intent = new Intent(this, CompareStudySpotsActivity.class);
            intent.putExtra(CompareStudySpotsActivity.EXTRA_SPOT_1_ID, placeId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
