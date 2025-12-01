package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import constants.DatabaseConstants;
import services.FirebaseService;

/**
 * Screen that lets the entrant view, edit, and delete their profile.
 * Loads the user's existing data, allows updates, and saves changes to Firestore.
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private EditText fullName, email, phone;
    private Button save, delete;
    private ImageButton back;

    private DocumentReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_profile_settings);

        fullName = findViewById(R.id.editTextFullName);
        email    = findViewById(R.id.editTextEmail);
        phone    = findViewById(R.id.editTextPhone);
        save     = findViewById(R.id.buttonSave);
        delete   = findViewById(R.id.buttonDelete);
        back     = findViewById(R.id.back_button);


        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (TextUtils.isEmpty(userId)) {
            userId = FirebaseService.firebaseService.getCurrentUserId();
        }
        //Don't really need anymore, just placed it when I didn't have an ID to pull from.
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Missing user id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseService.firebaseService.getUserDocumentReference(userId);

        //Load profile information
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> prefill(documentSnapshot))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );

        save.setOnClickListener(v -> save());
        delete.setOnClickListener(v -> delete());
        back.setOnClickListener(v -> finish());
    }
    //Fill the editText boxes to show the current users information. Not specified in
    //the project description but is nice to have.
    /**
     * Fills the text fields with existing user data from Firestore.
     *
     * @param snap The Firestore document containing user fields.
     */
    private void prefill(@NonNull DocumentSnapshot snap) {
        if (!snap.exists()) return;

        String newname  = snap.getString(DatabaseConstants.COLLECTION_USERS_FULL_NAME_FIELD);
        String newemail = snap.getString(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD);
        String newphone = snap.getString(DatabaseConstants.COLLECTION_USERS_PHONE_FIELD);

        if (newname  != null) fullName.setText(newname);
        if (newemail != null) email.setText(newemail);
        if (newphone != null) phone.setText(newphone);
    }
    //Save information when the user is done entering the new information.
    //Add new information to database. It's fine if a phone number is removed
    //but probably not if a full name is removed. Can remove if i'm wrong.
    //eventually should add checks for duplicates but function first.
    /**
     * Saves any changes the user made to their profile fields.
     * Requires that full name and email are not empty.
     */
    private void save() {
        String newname  = fullName.getText().toString().trim();
        String newemail = email.getText().toString().trim();
        String newphone = phone.getText().toString().trim();

        if (newname.isEmpty() || newemail.isEmpty()) {
            Toast.makeText(this, "Full name and email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(DatabaseConstants.COLLECTION_USERS_FULL_NAME_FIELD, newname);
        updates.put(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD, newemail);
        updates.put(DatabaseConstants.COLLECTION_USERS_PHONE_FIELD,
                newphone.isEmpty() ? null : newphone);

        userRef.update(updates)
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show());
    }
    //delete profile from database entry. Maybe it's better to save the
    //table entry just remove the fields?
    //Removing table entry for now.
    /**
     * Deletes the user's profile document from Firestore.
     * After deletion, the user is sent back to the HomeActivity.
     */
    private void delete() {
        userRef.delete()
                .addOnSuccessListener(v -> { //success, return to home screen
                    Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show());
    }
}
