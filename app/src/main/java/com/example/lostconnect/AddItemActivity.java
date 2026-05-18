package com.example.lostconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.libraries.places.api.Places;

import java.util.Arrays;
import java.util.ArrayList;
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
        locationEditText.setOnClickListener(v -> showAddressSearchDialog());
        currentLocationButton.setOnClickListener(v -> getCurrentLocation());
        selectImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        saveButton.setOnClickListener(v -> saveItem());
    }

    private void initialisePlaces() {
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized() && !"YOUR_API_KEY".equals(apiKey)) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);
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

    }

    private void showAddressSearchDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(40, 20, 40, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        AutoCompleteTextView addressInput = new AutoCompleteTextView(this);
        addressInput.setHint("Search address");
        addressInput.setThreshold(3);
        layout.addView(addressInput);

        ArrayList<AddressOption> addressOptions = new ArrayList<>();
        ArrayAdapter<AddressOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                addressOptions
        );
        addressInput.setAdapter(adapter);

        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() < 3) {
                    return;
                }

                addressOptions.clear();
                addressOptions.addAll(searchAddresses(query));
                adapter.notifyDataSetChanged();
                addressInput.showDropDown();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addressInput.setOnItemClickListener((parent, view, position, id) -> {
            AddressOption option = addressOptions.get(position);
            selectedLatitude = option.latitude;
            selectedLongitude = option.longitude;
            hasSelectedCoordinates = true;
            locationEditText.setText(option.address);
        });

        new AlertDialog.Builder(this)
                .setTitle("Search Location")
                .setView(layout)
                .setPositiveButton("Use Location", (dialog, which) -> {
                    if (hasSelectedCoordinates) {
                        return;
                    }

                    List<AddressOption> matches = searchAddresses(addressInput.getText().toString().trim());
                    if (matches.isEmpty()) {
                        Toast.makeText(this, "Unable to find that address", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AddressOption option = matches.get(0);
                    selectedLatitude = option.latitude;
                    selectedLongitude = option.longitude;
                    hasSelectedCoordinates = true;
                    locationEditText.setText(option.address);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private List<AddressOption> searchAddresses(String query) {
        ArrayList<AddressOption> results = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            if (addresses != null) {
                for (Address address : addresses) {
                    if (address.hasLatitude() && address.hasLongitude()) {
                        results.add(new AddressOption(
                                address.getAddressLine(0),
                                address.getLatitude(),
                                address.getLongitude()
                        ));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return results;
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

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Turn on device location and try again", Toast.LENGTH_SHORT).show();
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

    private static class AddressOption {
        String address;
        double latitude;
        double longitude;

        AddressOption(String address, double latitude, double longitude) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @NonNull
        @Override
        public String toString() {
            return address;
        }
    }
}
