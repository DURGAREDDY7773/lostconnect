package com.example.lostconnect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 300;
    private static final LatLng MELBOURNE = new LatLng(-37.8136, 144.9631);

    private GoogleMap googleMap;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLatLng;
    private double radiusKm = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        radiusKm = parseRadius(getIntent().getStringExtra("radius_km"));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        enableUserLocation();
        loadCurrentLocationThenMarkers();
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void loadCurrentLocationThenMarkers() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            showItemMarkers();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                    } else {
                        Toast.makeText(this, "Turn on device location for radius search", Toast.LENGTH_SHORT).show();
                    }
                    showItemMarkers();
                })
                .addOnFailureListener(e -> showItemMarkers());
    }

    private void showItemMarkers() {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        Cursor cursor = databaseHelper.getAllItems();
        boolean movedCamera = false;

        if (userLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            if (radiusKm > 0) {
                googleMap.addCircle(new CircleOptions()
                        .center(userLatLng)
                        .radius(radiusKm * 1000)
                        .strokeColor(Color.argb(160, 30, 136, 229))
                        .fillColor(Color.argb(32, 30, 136, 229))
                        .strokeWidth(3f));
            }
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));

                if (latitude == 0 && longitude == 0) {
                    continue;
                }

                if (!isInsideRadius(latitude, longitude)) {
                    continue;
                }

                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));

                LatLng itemPosition = new LatLng(latitude, longitude);
                float markerColor = "Found".equalsIgnoreCase(type)
                        ? BitmapDescriptorFactory.HUE_GREEN
                        : BitmapDescriptorFactory.HUE_RED;

                googleMap.addMarker(new MarkerOptions()
                        .position(itemPosition)
                        .title(title)
                        .snippet(type + "\n" + address + "\n" + date)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                if (!movedCamera && userLatLng == null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemPosition, 13f));
                    movedCamera = true;
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        if (!movedCamera && userLatLng == null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 11f));
        }
    }

    private boolean isInsideRadius(double itemLat, double itemLng) {
        if (radiusKm <= 0 || userLatLng == null) {
            return true;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                userLatLng.latitude,
                userLatLng.longitude,
                itemLat,
                itemLng,
                results
        );
        return results[0] / 1000 <= radiusKm;
    }

    private double parseRadius(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid radius ignored", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            loadCurrentLocationThenMarkers();
        }
    }
}
