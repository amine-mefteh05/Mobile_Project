package com.unimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.unimate.R;
import com.unimate.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onEventClick(Event event);
        void onEventDelete(Event event);
    }

    private final Context context;
    private List<Event> events;
    private final OnEventActionListener listener;

    public EventAdapter(Context context, List<Event> events, OnEventActionListener listener) {
        this.context = context;
        this.events = events != null ? events : new ArrayList<>();
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textTime;
        TextView textLocation;
        Chip chipType;
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textEventTitle);
            textTime = itemView.findViewById(R.id.textEventTime);
            textLocation = itemView.findViewById(R.id.textEventLocation);
            chipType = itemView.findViewById(R.id.chipEventType);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
        }

        public void bind(Event event) {
            textTitle.setText(event.getTitle());
            textTime.setText(event.getStartTime() + " - " + event.getEndTime());
            textLocation.setText(event.getLocation() != null ? event.getLocation() : "Campus");

            chipType.setText(event.getType() != null ? event.getType().name() : "EVENT");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEventClick(event);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onEventDelete(event);
            });
        }
    }
}
