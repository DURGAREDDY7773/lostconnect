package com.example.lostfoundapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {

    Spinner typeSpinner, categorySpinnerAdd;
    EditText titleEditText, descriptionEditText, locationEditText, contactEditText;
    ImageView selectedImageView;
    Button selectImageButton, saveButton;

    DatabaseHelper databaseHelper;
    Uri selectedImageUri = null;

    String[] types = {"Lost", "Found"};
    String[] categories = {"Electronics", "Pets", "Wallets", "Documents", "Keys", "Others"};

    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        typeSpinner = findViewById(R.id.typeSpinner);
        categorySpinnerAdd = findViewById(R.id.categorySpinnerAdd);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        contactEditText = findViewById(R.id.contactEditText);
        selectedImageView = findViewById(R.id.selectedImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveButton = findViewById(R.id.saveButton);

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdd.setAdapter(categoryAdapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        selectedImageView.setImageURI(uri);
                    }
                }
        );

        selectImageButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        saveButton.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {

        String type = typeSpinner.getSelectedItem().toString();
        String category = categorySpinnerAdd.getSelectedItem().toString();
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            return;
        }

        if (location.isEmpty()) {
            locationEditText.setError("Location is required");
            return;
        }

        if (contact.isEmpty()) {
            contactEditText.setError("Contact is required");
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = databaseHelper.addItem(
                type,
                title,
                category,
                description,
                location,
                contact,
                selectedImageUri.toString()
        );

        if (inserted) {
            Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show();
        }
    }
}