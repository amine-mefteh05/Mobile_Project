package com.unimate.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unimate.R;
import com.unimate.adapter.EventAdapter;
import com.unimate.controller.PlannerController;
import com.unimate.dto.PlanProposal;
import com.unimate.model.Event;

import java.util.List;

public class PlanProposalActivity extends AppCompatActivity {

    private TextView textExplanation, textUnscheduled;
    private RecyclerView recyclerViewProposals;
    private Button btnApprovePlan, btnCancelPlan;

    private PlannerController plannerController;
    private PlanProposal currentProposal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_proposal);

        plannerController = new PlannerController(this);

        Toolbar toolbar = findViewById(R.id.toolbarProposal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Smart Study Plan Proposal");
        }

        initViews();
        generateProposal();
    }

    private void initViews() {
        textExplanation = findViewById(R.id.textProposalExplanation);
        textUnscheduled = findViewById(R.id.textProposalUnscheduled);
        recyclerViewProposals = findViewById(R.id.recyclerViewProposals);
        btnApprovePlan = findViewById(R.id.btnApprovePlan);
        btnCancelPlan = findViewById(R.id.btnCancelPlan);

        recyclerViewProposals.setLayoutManager(new LinearLayoutManager(this));
    }

    private void generateProposal() {
        currentProposal = plannerController.proposeWeeklyPlan();
        if (currentProposal == null) return;

        textExplanation.setText(currentProposal.getExplanation());

        int unscheduledCount = currentProposal.getUnscheduledTasks() != null ? currentProposal.getUnscheduledTasks().size() : 0;
        textUnscheduled.setText("Unscheduled tasks due to tight schedule: " + unscheduledCount);

        List<Event> proposedEvents = currentProposal.getProposedEvents();
        EventAdapter adapter = new EventAdapter(this, proposedEvents, new EventAdapter.OnEventActionListener() {
            @Override
            public void onEventClick(Event event) {}

            @Override
            public void onEventDelete(Event event) {
                proposedEvents.remove(event);
                generateProposalListUI(proposedEvents);
            }
        });
        recyclerViewProposals.setAdapter(adapter);

        btnApprovePlan.setOnClickListener(v -> {
            int saved = plannerController.approveAndSavePlan(proposedEvents);
            Toast.makeText(this, "Saved " + saved + " study sessions to your Planner!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });

        btnCancelPlan.setOnClickListener(v -> finish());
    }

    private void generateProposalListUI(List<Event> events) {
        EventAdapter adapter = new EventAdapter(this, events, null);
        recyclerViewProposals.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
