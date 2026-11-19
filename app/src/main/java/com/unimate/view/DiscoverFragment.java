package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.unimate.R;
import com.unimate.adapter.OpportunityAdapter;
import com.unimate.adapter.StudySpotAdapter;
import com.unimate.controller.OpportunityController;
import com.unimate.controller.StudySpotController;
import com.unimate.dto.ScoredPlace;
import com.unimate.model.Favorite;
import com.unimate.model.Opportunity;
import com.unimate.model.StudyPlace;
import com.unimate.repository.FavoriteRepository;
import com.unimate.view.ui.UiKit;

import java.util.List;

public class DiscoverFragment extends Fragment {

    /** Filter values understood by the repositories ("All" means no filter). */
    private static final String[] SPOT_CATEGORIES = {"All", "Library", "Café", "Lounge", "Coworking"};
    private static final String[] OPP_CATEGORIES = {"All", "WORKSHOP", "EVENT", "HACKATHON", "INTERNSHIP", "SCHOLARSHIP", "RESOURCE"};

    private MaterialButtonToggleGroup toggleDiscover;
    private TextInputEditText editSearch;
    private ChipGroup chipGroupCategory;
    private RecyclerView recyclerViewDiscover;
    private View emptyDiscover;
    private TextView textCaption;

    private StudySpotController spotController;
    private OpportunityController oppController;
    private FavoriteRepository favRepo;

    private boolean isStudySpotsTab = true;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        spotController = new StudySpotController(requireContext());
        oppController = new OpportunityController(requireContext());
        favRepo = new FavoriteRepository(requireContext());

        initViews(view);
        setupTabs();
        setupSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDiscoverData();
    }

    private void initViews(View view) {
        toggleDiscover = view.findViewById(R.id.toggleDiscover);
        editSearch = view.findViewById(R.id.editDiscoverSearch);
        chipGroupCategory = view.findViewById(R.id.chipGroupDiscoverCategory);
        recyclerViewDiscover = view.findViewById(R.id.recyclerViewDiscover);
        emptyDiscover = view.findViewById(R.id.emptyDiscover);
        textCaption = view.findViewById(R.id.textDiscoverCaption);

        recyclerViewDiscover.setLayoutManager(new LinearLayoutManager(requireContext()));
        UiKit.addDividers(recyclerViewDiscover);
    }

    private void setupTabs() {
        toggleDiscover.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            isStudySpotsTab = (checkedId == R.id.segSpots);
            editSearch.setHint(isStudySpotsTab ? "Search study spots" : "Search opportunities");
            buildCategoryChips();
            loadDiscoverData();
        });
        buildCategoryChips();
    }

    private void buildCategoryChips() {
        chipGroupCategory.setOnCheckedStateChangeListener(null);
        chipGroupCategory.removeAllViews();
        selectedCategory = "All";

        String[] values = isStudySpotsTab ? SPOT_CATEGORIES : OPP_CATEGORIES;
        for (String value : values) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.view_filter_chip, chipGroupCategory, false);
            chip.setId(View.generateViewId());
            chip.setText("All".equals(value) ? "All" : UiKit.label(value));
            chip.setTag(value);
            chipGroupCategory.addView(chip);
            if ("All".equals(value)) chip.setChecked(true);
        }

        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedCategory = "All";
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null && chip.getTag() != null) selectedCategory = chip.getTag().toString();
            }
            loadDiscoverData();
        });
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDiscoverData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showEmpty(String title, String subtitle) {
        UiKit.bindEmptyState(emptyDiscover, isStudySpotsTab ? R.drawable.ic_location : R.drawable.ic_trophy, title, subtitle);
        emptyDiscover.setVisibility(View.VISIBLE);
        recyclerViewDiscover.setVisibility(View.GONE);
    }

    private void showList() {
        emptyDiscover.setVisibility(View.GONE);
        recyclerViewDiscover.setVisibility(View.VISIBLE);
    }

    private void loadDiscoverData() {
        if (!isAdded()) return;
        String query = editSearch.getText() != null ? editSearch.getText().toString() : "";
        String category = selectedCategory;
        boolean filtered = !query.trim().isEmpty() || !"All".equals(category);

        if (isStudySpotsTab) {
            List<ScoredPlace> places = spotController.getRankedPlaces(query, category);
            List<Long> favIds = favRepo.getFavoriteItemIds(Favorite.FavoriteType.PLACE);
            int count = places != null ? places.size() : 0;
            textCaption.setText(count == 0 ? "" : UiKit.plural(count, "spot") + ", ranked for your preferences");

            if (count == 0) {
                showEmpty(filtered ? "No spots match" : "No study spots yet",
                        filtered ? "Try a different search or category." : null);
                return;
            }
            showList();
            recyclerViewDiscover.setAdapter(new StudySpotAdapter(requireContext(), places, favIds, new StudySpotAdapter.OnSpotActionListener() {
                @Override
                public void onSpotClick(StudyPlace place) {
                    Intent intent = new Intent(requireContext(), StudySpotDetailsActivity.class);
                    intent.putExtra(StudySpotDetailsActivity.EXTRA_PLACE_ID, place.getId());
                    startActivity(intent);
                }

                @Override
                public void onSpotFavoriteToggle(StudyPlace place) {
                    spotController.toggleFavorite(place.getId());
                    loadDiscoverData();
                }
            }));
        } else {
            List<Opportunity> opps = oppController.getOpportunities(query, category);
            List<Long> favIds = favRepo.getFavoriteItemIds(Favorite.FavoriteType.OPPORTUNITY);
            int count = opps != null ? opps.size() : 0;
            textCaption.setText(count == 0 ? "" : count + (count == 1 ? " opportunity" : " opportunities") + ", soonest deadlines first");

            if (count == 0) {
                showEmpty(filtered ? "No opportunities match" : "No opportunities yet",
                        filtered ? "Try a different search or category." : null);
                return;
            }
            showList();
            recyclerViewDiscover.setAdapter(new OpportunityAdapter(requireContext(), opps, favIds, new OpportunityAdapter.OnOppActionListener() {
                @Override
                public void onOppClick(Opportunity opp) {
                    Intent intent = new Intent(requireContext(), OpportunityDetailsActivity.class);
                    intent.putExtra(OpportunityDetailsActivity.EXTRA_OPP_ID, opp.getId());
                    startActivity(intent);
                }

                @Override
                public void onOppFavoriteToggle(Opportunity opp) {
                    oppController.toggleFavorite(opp.getId());
                    loadDiscoverData();
                }
            }));
        }
    }
}
