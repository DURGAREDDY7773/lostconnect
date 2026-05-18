package com.example.lostconnect;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    private final Context context;
    private final ArrayList<LostFoundItem> itemList;
    private final OnItemClickListener clickListener;

    public ItemAdapter(Context context, ArrayList<LostFoundItem> itemList, OnItemClickListener clickListener) {
        this.context = context;
        this.itemList = itemList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        LostFoundItem item = itemList.get(position);

        holder.titleText.setText(item.type + ": " + item.title);
        holder.categoryText.setText("Category: " + item.category);
        holder.dateText.setText("Posted: " + item.date);
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(item));

        try {
            holder.imageView.setImageURI(Uri.parse(item.image));
        } catch (Exception e) {
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleText, categoryText, dateText;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImage);
            titleText = itemView.findViewById(R.id.itemTitle);
            categoryText = itemView.findViewById(R.id.itemCategory);
            dateText = itemView.findViewById(R.id.itemDate);
        }
    }
}
