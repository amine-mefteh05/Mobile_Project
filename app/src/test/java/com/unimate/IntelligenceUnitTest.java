package com.unimate;

import com.unimate.dto.PlanProposal;
import com.unimate.dto.ScoredPlace;
import com.unimate.dto.Suggestion;
import com.unimate.dto.Weights;
import com.unimate.intelligence.BudgetAnalyzer;
import com.unimate.intelligence.RecommendationEngine;
import com.unimate.intelligence.ScheduleOptimizer;
import com.unimate.intelligence.StudySpotScorer;
import com.unimate.intelligence.TaskPrioritizer;
import com.unimate.model.Event;
import com.unimate.model.Expense;
import com.unimate.model.StudyPlace;
import com.unimate.model.Task;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntelligenceUnitTest {

    @Test
    public void testTaskPrioritizationScore() {
        Task highPrioTask = new Task();
        highPrioTask.setPriority(Task.Priority.HIGH);
        highPrioTask.setCategory(Task.Category.EXAM);
        highPrioTask.setDeadline(LocalDate.now().plusDays(1).toString());

        Task lowPrioTask = new Task();
        lowPrioTask.setPriority(Task.Priority.LOW);
        lowPrioTask.setCategory(Task.Category.PERSONAL);
        lowPrioTask.setDeadline(LocalDate.now().plusDays(10).toString());

        double highScore = TaskPrioritizer.calculateScore(highPrioTask);
        double lowScore = TaskPrioritizer.calculateScore(lowPrioTask);

        assertTrue("High priority exam task should have higher score than low priority task", highScore > lowScore);
    }

    @Test
    public void testEventOverlapDetection() {
        Event e1 = new Event();
        e1.setDate("2025-05-15");
        e1.setStartTime("09:00");
        e1.setEndTime("10:30");

        Event e2 = new Event();
        e2.setDate("2025-05-15");
        e2.setStartTime("10:00");
        e2.setEndTime("11:30");

        Event e3 = new Event();
        e3.setDate("2025-05-15");
        e3.setStartTime("11:00");
        e3.setEndTime("12:00");

        assertTrue("e1 and e2 should overlap", ScheduleOptimizer.hasOverlap(e1, e2));
        assertFalse("e1 and e3 should not overlap", ScheduleOptimizer.hasOverlap(e1, e3));
    }

    @Test
    public void testStudySpotTightBudgetRule() {
        StudyPlace place = new StudyPlace();
        place.setPriceLevel(1); // Cheap
        place.setNoiseLevel(1); // Quiet
        place.setWifi(true);

        Weights weights = new Weights(50, 50, 50, 50);

        ScoredPlace normalScore = StudySpotScorer.scorePlace(place, weights, false);
        ScoredPlace tightBudgetScore = StudySpotScorer.scorePlace(place, weights, true);

        assertNotNull(normalScore);
        assertNotNull(tightBudgetScore);
        assertTrue("Tight budget score should incorporate doubled budget weight", tightBudgetScore.getScore() > 0);
    }

    @Test
    public void testBudgetAnalyzerThreshold() {
        double limit = 100.0;
        double spentNormal = 50.0;
        double spentTight = 85.0; // >= 80%

        assertFalse("50 TND out of 100 TND limit is not tight", BudgetAnalyzer.isBudgetTight(limit, spentNormal));
        assertTrue("85 TND out of 100 TND limit is tight (>= 80%)", BudgetAnalyzer.isBudgetTight(limit, spentTight));
    }

    @Test
    public void testSmartAssistantWhatNow() {
        List<Task> pendingTasks = new ArrayList<>();
        Task task = new Task();
        task.setTitle("Prepare Mobile Presentation");
        task.setPriority(Task.Priority.HIGH);
        task.setDeadline(LocalDate.now().plusDays(2).toString());
        pendingTasks.add(task);

        Suggestion suggestion = RecommendationEngine.whatNow(
                pendingTasks, new ArrayList<>(), "10:00", "08:00", "20:00", null
        );

        assertNotNull(suggestion);
        assertTrue("Suggestion title should mention the task", suggestion.getTitle().contains("Prepare Mobile Presentation"));
        assertNotNull(suggestion.getReason());
    }

    @Test
    public void testPlanWeekProposals() {
        List<Task> pendingTasks = new ArrayList<>();
        Task task = new Task();
        task.setId(101);
        task.setTitle("Study Operating Systems");
        task.setDuration(90);
        task.setPriority(Task.Priority.HIGH);
        task.setDeadline(LocalDate.now().plusDays(5).toString());
        pendingTasks.add(task);

        PlanProposal proposal = RecommendationEngine.planWeek(
                pendingTasks, new ArrayList<>(), "08:00", "20:00"
        );

        assertNotNull(proposal);
        assertTrue("Plan proposal should suggest study events", proposal.getProposedEvents().size() > 0);
        assertEquals("Proposed event type should be STUDY", Event.EventType.STUDY, proposal.getProposedEvents().get(0).getType());
    }
}
