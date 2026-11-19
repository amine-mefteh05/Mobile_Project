package com.unimate.controller;

import android.content.Context;
import android.util.Log;

import com.unimate.model.Favorite;
import com.unimate.model.Opportunity;
import com.unimate.model.Task;
import com.unimate.repository.FavoriteRepository;
import com.unimate.repository.OpportunityRepository;
import com.unimate.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class OpportunityController {

    private static final String TAG = "UniMate_OppCtrl";
    private final OpportunityRepository oppRepo;
    private final FavoriteRepository favRepo;
    private final TaskRepository taskRepo;

    public OpportunityController(Context context) {
        Context appContext = context.getApplicationContext();
        this.oppRepo = new OpportunityRepository(appContext);
        this.favRepo = new FavoriteRepository(appContext);
        this.taskRepo = new TaskRepository(appContext);
    }

    public List<Opportunity> getOpportunities(String query, String category) {
        try {
            return oppRepo.searchAndFilter(query, category);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching opportunities: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Opportunity getOpportunityById(long id) {
        return oppRepo.getById(id);
    }

    public boolean toggleFavorite(long oppId) {
        boolean isFav = favRepo.isFavorite(Favorite.FavoriteType.OPPORTUNITY, oppId);
        if (isFav) {
            return !favRepo.removeFavorite(Favorite.FavoriteType.OPPORTUNITY, oppId);
        } else {
            return favRepo.addFavorite(Favorite.FavoriteType.OPPORTUNITY, oppId);
        }
    }

    public boolean isFavorite(long oppId) {
        return favRepo.isFavorite(Favorite.FavoriteType.OPPORTUNITY, oppId);
    }

    /**
     * Connected Module Call (Figure 18 from Conception Dossier):
     * Converts an Opportunity deadline into a Task using TaskRepository contract.
     * Wrapped in try-catch for defensive integration.
     */
    public boolean addDeadlineAsTask(Opportunity opportunity, Task.Priority priority, int durationMinutes) {
        if (opportunity == null) return false;
        try {
            Task task = new Task();
            task.setStudentId(1);
            task.setTitle("Apply: " + opportunity.getTitle());
            task.setDescription("Opportunity Application (" + (opportunity.getOrganization() != null ? opportunity.getOrganization() : "Org") + ")\n" +
                    (opportunity.getLink() != null ? opportunity.getLink() : ""));
            task.setCategory(Task.Category.UNIVERSITY);
            task.setPriority(priority != null ? priority : Task.Priority.HIGH);
            task.setDeadline(opportunity.getDeadline());
            task.setDuration(durationMinutes > 0 ? durationMinutes : 60);
            task.setStatus(Task.Status.PENDING);

            long taskId = taskRepo.insert(task);
            Log.i(TAG, "Created task " + taskId + " from opportunity " + opportunity.getId());
            return taskId > 0;
        } catch (Exception e) {
            Log.e(TAG, "Defensive integration: failed to add deadline as task: " + e.getMessage(), e);
            return false;
        }
    }
}
