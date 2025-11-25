package com.example.konoha_events;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import constants.IntentConstants;
import models.EventModel;
import services.FirebaseService;
import util.ModelUtil;
import util.QRCodeUtil;

/**
 * EventQRCodeActivity
 * ----------------------
 * Displays the QR code for an event
 * Allows organizers to view and share the promotional QR code
 */
public class EventQRCodeActivity extends AppCompatActivity {

    private TextView eventTitleText;
    private ImageView qrCodeImageView;
    private Button shareButton, backButton;

    private String eventId;
    private EventModel currentEvent;
    private Bitmap qrCodeBitmap;
    private FirebaseService fbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_qr_code);

        fbs = FirebaseService.firebaseService;

        // Get event ID from intent
        eventId = getIntent().getStringExtra(IntentConstants.INTENT_EVENT_ID);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadEventAndGenerateQR();
    }

    /**
     * Initialize view components
     */
    private void initializeViews() {
        eventTitleText = findViewById(R.id.eventTitleText);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        shareButton = findViewById(R.id.shareButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());
        shareButton.setOnClickListener(v -> shareQRCode());
    }

    /**
     * Load event details and generate QR code
     */
    private void loadEventAndGenerateQR() {
        fbs.getEventDocumentReference(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = ModelUtil.toEventModel(documentSnapshot);
                        displayEventInfo();
                        generateAndDisplayQRCode();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Display event information
     */
    private void displayEventInfo() {
        if (currentEvent != null && currentEvent.getEventTitle() != null) {
            eventTitleText.setText(currentEvent.getEventTitle());
        } else {
            eventTitleText.setText("Event QR Code");
        }
    }

    /**
     * Generate and display the QR code
     */
    private void generateAndDisplayQRCode() {
        // Generate QR code bitmap from event ID using the standard format
        qrCodeBitmap = util.QRCodeUtil.generateQRCodeBitmapFromEventId(eventId, 800);

        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share the QR code image
     */
    private void shareQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "QR code not generated yet", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Save bitmap to MediaStore
            String title = currentEvent != null && currentEvent.getEventTitle() != null
                    ? currentEvent.getEventTitle() + " QR Code"
                    : "Event QR Code";

            String path = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    qrCodeBitmap,
                    title,
                    "Scan this QR code to view event details"
            );

            if (path != null) {
                Uri imageUri = Uri.parse(path);

                // Create share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "Scan this QR code to view event details!");

                startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            } else {
                Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to share QR code: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}