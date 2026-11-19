package com.unimate.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.unimate.R;
import com.unimate.controller.StudySpotController;
import com.unimate.dto.ScoredPlace;
import com.unimate.model.StudyPlace;

import java.util.ArrayList;
import java.util.List;

public class CompareStudySpotsActivity extends AppCompatActivity {

    public static final String EXTRA_SPOT_1_ID = "extra_spot_1_id";

    private AutoCompleteTextView spinnerSpot1, spinnerSpot2;
    private TextView textName1, textPrice1, textNoise1, textWifi1, textOutlets1, textScore1;
    private TextView textName2, textPrice2, textNoise2, textWifi2, textOutlets2, textScore2;

    private StudySpotController spotController;
    private List<ScoredPlace> rankedPlaces;
    private ScoredPlace place1, place2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_study_spots);

        spotController = new StudySpotController(this);

        Toolbar toolbar = findViewById(R.id.toolbarCompare);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Compare StudySpots");
        }

        initViews();
        loadPlacesData();
    }

    private void initViews() {
        spinnerSpot1 = findViewById(R.id.spinnerCompareSpot1);
        spinnerSpot2 = findViewById(R.id.spinnerCompareSpot2);

        textName1 = findViewById(R.id.textSpot1Name);
        textPrice1 = findViewById(R.id.textSpot1Price);
        textNoise1 = findViewById(R.id.textSpot1Noise);
        textWifi1 = findViewById(R.id.textSpot1Wifi);
        textOutlets1 = findViewById(R.id.textSpot1Outlets);
        textScore1 = findViewById(R.id.textSpot1Score);

        textName2 = findViewById(R.id.textSpot2Name);
        textPrice2 = findViewById(R.id.textSpot2Price);
        textNoise2 = findViewById(R.id.textSpot2Noise);
        textWifi2 = findViewById(R.id.textSpot2Wifi);
        textOutlets2 = findViewById(R.id.textSpot2Outlets);
        textScore2 = findViewById(R.id.textSpot2Score);
    }

    private void loadPlacesData() {
        rankedPlaces = spotController.getRankedPlaces(null, "All");
        if (rankedPlaces == null || rankedPlaces.isEmpty()) return;

        List<String> names = new ArrayList<>();
        for (ScoredPlace sp : rankedPlaces) {
            if (sp.getPlace() != null) {
                names.add(sp.getPlace().getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        spinnerSpot1.setAdapter(adapter);
        spinnerSpot2.setAdapter(adapter);

        long spot1Id = getIntent().getLongExtra(EXTRA_SPOT_1_ID, -1);
        if (spot1Id > 0) {
            for (ScoredPlace sp : rankedPlaces) {
                if (sp.getPlace() != null && sp.getPlace().getId() == spot1Id) {
                    place1 = sp;
                    spinnerSpot1.setText(sp.getPlace().getName(), false);
                    break;
                }
            }
        }

        if (place1 == null && !rankedPlaces.isEmpty()) {
            place1 = rankedPlaces.get(0);
            spinnerSpot1.setText(place1.getPlace().getName(), false);
        }

        if (rankedPlaces.size() > 1) {
            place2 = rankedPlaces.get(1);
            spinnerSpot2.setText(place2.getPlace().getName(), false);
        }

        updateComparisonUI();

        spinnerSpot1.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            place1 = findByName(selected);
            updateComparisonUI();
        });

        spinnerSpot2.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            place2 = findByName(selected);
            updateComparisonUI();
        });
    }

    private ScoredPlace findByName(String name) {
        if (rankedPlaces == null || name == null) return null;
        for (ScoredPlace sp : rankedPlaces) {
            if (sp.getPlace() != null && sp.getPlace().getName().equalsIgnoreCase(name)) {
                return sp;
            }
        }
        return null;
    }

    private void updateComparisonUI() {
        if (place1 != null && place1.getPlace() != null) {
            StudyPlace p1 = place1.getPlace();
            textName1.setText(p1.getName());
            textPrice1.setText("Price: " + p1.getPriceLevel() + "/3");
            textNoise1.setText("Noise: " + p1.getNoiseLevel() + "/5");
            textWifi1.setText("Wi-Fi: " + (p1.isWifi() ? "Yes" : "No"));
            textOutlets1.setText("Outlets: " + (p1.isOutlets() ? "Yes" : "No"));
            textScore1.setText(String.format("Score: %.0f/100", place1.getScore()));
        }

        if (place2 != null && place2.getPlace() != null) {
            StudyPlace p2 = place2.getPlace();
            textName2.setText(p2.getName());
            textPrice2.setText("Price: " + p2.getPriceLevel() + "/3");
            textNoise2.setText("Noise: " + p2.getNoiseLevel() + "/5");
            textWifi2.setText("Wi-Fi: " + (p2.isWifi() ? "Yes" : "No"));
            textOutlets2.setText("Outlets: " + (p2.isOutlets() ? "Yes" : "No"));
            textScore2.setText(String.format("Score: %.0f/100", place2.getScore()));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
