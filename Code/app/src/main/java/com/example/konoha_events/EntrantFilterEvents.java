package com.example.konoha_events;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.EventModel;
import models.OnWaitingListModel;
import services.FirebaseService;
/**
 * Allows an entrant to filter events by keyword and/or registration deadline.
 * Displays the filtered results in a list.
 */
public class EntrantFilterEvents extends AppCompatActivity {
    private RecyclerView recyclerFilter;
    private EventsAdapter adapter;

    private final ArrayList<EventModel> allEvents = new ArrayList<>();
    private String currentKeyword = null;
    private Date currentDateFilter = null;

    private final SimpleDateFormat dialogDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtered_events);

        recyclerFilter = findViewById(R.id.recyclerFilter);
        recyclerFilter.setLayoutManager(new LinearLayoutManager(this));
        //Use existing events adapter
        adapter = new EventsAdapter(new EventsAdapter.Callback() {
            /**
             * Called when an event row is tapped.
             * Performs waitlist checks and shows alerts if needed.
             *
             * @param event The event the user clicked.
             */
            @Override
            public void onRowClick(EventModel event) {
                String userId = FirebaseService.firebaseService.getCurrentUserId();

                OnWaitingListModel existing = FirebaseService.firebaseService
                        .getExistingWaitlistEntry(event.getId(), userId);

                //Check if the user has already joined the waiting list, has accepted
                //declined or is selected for the event.
                if (existing != null && existing.getStatus() != null) {

                    String status = existing.getStatus().name();

                    new androidx.appcompat.app.AlertDialog.Builder(EntrantFilterEvents.this)
                            .setTitle("Hold on!")
                            .setMessage("You are already " + status + " for this event.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                //Check if the registration deadline has already passed
                Date deadline = event.getRegistrationDeadline();
                Date now = new Date();

                if (deadline != null && deadline.before(now)) {
                    new androidx.appcompat.app.AlertDialog.Builder(EntrantFilterEvents.this)
                            .setTitle("Registration Closed")
                            .setMessage("The registration deadline for this event has already passed.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                new androidx.appcompat.app.AlertDialog.Builder(EntrantFilterEvents.this)
                        .setTitle("Join waiting list?")
                        .setPositiveButton("Join", (d, w) -> {
                            FirebaseService.firebaseService.joinWaitingList(event.getId(), userId);
                            Toast.makeText(
                                    EntrantFilterEvents.this,
                                    "Joined waiting list!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }


            @Override
            public void onQrClick(EventModel event) {
            }
        });
        recyclerFilter.setAdapter(adapter);

        FirebaseService.firebaseService.getEventsLiveData().observe(this, list -> {
            allEvents.clear();
            if (list != null) allEvents.addAll(list);
            applyFiltersAndShow(); //start with no filters
        });

        ImageButton back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> finish());

        showFilterDialog();
    }
    //Show the filter dialog for optional keywords and registration deadline
    /**
     * Opens a dialog where the user can choose a keyword and/or deadline filter.
     */
    private void showFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.filter_events_dialog, null);

        EditText editKeyword = view.findViewById(R.id.editKeyword);
        TextView textDate = view.findViewById(R.id.textDate);

        //Prefill
        if (currentKeyword != null) editKeyword.setText(currentKeyword);
        if (currentDateFilter != null)
            textDate.setText(dialogDateFormat.format(currentDateFilter));

        //Open date picker
        final Date[] selectedDate = new Date[1];
        selectedDate[0] = currentDateFilter;

        textDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedDate[0] != null) cal.setTime(selectedDate[0]);

            new DatePickerDialog(
                    this,
                    (dp, y, m, d) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(y, m, d, 0, 0, 0);
                        chosen.set(Calendar.MILLISECOND, 0);
                        selectedDate[0] = chosen.getTime();
                        textDate.setText(dialogDateFormat.format(selectedDate[0]));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Filter events")
                .setView(view)
                .setPositiveButton("Apply", (dialog, which) -> {
                    currentKeyword = editKeyword.getText().toString().trim();
                    currentKeyword = currentKeyword.isEmpty() ? null : currentKeyword;
                    currentDateFilter = selectedDate[0];
                    applyFiltersAndShow();
                })
                .setNegativeButton("Clear filters", (dialog, which) -> {
                    currentKeyword = null;
                    currentDateFilter = null;
                    applyFiltersAndShow();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
    /**
     * Applies the selected filters and updates the event list.
     */
    private void applyFiltersAndShow() {
        List<EventModel> filtered = new ArrayList<>();

        for (EventModel e : allEvents) {
            if (e == null) continue;
            if (!matchesKeyword(e)) continue;
            if (!matchesDate(e)) continue;
            filtered.add(e);
        }

        adapter.submitList(filtered);
    }
    /**
     * Checks whether an event matches the current keyword filter.
     *
     * @param e The event to test.
     * @return true if it matches or no keyword filter is applied.
     */
    private boolean matchesKeyword(EventModel e) {
        if (currentKeyword == null) return true;

        String kw = currentKeyword.toLowerCase(Locale.getDefault());
        String title = (e.getEventTitle() == null ? "" : e.getEventTitle()).toLowerCase();
        String desc  = (e.getDescription()  == null ? "" : e.getDescription()).toLowerCase();

        return title.contains(kw) || desc.contains(kw);
    }
    /**
     * Checks whether an event matches the current date filter.
     *
     * @param e The event to test.
     * @return true if the event's registration deadline is on or after the selected date.
     */
    private boolean matchesDate(EventModel e) {
        if (currentDateFilter == null) return true;
        if (e.getRegistrationDeadline() == null) return false;

        return !e.getRegistrationDeadline().before(currentDateFilter);
    }
}