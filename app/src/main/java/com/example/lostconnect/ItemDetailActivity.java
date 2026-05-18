package com.example.lostconnect;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView detailImage;
    TextView detailTitle, detailCategory, detailDate, detailDescription, detailLocation, detailContact;
    Button deleteButton;

    DatabaseHelper databaseHelper;
    int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailCategory = findViewById(R.id.detailCategory);
        detailDate = findViewById(R.id.detailDate);
        detailDescription = findViewById(R.id.detailDescription);
        detailLocation = findViewById(R.id.detailLocation);
        detailContact = findViewById(R.id.detailContact);
        deleteButton = findViewById(R.id.deleteButton);

        databaseHelper = new DatabaseHelper(this);

        itemId = getIntent().getIntExtra("item_id", -1);

        if (itemId != -1) {
            loadItemDetails();
        }

        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void loadItemDetails() {

        Cursor cursor = databaseHelper.getItemById(itemId);

        if (cursor != null && cursor.moveToFirst()) {

            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
            String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT));
            String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));

            detailTitle.setText(type + ": " + title);
            detailCategory.setText("Category: " + category);
            detailDate.setText("Posted on: " + date);
            detailDescription.setText("Description: " + description);
            detailLocation.setText("Location: " + location + "\nLat/Lng: " + latitude + ", " + longitude);
            detailContact.setText("Contact: " + contact);

            try {
                detailImage.setImageURI(Uri.parse(image));
            } catch (Exception e) {
                detailImage.setImageResource(R.mipmap.ic_launcher);
            }

            cursor.close();
        }
    }

    private void confirmDelete() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Advert");
        builder.setMessage("Are you sure you want to remove this advert after finding the owner?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            boolean deleted = databaseHelper.deleteItem(itemId);

            if (deleted) {
                Toast.makeText(this, "Advert removed", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to remove advert", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", null);
        builder.show();
    }
}
