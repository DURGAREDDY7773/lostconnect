package com.example.lostconnect;

public class LostFoundItem {

    int id;
    String type;
    String title;
    String category;
    String description;
    String location;
    double latitude;
    double longitude;
    String contact;
    String image;
    String date;

    public LostFoundItem(int id, String type, String title, String category,
                         String description, String location, double latitude, double longitude, String contact,
                         String image, String date) {

        this.id = id;
        this.type = type;
        this.title = title;
        this.category = category;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contact = contact;
        this.image = image;
        this.date = date;
    }
}
