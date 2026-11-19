package com.unimate.view.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.unimate.R;
import com.unimate.model.Event;
import com.unimate.model.Expense;
import com.unimate.model.Opportunity;
import com.unimate.model.Task;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Shared presentation helpers for the UniMate design system:
 * edge-to-edge insets, human-readable labels, relative dates and category colors.
 * Contains no business logic.
 */
public final class UiKit {

    private UiKit() {}

    // ------------------------------------------------------------------ Window

    /** Draws the app behind transparent system bars and picks bar icon contrast from the theme. */
    public static void applyEdgeToEdge(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        boolean light = activity.getResources().getBoolean(R.bool.light_system_bars);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(light);
        controller.setAppearanceLightNavigationBars(light);
    }

    /**
     * Adds system-bar insets (and the keyboard, for the bottom edge) on top of the view's
     * own XML padding. Safe to call once per view.
     */
    public static void padForSystemBars(View view, boolean top, boolean bottom) {
        final int pl = view.getPaddingLeft();
        final int pt = view.getPaddingTop();
        final int pr = view.getPaddingRight();
        final int pb = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(bars.bottom, ime.bottom);
            v.setPadding(pl + bars.left,
                    pt + (top ? bars.top : 0),
                    pr + bars.right,
                    pb + (bottom ? bottomInset : 0));
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Standard secondary screen: edge-to-edge window, toolbar as the action bar with an up arrow,
     * and status-bar padding on the toolbar. Call right after setContentView.
     */
    public static Toolbar setupSecondaryScreen(AppCompatActivity activity, @IdRes int toolbarId,
                                               @Nullable CharSequence title) {
        return setupSecondaryScreen(activity, toolbarId, title, R.drawable.ic_arrow_back, "Back");
    }

    /** Variant for modal-style screens (forms) that close with an ✕ instead of a back arrow. */
    public static Toolbar setupSecondaryScreen(AppCompatActivity activity, @IdRes int toolbarId,
                                               @Nullable CharSequence title, @DrawableRes int navIcon,
                                               String navDescription) {
        applyEdgeToEdge(activity);
        Toolbar toolbar = activity.findViewById(toolbarId);
        activity.setSupportActionBar(toolbar);
        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeAsUpIndicator(navIcon);
            bar.setHomeActionContentDescription(navDescription);
            if (title != null) bar.setTitle(title);
        }
        padForSystemBars(toolbar, true, false);
        return toolbar;
    }

    // ------------------------------------------------------------------ Pickers

    /** Material date picker. Emits an ISO date (yyyy-MM-dd). */
    public static void pickDate(FragmentActivity activity, String title, @Nullable String currentIso,
                                Consumer<String> onPicked) {
        LocalDate initial = parse(currentIso);
        if (initial == null) initial = LocalDate.now();
        long selection = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(selection)
                .build();
        picker.addOnPositiveButtonClickListener(millis -> onPicked.accept(
                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()));
        picker.show(activity.getSupportFragmentManager(), "date_picker");
    }

    /** Material 24h time picker. Emits HH:mm. */
    public static void pickTime(FragmentActivity activity, String title, @Nullable String currentHhMm,
                                Consumer<String> onPicked) {
        LocalTime initial;
        try {
            initial = currentHhMm == null || currentHhMm.trim().isEmpty()
                    ? LocalTime.now().withMinute(0) : LocalTime.parse(currentHhMm.trim());
        } catch (Exception e) {
            initial = LocalTime.now().withMinute(0);
        }
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(initial.getHour())
                .setMinute(initial.getMinute())
                .setTitleText(title)
                .build();
        picker.addOnPositiveButtonClickListener(v -> onPicked.accept(
                String.format(Locale.US, "%02d:%02d", picker.getHour(), picker.getMinute())));
        picker.show(activity.getSupportFragmentManager(), "time_picker");
    }

    /** Hairline separators between list rows (rows are flat; there are no per-item cards). */
    public static void addDividers(androidx.recyclerview.widget.RecyclerView list) {
        Context c = list.getContext();
        com.google.android.material.divider.MaterialDividerItemDecoration divider =
                new com.google.android.material.divider.MaterialDividerItemDecoration(
                        c, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL);
        divider.setDividerColor(color(c, R.color.stroke));
        divider.setDividerThickness(Math.max(1, dp(c, 1)));
        divider.setLastItemDecorated(false);
        // Align hairlines with the content column, not the screen edge.
        divider.setDividerInsetStart(list.getPaddingStart());
        divider.setDividerInsetEnd(list.getPaddingEnd());
        list.addItemDecoration(divider);
    }

    /** Clears a field's error as soon as the user edits it. */
    public static void clearErrorOnEdit(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            if (layout == null || layout.getEditText() == null) continue;
            layout.getEditText().addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    if (layout.getError() != null) layout.setError(null);
                }
            });
        }
    }

    /** "42.50 TND" with locale grouping. */
    public static String money(double amount) {
        return String.format(Locale.getDefault(), "%.2f TND", amount);
    }

    public static int dp(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static int color(Context context, @ColorRes int res) {
        return ContextCompat.getColor(context, res);
    }

    public static ColorStateList colorList(Context context, @ColorRes int res) {
        return ColorStateList.valueOf(ContextCompat.getColor(context, res));
    }

    /** A translucent wash of a hue, for tag and icon-tile backgrounds. Works in light and dark. */
    public static ColorStateList softTint(Context context, @ColorRes int res) {
        return ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(ContextCompat.getColor(context, res), 0x24));
    }

    /** Applies a hue to a small tag: soft background, full-strength text. */
    public static void styleTag(TextView tag, @ColorRes int hue) {
        Context c = tag.getContext();
        tag.setBackgroundTintList(softTint(c, hue));
        tag.setTextColor(color(c, hue));
    }

    /** Puts a small tinted icon before a TextView's text (compound drawables cannot be sized in XML). */
    public static void setLeadingIcon(TextView view, @DrawableRes int icon, @ColorRes int tint, float sizeDp) {
        android.graphics.drawable.Drawable d = ContextCompat.getDrawable(view.getContext(), icon);
        if (d == null) return;
        d = androidx.core.graphics.drawable.DrawableCompat.wrap(d.mutate());
        androidx.core.graphics.drawable.DrawableCompat.setTint(d, color(view.getContext(), tint));
        int size = dp(view.getContext(), sizeDp);
        d.setBounds(0, 0, size, size);
        view.setCompoundDrawablesRelative(d, null, null, null);
        view.setCompoundDrawablePadding(dp(view.getContext(), 4));
    }

    // ------------------------------------------------------------------ Empty state

    public static void bindEmptyState(View root, @DrawableRes int icon, String title, String subtitle) {
        if (root == null) return;
        ImageView iconView = root.findViewById(R.id.emptyIcon);
        TextView titleView = root.findViewById(R.id.emptyTitle);
        TextView subtitleView = root.findViewById(R.id.emptySubtitle);
        if (iconView != null) iconView.setImageResource(icon);
        if (titleView != null) titleView.setText(title);
        if (subtitleView != null) {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(subtitle == null ? View.GONE : View.VISIBLE);
        }
    }

    // ------------------------------------------------------------------ Text

    /** "UNIVERSITY" -> "University", "Café" stays "Café". */
    public static String label(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String lower = raw.toLowerCase(Locale.getDefault()).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public static String label(Enum<?> value) {
        return value == null ? "" : label(value.name());
    }

    public static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    public static String firstName(String name) {
        if (name == null || name.trim().isEmpty()) return "there";
        return name.trim().split("\\s+")[0];
    }

    public static String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 5) return "Good evening";
        if (hour < 12) return "Good morning";
        if (hour < 18) return "Good afternoon";
        return "Good evening";
    }

    // ------------------------------------------------------------------ Dates

    private static LocalDate parse(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(isoDate.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /** "Due today", "Due tomorrow", "Due in 4 days", "Overdue by 2 days", "Due 3 Dec". */
    public static String relativeDue(String isoDate) {
        LocalDate date = parse(isoDate);
        if (date == null) return "No deadline";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (days < 0) return "Overdue by " + plural(-days, "day");
        if (days == 0) return "Due today";
        if (days == 1) return "Due tomorrow";
        if (days < 7) return "Due in " + plural(days, "day");
        return "Due " + date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()));
    }

    /** For opportunities: "Closes today", "Closes in 5 days", "Closed". */
    public static String relativeClosing(String isoDate) {
        LocalDate date = parse(isoDate);
        if (date == null) return "Open application";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (days < 0) return "Closed";
        if (days == 0) return "Closes today";
        if (days == 1) return "Closes tomorrow";
        if (days < 30) return "Closes in " + plural(days, "day");
        return "Closes " + date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()));
    }

    /** Days until the date, or Long.MAX_VALUE when unknown. */
    public static long daysUntil(String isoDate) {
        LocalDate date = parse(isoDate);
        return date == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public static String prettyDate(String isoDate) {
        LocalDate date = parse(isoDate);
        if (date == null) return isoDate == null ? "" : isoDate;
        return date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()));
    }

    public static String plural(long n, String unit) {
        return n + " " + unit + (n == 1 ? "" : "s");
    }

    // ------------------------------------------------------------------ Category colors

    @ColorRes
    public static int eventTypeColor(Event.EventType type) {
        if (type == null) return R.color.hue_gray;
        switch (type) {
            case LECTURE: return R.color.hue_blue;
            case PRACTICAL: return R.color.hue_teal;
            case EXAM: return R.color.hue_rose;
            case STUDY: return R.color.brand;
            default: return R.color.hue_amber;
        }
    }

    @ColorRes
    public static int priorityColor(Task.Priority priority) {
        if (priority == Task.Priority.HIGH) return R.color.danger;
        if (priority == Task.Priority.LOW) return R.color.success;
        return R.color.warning;
    }

    @ColorRes
    public static int opportunityColor(Opportunity.Category category) {
        if (category == null) return R.color.hue_gray;
        switch (category) {
            case HACKATHON: return R.color.hue_violet;
            case INTERNSHIP: return R.color.hue_blue;
            case SCHOLARSHIP: return R.color.hue_teal;
            case WORKSHOP: return R.color.hue_amber;
            case RESOURCE: return R.color.hue_gray;
            default: return R.color.hue_rose;
        }
    }

    @DrawableRes
    public static int expenseIcon(Expense.Category category) {
        if (category == null) return R.drawable.ic_more;
        switch (category) {
            case FOOD: return R.drawable.ic_food;
            case TRANSPORT: return R.drawable.ic_bus;
            case STUDY: return R.drawable.ic_book;
            default: return R.drawable.ic_more;
        }
    }

    @ColorRes
    public static int expenseColor(Expense.Category category) {
        if (category == null) return R.color.hue_gray;
        switch (category) {
            case FOOD: return R.color.hue_amber;
            case TRANSPORT: return R.color.hue_blue;
            case STUDY: return R.color.hue_violet;
            default: return R.color.hue_gray;
        }
    }

    public static String noiseLabel(int noise) {
        switch (noise) {
            case 1: return "Very quiet";
            case 2: return "Quiet";
            case 3: return "Moderate";
            case 4: return "Busy";
            default: return "Loud";
        }
    }

    public static String priceLabel(int level) {
        switch (level) {
            case 1: return "Free";
            case 2: return "Affordable";
            default: return "Pricey";
        }
    }
}
