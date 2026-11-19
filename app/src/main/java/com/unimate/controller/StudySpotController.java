package com.unimate.controller;

import android.content.Context;
import android.util.Log;

import com.unimate.database.PreferencesManager;
import com.unimate.dto.ScoredPlace;
import com.unimate.dto.Weights;
import com.unimate.intelligence.StudySpotScorer;
import com.unimate.model.Favorite;
import com.unimate.model.StudyPlace;
import com.unimate.repository.FavoriteRepository;
import com.unimate.repository.StudySpotRepository;

import java.util.ArrayList;
import java.util.List;

public class StudySpotController {

    private static final String TAG = "UniMate_SpotCtrl";
    private final StudySpotRepository spotRepo;
    private final FavoriteRepository favRepo;
    private final PreferencesManager prefsManager;
    private final BudgetController budgetController;

    public StudySpotController(Context context) {
        Context appContext = context.getApplicationContext();
        this.spotRepo = new StudySpotRepository(appContext);
        this.favRepo = new FavoriteRepository(appContext);
        this.prefsManager = PreferencesManager.getInstance(appContext);
        this.budgetController = new BudgetController(appContext);
    }

    public List<ScoredPlace> getRankedPlaces(String query, String category) {
        try {
            List<StudyPlace> places = spotRepo.searchAndFilter(query, category);
            Weights weights = prefsManager.getWeights();
            boolean isTight = budgetController.isBudgetTight();

            return StudySpotScorer.rankPlaces(places, weights, isTight);
        } catch (Exception e) {
            Log.e(TAG, "Error ranking study places: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public StudyPlace getPlaceById(long id) {
        return spotRepo.getById(id);
    }

    public boolean toggleFavorite(long placeId) {
        boolean isFav = favRepo.isFavorite(Favorite.FavoriteType.PLACE, placeId);
        if (isFav) {
            return !favRepo.removeFavorite(Favorite.FavoriteType.PLACE, placeId);
        } else {
            return favRepo.addFavorite(Favorite.FavoriteType.PLACE, placeId);
        }
    }

    public boolean isFavorite(long placeId) {
        return favRepo.isFavorite(Favorite.FavoriteType.PLACE, placeId);
    }

    public List<StudyPlace> getFavoritePlaces() {
        List<Long> favIds = favRepo.getFavoriteItemIds(Favorite.FavoriteType.PLACE);
        List<StudyPlace> favorites = new ArrayList<>();
        for (long id : favIds) {
            StudyPlace place = spotRepo.getById(id);
            if (place != null) {
                favorites.add(place);
            }
        }
        return favorites;
    }

    public StudyPlace getTopRecommendedPlace() {
        List<ScoredPlace> ranked = getRankedPlaces(null, "All");
        if (ranked != null && !ranked.isEmpty()) {
            return ranked.get(0).getPlace();
        }
        return null;
    }
}
