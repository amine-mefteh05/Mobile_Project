package com.unimate.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.unimate.R;
import com.unimate.adapter.EventAdapter;
import com.unimate.controller.PlannerController;
import com.unimate.dto.PlanProposal;
import com.unimate.model.Event;
import com.unimate.view.ui.UiKit;

import java.util.ArrayList;
import java.util.List;

public class PlanProposalActivity extends AppCompatActivity {

    private TextView textHeadline, textExplanation, textUnscheduled, textSessionsHeader, textSessionsHint;
    private View cardUnscheduled, emptyProposals;
    private RecyclerView recyclerViewProposals;
    private MaterialButton btnApprovePlan, btnCancelPlan;

    private PlannerController plannerController;
    private EventAdapter adapter;
    private final List<Event> proposedEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_proposal);
        UiKit.setupSecondaryScreen(this, R.id.toolbarProposal, "Study plan");
        UiKit.padForSystemBars(findViewById(R.id.bottomBarProposal), false, true);

        plannerController = new PlannerController(this);

        initViews();
        generateProposal();
    }

    private void initViews() {
        textHeadline = findViewById(R.id.textProposalHeadline);
        textExplanation = findViewById(R.id.textProposalExplanation);
        textUnscheduled = findViewById(R.id.textProposalUnscheduled);
        textSessionsHeader = findViewById(R.id.textProposalSessionsHeader);
        textSessionsHint = findViewById(R.id.textProposalSessionsHint);
        cardUnscheduled = findViewById(R.id.cardProposalUnscheduled);
        emptyProposals = findViewById(R.id.emptyProposals);
        recyclerViewProposals = findViewById(R.id.recyclerViewProposals);
        btnApprovePlan = findViewById(R.id.btnApprovePlan);
        btnCancelPlan = findViewById(R.id.btnCancelPlan);

        recyclerViewProposals.setLayoutManager(new LinearLayoutManager(this));
        UiKit.addDividers(recyclerViewProposals);
        btnCancelPlan.setOnClickListener(v -> finish());
    }

    private void generateProposal() {
        PlanProposal proposal = plannerController.proposeWeeklyPlan();
        if (proposal != null && proposal.getProposedEvents() != null) {
            proposedEvents.addAll(proposal.getProposedEvents());
        }

        String explanation = proposal != null ? proposal.getExplanation() : null;
        textExplanation.setText(explanation != null && !explanation.trim().isEmpty()
                ? explanation
                : "Sessions are placed in your free time before each deadline, highest priority first.");
        textExplanation.setVisibility(View.VISIBLE);

        int unscheduled = proposal != null && proposal.getUnscheduledTasks() != null
                ? proposal.getUnscheduledTasks().size() : 0;
        if (unscheduled > 0) {
            textUnscheduled.setText(UiKit.plural(unscheduled, "task") + " didn't fit this week. Free up time or widen your study day in Profile.");
            cardUnscheduled.setVisibility(View.VISIBLE);
        } else {
            cardUnscheduled.setVisibility(View.GONE);
        }

        // One adapter for the screen's lifetime; removals only touch the local list (nothing saved yet).
        adapter = new EventAdapter(this, proposedEvents, new EventAdapter.OnEventActionListener() {
            @Override
            public void onEventClick(Event event) { }

            @Override
            public void onEventDelete(Event event) {
                int index = proposedEvents.indexOf(event);
                if (index < 0) return;
                proposedEvents.remove(index);
                adapter.notifyItemRemoved(index);
                // The engine's summary describes the original proposal; hide it once edited.
                textExplanation.setVisibility(View.GONE);
                bindCounts();
            }
        });
        adapter.setRemoveMode(true);
        recyclerViewProposals.setAdapter(adapter);

        btnApprovePlan.setOnClickListener(v -> {
            int saved = plannerController.approveAndSavePlan(proposedEvents);
            Toast.makeText(this, saved == 1 ? "1 session added to your planner"
                    : saved + " sessions added to your planner", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });

        bindCounts();
    }

    private void bindCounts() {
        int count = proposedEvents.size();
        boolean empty = count == 0;

        textHeadline.setText(empty ? "Nothing to schedule" : UiKit.plural(count, "study session") + " this week");
        btnApprovePlan.setEnabled(!empty);
        btnApprovePlan.setText(empty ? "Add to planner" : "Add " + UiKit.plural(count, "session"));

        textSessionsHeader.setVisibility(empty ? View.GONE : View.VISIBLE);
        textSessionsHint.setVisibility(empty ? View.GONE : View.VISIBLE);
        recyclerViewProposals.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            UiKit.bindEmptyState(emptyProposals, R.drawable.ic_planner, "No sessions to add",
                    "You're all caught up, or no pending task fits your free time this week.");
            emptyProposals.setVisibility(View.VISIBLE);
        } else {
            emptyProposals.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
