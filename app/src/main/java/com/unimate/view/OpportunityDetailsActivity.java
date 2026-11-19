package com.unimate.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.unimate.R;
import com.unimate.controller.OpportunityController;
import com.unimate.model.Opportunity;
import com.unimate.model.Task;
import com.unimate.view.ui.UiKit;

public class OpportunityDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_OPP_ID = "extra_opp_id";

    private TextView textTitle, textOrg, textDeadline, textClosing, textDescription, textCategory, textLink;
    private View rowLink, dividerLink, bottomBar;
    private ImageButton btnFavorite;
    private MaterialButton btnOpenLink, btnAddDeadlineTask;

    private OpportunityController oppController;
    private long oppId = -1;
    private Opportunity opportunity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_details);
        UiKit.setupSecondaryScreen(this, R.id.toolbarOppDetails, "");

        oppController = new OpportunityController(this);

        initViews();

        oppId = getIntent().getLongExtra(EXTRA_OPP_ID, -1);
        if (oppId > 0) {
            loadOpportunityData();
        }
        if (opportunity == null) {
            android.widget.Toast.makeText(this, "Opportunity not found", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
    }

    private void initViews() {
        textTitle = findViewById(R.id.textOppDetailsTitle);
        textOrg = findViewById(R.id.textOppDetailsOrg);
        textDeadline = findViewById(R.id.textOppDetailsDeadline);
        textClosing = findViewById(R.id.textOppDetailsClosing);
        textDescription = findViewById(R.id.textOppDetailsDesc);
        textCategory = findViewById(R.id.textOppDetailsCategory);
        textLink = findViewById(R.id.textOppDetailsLink);
        rowLink = findViewById(R.id.rowOppLink);
        dividerLink = findViewById(R.id.dividerOppLink);
        btnFavorite = findViewById(R.id.btnOppDetailsFavorite);
        btnOpenLink = findViewById(R.id.btnOpenOppLink);
        btnAddDeadlineTask = findViewById(R.id.btnAddDeadlineAsTask);
        bottomBar = findViewById(R.id.bottomBarOppDetails);
        UiKit.padForSystemBars(bottomBar, false, true);
    }

    private void loadOpportunityData() {
        opportunity = oppController.getOpportunityById(oppId);
        if (opportunity == null) return;

        textTitle.setText(opportunity.getTitle());

        String org = opportunity.getOrganization();
        textOrg.setText(org != null && !org.trim().isEmpty() ? org : "University opportunity");
        UiKit.setLeadingIcon(textOrg, R.drawable.ic_school, R.color.text_tertiary, 16);
        textOrg.setCompoundDrawablePadding(UiKit.dp(this, 6));

        int hue = UiKit.opportunityColor(opportunity.getCategory());
        textCategory.setText(opportunity.getCategory() != null ? UiKit.label(opportunity.getCategory()) : "Event");
        UiKit.styleTag(textCategory, hue);

        String deadline = opportunity.getDeadline();
        boolean hasDeadline = deadline != null && !deadline.trim().isEmpty();
        textDeadline.setText(hasDeadline ? UiKit.prettyDate(deadline) : "Rolling — no fixed deadline");
        textClosing.setText(UiKit.relativeClosing(deadline));
        long days = UiKit.daysUntil(deadline);
        if (days < 0) UiKit.styleTag(textClosing, R.color.hue_gray);
        else if (days <= 7) UiKit.styleTag(textClosing, R.color.warning);
        else UiKit.styleTag(textClosing, R.color.success);
        textClosing.setVisibility(hasDeadline ? View.VISIBLE : View.GONE);

        // A closed opportunity can't usefully become a task.
        btnAddDeadlineTask.setEnabled(hasDeadline && days >= 0);

        String desc = opportunity.getDescription();
        textDescription.setText(desc != null && !desc.trim().isEmpty() ? desc : "No description provided.");

        String link = opportunity.getLink();
        boolean hasLink = link != null && !link.trim().isEmpty();
        rowLink.setVisibility(hasLink ? View.VISIBLE : View.GONE);
        dividerLink.setVisibility(hasLink ? View.VISIBLE : View.GONE);
        btnOpenLink.setVisibility(hasLink ? View.VISIBLE : View.GONE);
        if (hasLink) {
            Uri uri = Uri.parse(link.trim());
            textLink.setText(uri.getHost() != null ? uri.getHost().replaceFirst("^www\\.", "") : link);
        }

        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        boolean isFav = oppController.isFavorite(oppId);
        btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        btnFavorite.setImageTintList(UiKit.colorList(this, isFav ? R.color.favorite : R.color.text_secondary));
        btnFavorite.setContentDescription(isFav ? "Remove from favorites" : "Save to favorites");
    }

    private void setupListeners() {
        btnFavorite.setOnClickListener(v -> {
            boolean isNowFav = oppController.toggleFavorite(oppId);
            updateFavoriteIcon();
            showMessage(isNowFav ? "Saved to favorites" : "Removed from favorites");
        });

        View.OnClickListener openLink = v -> {
            if (opportunity == null || opportunity.getLink() == null) return;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(opportunity.getLink().trim())));
            } catch (Exception e) {
                showMessage("No app can open this link");
            }
        };
        btnOpenLink.setOnClickListener(openLink);
        rowLink.setOnClickListener(openLink);

        // Cross-module contract: turns the deadline into a high-priority task.
        btnAddDeadlineTask.setOnClickListener(v -> {
            if (opportunity == null) return;
            boolean created = oppController.addDeadlineAsTask(opportunity, Task.Priority.HIGH, 60);
            if (created) {
                btnAddDeadlineTask.setEnabled(false);
                btnAddDeadlineTask.setText("Added");
                btnAddDeadlineTask.setIconResource(R.drawable.ic_check);
                showMessage("Deadline added to your tasks");
            } else {
                showMessage("Couldn't create the task");
            }
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
