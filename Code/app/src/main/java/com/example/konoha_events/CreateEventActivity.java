package com.example.konoha_events;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Date;
import services.FirebaseService;

public class CreateEventActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText eventTitleInput, eventDescriptionInput, entrantLimitInput;
    private Button selectDeadlineButton, selectImageButton, createButton;
    private CheckBox limitEntrantsCheckbox;
    private ImageView eventPosterPreview;
    private String deviceId;
    private Date selectedDeadline;
    private Uri selectedImageUri;
    private FirebaseService fbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        fbs = FirebaseService.firebaseService;
        deviceId = getIntent().getStringExtra("deviceId");

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        eventTitleInput = findViewById(R.id.eventTitleInput);
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput);
        entrantLimitInput = findViewById(R.id.entrantLimitInput);
        limitEntrantsCheckbox = findViewById(R.id.limitEntrantsCheckbox);
        selectDeadlineButton = findViewById(R.id.selectDeadlineButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        createButton = findViewById(R.id.createButton);
        eventPosterPreview = findViewById(R.id.eventPosterPreview);
    }

    private void setupListeners() {
        // US 02.03.01 - Optional entrant limit
        limitEntrantsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            entrantLimitInput.setEnabled(isChecked);
            if (!isChecked) {
                entrantLimitInput.setText("");
            }
        });

        // US 02.01.04 - Registration deadline
        selectDeadlineButton.setOnClickListener(v -> showDatePicker());

        // US 02.04.01 - Event poster upload
        selectImageButton.setOnClickListener(v -> selectImage());

        createButton.setOnClickListener(v -> createEvent());

        // Cancel button
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDeadline = calendar.getTime();
            selectDeadlineButton.setText("Deadline: " + selectedDeadline.toString());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Event Poster"),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            eventPosterPreview.setImageURI(selectedImageUri);
        }
    }

    private void createEvent() {
        String title = eventTitleInput.getText().toString().trim();
        String description = eventDescriptionInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDeadline == null) {
            Toast.makeText(this, "Please select a registration deadline",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Integer entrantLimit = -1;
        if (limitEntrantsCheckbox.isChecked()) {
            String limitStr = entrantLimitInput.getText().toString().trim();
            if (!limitStr.isEmpty()) {
                entrantLimit = Integer.parseInt(limitStr);
            }
        }

        fbs.createEvent(deviceId, entrantLimit, selectedDeadline, title,
                description, selectedImageUri, fbs.getCurrentUserId());

        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}