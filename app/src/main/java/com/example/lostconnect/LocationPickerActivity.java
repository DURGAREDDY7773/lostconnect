package com.example.lostconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 400;
    private static final LatLng MELBOURNE = new LatLng(-37.8136, 144.9631);

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText searchEditText;
    private TextView selectedAddressTextView;
    private Marker selectedMarker;
    private double selectedLatitude;
    private double selectedLongitude;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        searchEditText = findViewById(R.id.mapSearchEditText);
        selectedAddressTextView = findViewById(R.id.selectedAddressTextView);
        Button searchButton = findViewById(R.id.mapSearchButton);
        Button saveButton = findViewById(R.id.saveMapLocationButton);

        searchButton.setOnClickListener(v -> searchLocation());
        saveButton.setOnClickListener(v -> saveLocation());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.locationPickerMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMapClickListener(this::selectLocation);

        double initialLatitude = getIntent().getDoubleExtra("latitude", 0);
        double initialLongitude = getIntent().getDoubleExtra("longitude", 0);
        if (initialLatitude != 0 || initialLongitude != 0) {
            selectLocation(new LatLng(initialLatitude, initialLongitude));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(initialLatitude, initialLongitude), 16f));
        } else {
            moveToCurrentLocation();
        }
    }

    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 12f));
            return;
        }

        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 12f));
                        return;
                    }

                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                })
                .addOnFailureListener(e ->
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 12f)));
    }

    private void searchLocation() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            searchEditText.setError("Enter a location");
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this, "No matching location found", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            LatLng position = new LatLng(address.getLatitude(), address.getLongitude());
            selectLocation(position);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16f));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to search location", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectLocation(LatLng position) {
        selectedLatitude = position.latitude;
        selectedLongitude = position.longitude;
        selectedAddress = getAddressFromCoordinates(selectedLatitude, selectedLongitude);
        selectedAddressTextView.setText(selectedAddress);

        if (selectedMarker != null) {
            selectedMarker.remove();
        }
        selectedMarker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Selected Location")
                .snippet(selectedAddress));
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

    private void saveLocation() {
        if (selectedMarker == null) {
            Toast.makeText(this, "Tap the map to choose a location", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra("address", selectedAddress);
        result.putExtra("latitude", selectedLatitude);
        result.putExtra("longitude", selectedLongitude);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation();
        }
    }
}
