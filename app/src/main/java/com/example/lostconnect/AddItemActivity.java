package com.example.lostconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 200;

    Spinner typeSpinner, categorySpinnerAdd;
    EditText titleEditText, descriptionEditText, locationEditText, contactEditText;
    ImageView selectedImageView;
    Button selectImageButton, currentLocationButton, saveButton;

    DatabaseHelper databaseHelper;
    FusedLocationProviderClient fusedLocationClient;
    Uri selectedImageUri = null;
    double selectedLatitude = 0;
    double selectedLongitude = 0;
    boolean hasSelectedCoordinates = false;

    String[] types = {"Lost", "Found"};
    String[] categories = {"Electronics", "Pets", "Wallets", "Documents", "Keys", "Others"};

    ActivityResultLauncher<String> imagePickerLauncher;
    ActivityResultLauncher<Intent> autocompleteLauncher;

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
        currentLocationButton = findViewById(R.id.currentLocationButton);
        saveButton = findViewById(R.id.saveButton);

        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initialisePlaces();
        setupSpinners();
        setupLaunchers();

        locationEditText.setFocusable(false);
        locationEditText.setOnClickListener(v -> openLocationAutocomplete());
        currentLocationButton.setOnClickListener(v -> getCurrentLocation());
        selectImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        saveButton.setOnClickListener(v -> saveItem());
    }

    private void initialisePlaces() {
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized() && !"YOUR_API_KEY".equals(apiKey)) {
            Places.initialize(getApplicationContext(), apiKey);
        }
    }

    private void setupSpinners() {
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
    }

    private void setupLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        selectedImageView.setImageURI(uri);
                    }
                }
        );

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        if (place.getLatLng() != null) {
                            selectedLatitude = place.getLatLng().latitude;
                            selectedLongitude = place.getLatLng().longitude;
                            hasSelectedCoordinates = true;
                        }
                        locationEditText.setText(place.getAddress());
                    }
                }
        );
    }

    private void openLocationAutocomplete() {
        if (!Places.isInitialized()) {
            Toast.makeText(this, "Add your Google Maps API key first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Current location is not available yet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                    hasSelectedCoordinates = true;
                    locationEditText.setText(getAddressFromCoordinates(selectedLatitude, selectedLongitude));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show());
    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (Exception ignored) {
        }
        return latitude + ", " + longitude;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
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

        if (location.isEmpty() || !hasSelectedCoordinates) {
            locationEditText.setError("Choose a location or use current location");
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
                selectedLatitude,
                selectedLongitude,
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
