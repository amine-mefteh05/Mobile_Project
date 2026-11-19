package com.unimate.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
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

import java.util.List;

public class DiscoverFragment extends Fragment {

    private TabLayout tabLayoutDiscover;
    private EditText editSearch;
    private Spinner spinnerCategory;
    private RecyclerView recyclerViewDiscover;
    private TextView textEmptyDiscover;

    private StudySpotController spotController;
    private OpportunityController oppController;
    private FavoriteRepository favRepo;

    private boolean isStudySpotsTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        spotController = new StudySpotController(requireContext());
        oppController = new OpportunityController(requireContext());
        favRepo = new FavoriteRepository(requireContext());

        initViews(view);
        setupTabs();
        setupSearchAndFilter();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDiscoverData();
    }

    private void initViews(View view) {
        tabLayoutDiscover = view.findViewById(R.id.tabLayoutDiscover);
        editSearch = view.findViewById(R.id.editDiscoverSearch);
        spinnerCategory = view.findViewById(R.id.spinnerDiscoverCategory);
        recyclerViewDiscover = view.findViewById(R.id.recyclerViewDiscover);
        textEmptyDiscover = view.findViewById(R.id.textEmptyDiscover);

        recyclerViewDiscover.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupTabs() {
        tabLayoutDiscover.removeAllTabs();
        tabLayoutDiscover.addTab(tabLayoutDiscover.newTab().setText("StudySpots"));
        tabLayoutDiscover.addTab(tabLayoutDiscover.newTab().setText("Opportunities"));

        tabLayoutDiscover.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isStudySpotsTab = (tab.getPosition() == 0);
                updateCategorySpinnerOptions();
                loadDiscoverData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        updateCategorySpinnerOptions();
    }

    private void updateCategorySpinnerOptions() {
        String[] options;
        if (isStudySpotsTab) {
            options = new String[]{"All", "Library", "Café", "Lounge", "Coworking"};
        } else {
            options = new String[]{"All", "WORKSHOP", "EVENT", "HACKATHON", "INTERNSHIP", "SCHOLARSHIP", "RESOURCE"};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
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

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDiscoverData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDiscoverData() {
        String query = editSearch.getText() != null ? editSearch.getText().toString() : "";
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "All";

        if (isStudySpotsTab) {
            List<ScoredPlace> places = spotController.getRankedPlaces(query, category);
            List<Long> favIds = favRepo.getFavoriteItemIds(Favorite.FavoriteType.PLACE);

            if (places == null || places.isEmpty()) {
                textEmptyDiscover.setVisibility(View.VISIBLE);
                recyclerViewDiscover.setVisibility(View.GONE);
            } else {
                textEmptyDiscover.setVisibility(View.GONE);
                recyclerViewDiscover.setVisibility(View.VISIBLE);

                StudySpotAdapter adapter = new StudySpotAdapter(requireContext(), places, favIds, new StudySpotAdapter.OnSpotActionListener() {
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
                });
                recyclerViewDiscover.setAdapter(adapter);
            }
        } else {
            List<Opportunity> opps = oppController.getOpportunities(query, category);
            List<Long> favIds = favRepo.getFavoriteItemIds(Favorite.FavoriteType.OPPORTUNITY);

            if (opps == null || opps.isEmpty()) {
                textEmptyDiscover.setVisibility(View.VISIBLE);
                recyclerViewDiscover.setVisibility(View.GONE);
            } else {
                textEmptyDiscover.setVisibility(View.GONE);
                recyclerViewDiscover.setVisibility(View.VISIBLE);

                OpportunityAdapter adapter = new OpportunityAdapter(requireContext(), opps, favIds, new OpportunityAdapter.OnOppActionListener() {
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
                });
                recyclerViewDiscover.setAdapter(adapter);
            }
        }
    }
}
