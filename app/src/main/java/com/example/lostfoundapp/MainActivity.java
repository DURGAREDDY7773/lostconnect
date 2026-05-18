package com.example.lostfoundapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Spinner categorySpinner;
    Button addButton;
    ListView itemListView;

    DatabaseHelper databaseHelper;
    ArrayList<LostFoundItem> itemList;
    ItemAdapter itemAdapter;

    String[] categories = {"All", "Electronics", "Pets", "Wallets", "Documents", "Keys", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categorySpinner = findViewById(R.id.categorySpinner);
        addButton = findViewById(R.id.addButton);
        itemListView = findViewById(R.id.itemListView);

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        loadItems("All");

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                loadItems(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        itemListView.setOnItemClickListener((parent, view, position, id) -> {
            LostFoundItem selectedItem = itemList.get(position);

            Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
            intent.putExtra("item_id", selectedItem.id);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (categorySpinner != null) {
            loadItems(categorySpinner.getSelectedItem().toString());
        }
    }

    private void loadItems(String category) {

        itemList = new ArrayList<>();

        Cursor cursor = databaseHelper.getItemsByCategory(category);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LostFoundItem item = new LostFoundItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE))
                );

                itemList.add(item);

            } while (cursor.moveToNext());

            cursor.close();
        }

        itemAdapter = new ItemAdapter(this, itemList);
        itemListView.setAdapter(itemAdapter);
    }
}