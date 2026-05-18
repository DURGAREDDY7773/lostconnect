package com.example.lostconnect;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Spinner categorySpinner;
    Button addButton, showOnMapButton;
    EditText searchEditText, radiusEditText;
    RecyclerView itemRecyclerView;

    DatabaseHelper databaseHelper;
    ArrayList<LostFoundItem> itemList;
    ItemAdapter itemAdapter;

    String[] categories = {"All", "Electronics", "Pets", "Wallets", "Documents", "Keys", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categorySpinner = findViewById(R.id.categorySpinner);
        searchEditText = findViewById(R.id.searchEditText);
        addButton = findViewById(R.id.addButton);
        showOnMapButton = findViewById(R.id.showOnMapButton);
        radiusEditText = findViewById(R.id.radiusEditText);
        itemRecyclerView = findViewById(R.id.itemRecyclerView);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        loadItems("All", "");

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        showOnMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("radius_km", radiusEditText.getText().toString().trim());
            startActivity(intent);
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                loadItems(selectedCategory, searchEditText.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadItems(categorySpinner.getSelectedItem().toString(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (categorySpinner != null) {
            String searchText = searchEditText == null ? "" : searchEditText.getText().toString();
            loadItems(categorySpinner.getSelectedItem().toString(), searchText);
        }
    }

    private void loadItems(String category, String searchText) {

        itemList = new ArrayList<>();

        Cursor cursor = databaseHelper.searchItems(category, searchText);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LostFoundItem item = new LostFoundItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE))
                );

                itemList.add(item);

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        itemAdapter = new ItemAdapter(this, itemList, selectedItem -> {
            Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
            intent.putExtra("item_id", selectedItem.id);
            startActivity(intent);
        });
        itemRecyclerView.setAdapter(itemAdapter);
    }
}
