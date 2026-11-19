package com.unimate.dto;

import com.unimate.model.Event;
import com.unimate.model.Task;

import java.util.ArrayList;
import java.util.List;

public class PlanProposal {

    private List<Event> proposedEvents;
    private List<Task> unscheduledTasks;
    private String explanation;

    public PlanProposal() {
        this.proposedEvents = new ArrayList<>();
        this.unscheduledTasks = new ArrayList<>();
        this.explanation = "";
    }

    public PlanProposal(List<Event> proposedEvents, List<Task> unscheduledTasks, String explanation) {
        this.proposedEvents = proposedEvents;
        this.unscheduledTasks = unscheduledTasks;
        this.explanation = explanation;
    }

    public List<Event> getProposedEvents() {
        return proposedEvents;
    }

    public void setProposedEvents(List<Event> proposedEvents) {
        this.proposedEvents = proposedEvents;
    }

    public List<Task> getUnscheduledTasks() {
        return unscheduledTasks;
    }

    public void setUnscheduledTasks(List<Task> unscheduledTasks) {
        this.unscheduledTasks = unscheduledTasks;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
