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
import com.unimate.controller.OpportunityController;
import com.unimate.model.Opportunity;
import com.unimate.model.Task;

public class OpportunityDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_OPP_ID = "extra_opp_id";

    private TextView textTitle, textOrg, textDeadline, textDescription;
    private Chip chipCategory;
    private ImageButton btnFavorite;
    private Button btnOpenLink, btnAddDeadlineTask;

    private OpportunityController oppController;
    private long oppId = -1;
    private Opportunity opportunity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_details);

        oppController = new OpportunityController(this);

        Toolbar toolbar = findViewById(R.id.toolbarOppDetails);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        oppId = getIntent().getLongExtra(EXTRA_OPP_ID, -1);
        if (oppId > 0) {
            loadOpportunityData();
        } else {
            Toast.makeText(this, "Opportunity not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void initViews() {
        textTitle = findViewById(R.id.textOppDetailsTitle);
        textOrg = findViewById(R.id.textOppDetailsOrg);
        textDeadline = findViewById(R.id.textOppDetailsDeadline);
        textDescription = findViewById(R.id.textOppDetailsDesc);
        chipCategory = findViewById(R.id.chipOppDetailsCategory);
        btnFavorite = findViewById(R.id.btnOppDetailsFavorite);
        btnOpenLink = findViewById(R.id.btnOpenOppLink);
        btnAddDeadlineTask = findViewById(R.id.btnAddDeadlineAsTask);
    }

    private void loadOpportunityData() {
        opportunity = oppController.getOpportunityById(oppId);
        if (opportunity == null) return;

        setTitle(opportunity.getTitle());
        textTitle.setText(opportunity.getTitle());
        textOrg.setText(opportunity.getOrganization() != null ? opportunity.getOrganization() : "University Opportunity");
        textDeadline.setText("Deadline: " + (opportunity.getDeadline() != null ? opportunity.getDeadline() : "Open"));
        textDescription.setText(opportunity.getDescription());
        chipCategory.setText(opportunity.getCategory() != null ? opportunity.getCategory().name() : "EVENT");

        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        boolean isFav = oppController.isFavorite(oppId);
        btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    private void setupListeners() {
        btnFavorite.setOnClickListener(v -> {
            boolean isNowFav = oppController.toggleFavorite(oppId);
            Toast.makeText(this, isNowFav ? "Added to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
            updateFavoriteIcon();
        });

        btnOpenLink.setOnClickListener(v -> {
            if (opportunity != null && opportunity.getLink() != null) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(opportunity.getLink()));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add deadline as task (Cross-Module contract call!)
        btnAddDeadlineTask.setOnClickListener(v -> {
            if (opportunity != null) {
                boolean created = oppController.addDeadlineAsTask(opportunity, Task.Priority.HIGH, 60);
                if (created) {
                    Toast.makeText(this, "Deadline added to your Tasks!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to create task", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
