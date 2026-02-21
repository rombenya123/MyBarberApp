package com.example.mybarberapp.dataClasses;

public class Booking {
    private String date;
    private String time;
    private String stylist;
    private String id; // Firestore document ID for deletion

    public Booking() { }

    public Booking(String date, String time, String stylist, String id) {
        this.date = date;
        this.time = time;
        this.stylist = stylist;
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStylist() {
        return stylist;
    }

    public String getId() {
        return id;
    }
}
