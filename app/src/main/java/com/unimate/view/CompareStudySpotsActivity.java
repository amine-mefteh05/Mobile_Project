package com.unimate.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unimate.R;
import com.unimate.controller.StudySpotController;
import com.unimate.dto.ScoredPlace;
import com.unimate.model.StudyPlace;
import com.unimate.view.ui.UiKit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompareStudySpotsActivity extends AppCompatActivity {

    public static final String EXTRA_SPOT_1_ID = "extra_spot_1_id";

    private AutoCompleteTextView spinnerSpot1, spinnerSpot2;
    private TextView textName1, textPrice1, textNoise1, textWifi1, textOutlets1, textScore1;
    private TextView textName2, textPrice2, textNoise2, textWifi2, textOutlets2, textScore2;
    private TextView textVerdict;
    private View cardVerdict, cardTable, textLegend, emptyCompare;

    private StudySpotController spotController;
    private List<ScoredPlace> rankedPlaces;
    private ScoredPlace place1, place2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_study_spots);
        UiKit.setupSecondaryScreen(this, R.id.toolbarCompare, "Compare spots");
        UiKit.padForSystemBars(findViewById(R.id.scrollCompare), false, true);

        spotController = new StudySpotController(this);

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

        textVerdict = findViewById(R.id.textCompareVerdict);
        cardVerdict = findViewById(R.id.cardCompareVerdict);
        cardTable = findViewById(R.id.cardCompareTable);
        textLegend = findViewById(R.id.textCompareLegend);
        emptyCompare = findViewById(R.id.emptyCompare);
    }

    private void loadPlacesData() {
        rankedPlaces = spotController.getRankedPlaces(null, "All");
        if (rankedPlaces == null || rankedPlaces.size() < 2) {
            showEmpty();
            return;
        }

        List<String> names = new ArrayList<>();
        for (ScoredPlace sp : rankedPlaces) {
            if (sp.getPlace() != null) names.add(sp.getPlace().getName());
        }

        spinnerSpot1.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
        spinnerSpot2.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));

        long spot1Id = getIntent().getLongExtra(EXTRA_SPOT_1_ID, -1);
        if (spot1Id > 0) {
            for (ScoredPlace sp : rankedPlaces) {
                if (sp.getPlace() != null && sp.getPlace().getId() == spot1Id) {
                    place1 = sp;
                    break;
                }
            }
        }
        if (place1 == null) place1 = rankedPlaces.get(0);

        // Default the second slot to the best-ranked spot that isn't the first.
        for (ScoredPlace sp : rankedPlaces) {
            if (sp != place1) {
                place2 = sp;
                break;
            }
        }

        spinnerSpot1.setText(place1.getPlace().getName(), false);
        if (place2 != null) spinnerSpot2.setText(place2.getPlace().getName(), false);

        updateComparisonUI();

        spinnerSpot1.setOnItemClickListener((parent, view, position, id) -> {
            place1 = findByName((String) parent.getItemAtPosition(position));
            updateComparisonUI();
        });

        spinnerSpot2.setOnItemClickListener((parent, view, position, id) -> {
            place2 = findByName((String) parent.getItemAtPosition(position));
            updateComparisonUI();
        });
    }

    private void showEmpty() {
        cardVerdict.setVisibility(View.GONE);
        cardTable.setVisibility(View.GONE);
        textLegend.setVisibility(View.GONE);
        UiKit.bindEmptyState(emptyCompare, R.drawable.ic_compare, "Not enough spots to compare",
                "At least two study spots are needed.");
        emptyCompare.setVisibility(View.VISIBLE);
    }

    private ScoredPlace findByName(String name) {
        if (rankedPlaces == null || name == null) return null;
        for (ScoredPlace sp : rankedPlaces) {
            if (sp.getPlace() != null && sp.getPlace().getName().equalsIgnoreCase(name)) return sp;
        }
        return null;
    }

    private void updateComparisonUI() {
        if (place1 == null || place2 == null || place1.getPlace() == null || place2.getPlace() == null) return;
        StudyPlace p1 = place1.getPlace();
        StudyPlace p2 = place2.getPlace();

        textName1.setText(p1.getName());
        textName2.setText(p2.getName());

        // For each row, compare(a, b) > 0 means the first spot wins.
        bindRow(textScore1, textScore2,
                String.valueOf(Math.round(place1.getScore())), String.valueOf(Math.round(place2.getScore())),
                Double.compare(place1.getScore(), place2.getScore()));
        bindRow(textPrice1, textPrice2,
                UiKit.priceLabel(p1.getPriceLevel()), UiKit.priceLabel(p2.getPriceLevel()),
                Integer.compare(p2.getPriceLevel(), p1.getPriceLevel()));
        bindRow(textNoise1, textNoise2,
                UiKit.noiseLabel(p1.getNoiseLevel()), UiKit.noiseLabel(p2.getNoiseLevel()),
                Integer.compare(p2.getNoiseLevel(), p1.getNoiseLevel()));
        bindRow(textWifi1, textWifi2, yesNo(p1.isWifi()), yesNo(p2.isWifi()),
                Boolean.compare(p1.isWifi(), p2.isWifi()));
        bindRow(textOutlets1, textOutlets2, yesNo(p1.isOutlets()), yesNo(p2.isOutlets()),
                Boolean.compare(p1.isOutlets(), p2.isOutlets()));

        textVerdict.setText(buildVerdict(place1, place2));
    }

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private void bindRow(TextView a, TextView b, String valueA, String valueB, int comparison) {
        a.setText(valueA);
        b.setText(valueB);
        styleCell(a, comparison > 0);
        styleCell(b, comparison < 0);
    }

    private void styleCell(TextView cell, boolean winner) {
        cell.setTextColor(UiKit.color(this, winner ? R.color.brand : R.color.text_secondary));
        cell.setTypeface(Typeface.create(winner ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
    }

    private String buildVerdict(ScoredPlace a, ScoredPlace b) {
        if (a == b || a.getPlace().getId() == b.getPlace().getId()) {
            return "You picked the same spot twice — choose a different second spot to compare.";
        }
        long scoreA = Math.round(a.getScore());
        long scoreB = Math.round(b.getScore());
        if (scoreA == scoreB) {
            List<String> edgeA = advantages(a, b);
            List<String> edgeB = advantages(b, a);
            String verdict = "Even match on your preferences (" + scoreA + ").";
            if (!edgeA.isEmpty()) verdict += " " + a.getPlace().getName() + " " + String.join(", ", edgeA) + ".";
            if (!edgeB.isEmpty()) verdict += " " + b.getPlace().getName() + " " + String.join(", ", edgeB) + ".";
            if (edgeA.isEmpty() && edgeB.isEmpty()) verdict += " Pick whichever is closer.";
            return verdict;
        }
        ScoredPlace best = scoreA > scoreB ? a : b;
        ScoredPlace other = best == a ? b : a;
        List<String> reasons = advantages(best, other);

        String verdict = String.format(Locale.getDefault(), "%s is the better match for you (%d vs %d)",
                best.getPlace().getName(), Math.max(scoreA, scoreB), Math.min(scoreA, scoreB));
        if (!reasons.isEmpty()) verdict += " — it " + String.join(", ", reasons);
        return verdict + ".";
    }

    /** What {@code x} offers that {@code y} doesn't, phrased to follow the place name. */
    private static List<String> advantages(ScoredPlace x, ScoredPlace y) {
        List<String> out = new ArrayList<>();
        StudyPlace p = x.getPlace(), q = y.getPlace();
        if (p.getNoiseLevel() < q.getNoiseLevel()) out.add("is quieter");
        if (p.getPriceLevel() < q.getPriceLevel()) out.add("is cheaper");
        if (p.isWifi() && !q.isWifi()) out.add("has Wi-Fi");
        if (p.isOutlets() && !q.isOutlets()) out.add("has outlets");
        return out;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
