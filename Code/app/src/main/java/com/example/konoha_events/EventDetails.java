package com.example.konoha_events;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;

import constants.DatabaseConstants;
import constants.IntentConstants;
import interfaces.OnWaitingListArrayListCallback;
import lombok.val;
import models.EventModel;
import models.OnWaitingListModel;
import services.FirebaseService;
import util.ModelUtil;
import util.ViewUtil;

import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import interfaces.UserModelArrayListCallback;

/**
 * Activity displaying details about events. This view is used by organizers and adminstrators.
 * Requires two intent constants to be filled to function correctly:
 *   - IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE: The Activity that the back button should return
 *     to. This is needed to know what view to go back to since it's used in various places.
 *   - IntentConstants.INTENT_VIEW_EVENT_EVENT_ID: The eventId of the event to display details for
 * Export final list of enrolled entrants in CSV format
 */
public class EventDetails extends AppCompatActivity {
    private final String tag = "[EventDetails]";
    private FirebaseService fbs;
    private TextView eventIdTextView;
    private TextView eventDescriptionTextView;
    private TextView isGeolocationEnabledTextView;
    private TextView deadlineTextView;
    private TextView totalOnListTextView;
    private TextView waitingTextView;
    private TextView selectedTextView;
    private TextView acceptedTextView;
    private TextView declinedTextView;
    private TextView cancelledTextView;
    private Button viewEntrantsButton;
    private Button drawFromWaitlistButton;
    private Button uploadEventPosterButton;
    private Button deleteEventPosterButton;
    private Button showQRCodeButton;
    private Button showSendNotificationMenuButton;
    private NumberPicker numberPicker;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView posterImageView;
    private String eventId;
    private EventModel eventModel;
    private Button exportListOfEntrantsButton;

    public enum SEND_NOTIFICATION_OPTIONS {
        EVERYONE(null),
        WAITING(DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING),
        SELECTED(DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED),
        ACCEPTED(DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED),
        DECLINED(DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED),
        CANCELLED(DatabaseConstants.ON_WAITING_LIST_STATUS.CANCELLED);

        private final DatabaseConstants.ON_WAITING_LIST_STATUS mappedStatus;

        SEND_NOTIFICATION_OPTIONS(DatabaseConstants.ON_WAITING_LIST_STATUS status) {
            this.mappedStatus = status;
        }

        public DatabaseConstants.ON_WAITING_LIST_STATUS toWaitingListStatus() {
            return mappedStatus;
        }

        @NonNull
        @Override
        public String toString() {
            String lower = name().toLowerCase();
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_event_toolbar);

        String returnActivityName = getIntent().getStringExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE);
        eventId = getIntent().getStringExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID);
        Class<?> returnActivityClass = null;
        if (returnActivityName == null) {
            Log.e(tag, "EventDetails activity started with null returnActivityName intent");
            return;
        } else if (eventId == null) {
            Log.e(tag, "EventDetails activity started with null eventId intent");
            return;
        }

        try {
            returnActivityClass = Class.forName(returnActivityName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        eventIdTextView = findViewById(R.id.activity_event_view_id);
        eventDescriptionTextView = findViewById(R.id.activity_event_view_description);
        isGeolocationEnabledTextView = findViewById(R.id.activity_event_view_is_geolocation_enabled_text);
        deadlineTextView = findViewById(R.id.activity_event_view_deadline_text);
        totalOnListTextView = findViewById(R.id.activity_event_view_total_on_list_text);
        waitingTextView = findViewById(R.id.activity_event_view_waiting_text);
        selectedTextView = findViewById(R.id.activity_event_view_selected_text);
        acceptedTextView = findViewById(R.id.activity_event_view_accepted_text);
        declinedTextView = findViewById(R.id.activity_event_view_declined_text);
        cancelledTextView = findViewById(R.id.activity_event_view_cancelled_text);
        viewEntrantsButton = findViewById(R.id.activity_event_view_entrants_button);
        drawFromWaitlistButton = findViewById(R.id.activity_event_view_draw_from_waitlist_button);
        numberPicker = findViewById(R.id.activity_event_view_number_picker);
        uploadEventPosterButton = findViewById(R.id.activity_event_view_upload_event_poster);
        deleteEventPosterButton = findViewById(R.id.activity_event_view_delete_event_poster);
        posterImageView = findViewById(R.id.activity_event_view_poster_image_view);
        showQRCodeButton = findViewById(R.id.activity_event_view_show_qr_code_button);
        showSendNotificationMenuButton = findViewById(R.id.activity_event_view_start_send_notification_menu_button);
        exportListOfEntrantsButton = findViewById(R.id.export_csv);

        DocumentReference eventDocument = fbs.getEventDocumentReference(eventId);
        Class<?> finalReturnActivityClass = returnActivityClass;
        eventDocument
                .get()
                .addOnSuccessListener((v) -> {
                    Log.i(tag, String.format("Got event model of event %s successfully", eventId));
                    eventModel = ModelUtil.toEventModel(v);
                    ViewUtil.setupToolbarWithBackButtonToActivity(this, toolbar, eventModel.getEventTitle(), (Class<? extends Activity>) finalReturnActivityClass);
                    if (eventModel.getRegistrationDeadline() != null) {
                        displayDeadline(eventModel.getRegistrationDeadline());
                    }

                    eventIdTextView.setText(String.format("Event ID: %s", eventModel.getId()));
                    eventDescriptionTextView.setText(String.format("Description: %s", eventModel.getDescription()));

                    // Geolocation isn't actually in the model
                    isGeolocationEnabledTextView.setText("Geolocation Enabled: No");

                    drawFromWaitlistButton.setOnClickListener(vv -> {
                        fbs.selectUsersForEvent(eventModel.getId(), numberPicker.getValue());
                    });
                    Glide.with(this)
                            .load(eventModel.getImageBitmap())
                            .into(posterImageView);
                })
                .addOnFailureListener((e) -> Log.i(tag,
                        String.format("Didn't find or doesn't exist event %s", eventId)));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(0);
        numberPicker.setValue(0);

        fbs.getOnWaitingListsOfEvent(eventId, new OnWaitingListArrayListCallback() {
            @Override
            public void onCompleted(ArrayList<OnWaitingListModel> onWaitingListModels) {
                Log.i(tag, "Successfully got on waiting list models for event ");
                int waitingCount = 0;
                int selectedCount = 0;
                int acceptedCount = 0;
                int declinedCount = 0;
                int cancelledCount = 0;
                int total = onWaitingListModels.size();
                for (OnWaitingListModel owm : onWaitingListModels) {
                    switch (owm.getStatus()) {
                        case WAITING:
                            waitingCount++;
                            break;
                        case SELECTED:
                            selectedCount++;
                            break;
                        case ACCEPTED:
                            acceptedCount++;
                            break;
                        case DECLINED:
                            declinedCount++;
                            break;
                        case CANCELLED:
                            cancelledCount++;
                            break;
                        default:
                            Log.e(tag, "Unknown status in on waiting list model: " + owm.getStatus());
                            break;
                    }
                }

                displayTotals(total);
                displayWaiting(waitingCount);
                displaySelected(selectedCount);
                displayAccepted(acceptedCount);
                displayDeclined(declinedCount);
                displayCancelled(cancelledCount);

                numberPicker.setMinValue(0);
                numberPicker.setMaxValue(waitingCount);
                numberPicker.setValue(0);
                numberPicker.setWrapSelectorWheel(true);
            }
        });

        viewEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventEntrantDetails.class);
            intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID, eventId);
            intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE, returnActivityName);
            startActivity(intent);
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            fbs.updateEventImage(eventId, selectedImageUri, getContentResolver());
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .into(posterImageView);
                        }
                    }
                }
        );

        uploadEventPosterButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        deleteEventPosterButton.setOnClickListener(v -> {
            fbs.deleteEventImage(eventId);
        });

        showQRCodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventQRCodeActivity.class);
            intent.putExtra(IntentConstants.INTENT_EVENT_ID, eventId);
            startActivity(intent);
        });

        showSendNotificationMenuButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.organizer_send_notification_dialog, null);
            Spinner spinner = dialogView.findViewById(R.id.organizer_send_notification_event_spinner);
            EditText editText = dialogView.findViewById(R.id.organizer_send_notification_edit_text);

            SEND_NOTIFICATION_OPTIONS[] notificationOptions = SEND_NOTIFICATION_OPTIONS.values();
            ArrayAdapter<SEND_NOTIFICATION_OPTIONS> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    notificationOptions
            );
            spinner.setAdapter(adapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Send Notification");
            builder.setView(dialogView);

            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SEND_NOTIFICATION_OPTIONS chosenOption = (SEND_NOTIFICATION_OPTIONS) spinner.getSelectedItem();
                    String enteredText = editText.getText().toString();

                    if (chosenOption == SEND_NOTIFICATION_OPTIONS.EVERYONE) {
                        fbs.createNotificationForAllUsersOfEvent(
                                eventId,
                                enteredText,
                                DatabaseConstants.NOTIFICATION_TYPE.INFO
                        );
                    } else {
                        fbs.createNotificationForUsersOfStatusOfEvent(
                                eventId,
                                chosenOption.toWaitingListStatus(),
                                enteredText,
                                DatabaseConstants.NOTIFICATION_TYPE.INFO
                        );
                    }
                }
            });

            builder.setNegativeButton("Cancel", null);

            builder.create().show();
        });

        exportListOfEntrantsButton.setOnClickListener(v -> exportAllEntrantsToCSV());
    }

    private void displayDeadline(Date deadline) {
        deadlineTextView.setText(
                String.format("Registration Deadline: %s", deadline.toString()));
    }

    private void displayTotals(int total) {
        totalOnListTextView.setText(
                String.format("Total: %s", total));
    }

    private void displayWaiting(int waiting) {
        waitingTextView.setText(
                String.format("Waiting: %s", waiting));
    }

    private void displaySelected(int selected) {
        selectedTextView.setText(
                String.format("Selected: %s", selected));
    }

    private void displayAccepted(int accepted) {
        acceptedTextView.setText(
                String.format("Accepted: %s", accepted));
    }

    private void displayDeclined(int declined) {
        declinedTextView.setText(
                String.format("Declined: %s", declined));
    }

    private void displayCancelled(int cancelled) {
        cancelledTextView.setText(
                String.format("Cancelled: %s", cancelled));
    }

    /**
     * Export all entrants to a CSV file with their status
     */
    private void exportAllEntrantsToCSV() {
        if (eventId == null) {
            android.widget.Toast.makeText(this, "Event ID not loaded", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        android.widget.Toast.makeText(this, "Generating CSV file...", android.widget.Toast.LENGTH_SHORT).show();

        fbs.getOnWaitingListsOfEvent(eventId, new OnWaitingListArrayListCallback() {
            @Override
            public void onCompleted(ArrayList<OnWaitingListModel> onWaitingListModels) {
                if (onWaitingListModels.isEmpty()) {
                    android.widget.Toast.makeText(EventDetails.this,
                            "No entrants to export",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchUsersAndCreateCSV(onWaitingListModels);
            }
        });
    }

    private void fetchUsersAndCreateCSV(ArrayList<OnWaitingListModel> onWaitingListModels) {
        ArrayList<EntrantWithStatus> entrantsWithStatus = new ArrayList<>();
        final int totalEntrants = onWaitingListModels.size();
        final int[] fetchedCount = {0};

        for (OnWaitingListModel waitingListEntry : onWaitingListModels) {
            String userId = waitingListEntry.getUserId();
            DatabaseConstants.ON_WAITING_LIST_STATUS status = waitingListEntry.getStatus();

            fbs.getUserDocumentReference(userId)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            models.UserModel user = ModelUtil.toUserModel(userSnapshot);
                            entrantsWithStatus.add(new EntrantWithStatus(user, status));
                        }

                        fetchedCount[0]++;

                        if (fetchedCount[0] == totalEntrants) {
                            File csvFile = createCSVFile(entrantsWithStatus);
                            if (csvFile != null) {
                                shareCSVFile(csvFile, entrantsWithStatus.size());
                            } else {
                                android.widget.Toast.makeText(EventDetails.this,
                                        "Failed to create CSV file",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(tag, "Failed to fetch user: " + userId, e);
                        fetchedCount[0]++;

                        if (fetchedCount[0] == totalEntrants) {
                            File csvFile = createCSVFile(entrantsWithStatus);
                            if (csvFile != null) {
                                shareCSVFile(csvFile, entrantsWithStatus.size());
                            }
                        }
                    });
        }
    }

    private File createCSVFile(ArrayList<EntrantWithStatus> entrantsWithStatus) {
        try {
            String eventTitle = eventModel != null ?
                    eventModel.getEventTitle().replaceAll("[^a-zA-Z0-9]", "_") :
                    "event";
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String filename = String.format("%s_all_entrants_%s.csv", eventTitle, timestamp);

            File csvFile = new File(getCacheDir(), filename);
            FileWriter writer = new FileWriter(csvFile);

            writer.append("User ID,Full Name,Status\n");

            for (EntrantWithStatus entrant : entrantsWithStatus) {
                models.UserModel user = entrant.user;
                String status = entrant.status.toString();

                writer.append(escapeCSV(user.getId())).append(",");
                writer.append(escapeCSV(user.getFullName())).append(",");
                writer.append(escapeCSV(status)).append("\n");
            }

            writer.flush();
            writer.close();

            Log.i(tag, "CSV file created successfully: " + csvFile.getAbsolutePath());
            return csvFile;

        } catch (IOException e) {
            Log.e(tag, "Failed to create CSV file", e);
            return null;
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private void shareCSVFile(File csvFile, int entrantCount) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    csvFile
            );

            // Show dialog with options to View or Share
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("CSV Export Complete");
            builder.setMessage(entrantCount + " entrants exported successfully.\n\nWhat would you like to do?");

            // View button
            builder.setPositiveButton("View File", (dialog, which) -> {
                viewCSVFile(fileUri);
            });

            // Share button
            builder.setNeutralButton("Share File", (dialog, which) -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/csv");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "All Entrants List");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "List of all entrants for: " +
                                (eventModel != null ? eventModel.getEventTitle() : "Event"));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share CSV File"));
            });

            // Cancel button
            builder.setNegativeButton("Cancel", null);

            builder.show();

        } catch (Exception e) {
            Log.e(tag, "Failed to prepare CSV file", e);
            android.widget.Toast.makeText(this, "Failed to prepare CSV file: " + e.getMessage(),
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void viewCSVFile(Uri fileUri) {
        try {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(fileUri, "text/csv");
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Check if there's an app that can open CSV files
            if (viewIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(viewIntent, "Open CSV File"));
            } else {
                // No CSV viewer available, try as plain text
                viewIntent.setDataAndType(fileUri, "text/plain");
                if (viewIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(viewIntent, "Open CSV File"));
                } else {
                    // No viewer available at all
                    android.widget.Toast.makeText(this,
                            "No app available to view CSV files. Please save to Google Drive.",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(tag, "Failed to view CSV file", e);
            android.widget.Toast.makeText(this,
                    "Failed to open CSV file. Try sharing it instead.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private static class EntrantWithStatus {
        models.UserModel user;
        DatabaseConstants.ON_WAITING_LIST_STATUS status;

        EntrantWithStatus(models.UserModel user, DatabaseConstants.ON_WAITING_LIST_STATUS status) {
            this.user = user;
            this.status = status;
        }
    }
}
