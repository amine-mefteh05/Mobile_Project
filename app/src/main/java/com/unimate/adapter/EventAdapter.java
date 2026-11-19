package com.unimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unimate.R;
import com.unimate.model.Event;
import com.unimate.view.ui.UiKit;

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
    /** When true the trailing action reads "remove from list" (plan proposals) rather than "delete". */
    private boolean removeMode = false;

    public EventAdapter(Context context, List<Event> events, OnEventActionListener listener) {
        this.context = context;
        this.events = events != null ? events : new ArrayList<>();
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setRemoveMode(boolean removeMode) {
        this.removeMode = removeMode;
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
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        final TextView textTitle, textStart, textEnd, textLocation, textType;
        final ImageView iconLocation;
        final View accent;
        final ImageButton btnDelete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textEventTitle);
            textStart = itemView.findViewById(R.id.textEventStart);
            textEnd = itemView.findViewById(R.id.textEventEnd);
            textLocation = itemView.findViewById(R.id.textEventLocation);
            textType = itemView.findViewById(R.id.textEventType);
            iconLocation = itemView.findViewById(R.id.iconEventLocation);
            accent = itemView.findViewById(R.id.viewEventAccent);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
        }

        void bind(Event event) {
            textTitle.setText(event.getTitle());
            textStart.setText(event.getStartTime());
            textEnd.setText(event.getEndTime());

            boolean hasLocation = event.getLocation() != null && !event.getLocation().trim().isEmpty();
            if (removeMode) {
                // Plan proposals span the week, so the day matters more than the room.
                String day = UiKit.prettyDate(event.getDate());
                textLocation.setText(hasLocation ? day + " · " + event.getLocation() : day);
                textLocation.setVisibility(View.VISIBLE);
                iconLocation.setImageResource(R.drawable.ic_event);
                iconLocation.setVisibility(View.VISIBLE);
            } else {
                textLocation.setText(hasLocation ? event.getLocation() : "");
                textLocation.setVisibility(hasLocation ? View.VISIBLE : View.GONE);
                iconLocation.setImageResource(R.drawable.ic_location);
                iconLocation.setVisibility(hasLocation ? View.VISIBLE : View.GONE);
            }

            int hue = UiKit.eventTypeColor(event.getType());
            textType.setText(event.getType() != null ? UiKit.label(event.getType()) : "Event");
            UiKit.styleTag(textType, hue);
            accent.setBackgroundTintList(UiKit.colorList(context, hue));

            // Read-only lists (e.g. Home) pass no listener: hide actions that would do nothing.
            if (listener == null) {
                btnDelete.setVisibility(View.GONE);
                itemView.setClickable(false);
                itemView.setOnClickListener(null);
                return;
            }

            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setImageResource(removeMode ? R.drawable.ic_close : R.drawable.ic_delete);
            btnDelete.setContentDescription(removeMode
                    ? "Remove " + event.getTitle() + " from plan"
                    : "Delete " + event.getTitle());

            itemView.setClickable(true);
            itemView.setOnClickListener(v -> listener.onEventClick(event));
            btnDelete.setOnClickListener(v -> listener.onEventDelete(event));
        }
    }
}
