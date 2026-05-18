package com.example.lostfoundapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {

    Context context;
    ArrayList<LostFoundItem> itemList;

    public ItemAdapter(Context context, ArrayList<LostFoundItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.itemImage);
        TextView titleText = view.findViewById(R.id.itemTitle);
        TextView categoryText = view.findViewById(R.id.itemCategory);
        TextView dateText = view.findViewById(R.id.itemDate);

        LostFoundItem item = itemList.get(position);

        titleText.setText(item.type + ": " + item.title);
        categoryText.setText("Category: " + item.category);
        dateText.setText("Posted: " + item.date);

        try {
            imageView.setImageURI(Uri.parse(item.image));
        } catch (Exception e) {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }

        return view;
    }
}