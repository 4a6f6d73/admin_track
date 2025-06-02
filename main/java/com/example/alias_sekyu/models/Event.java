package com.example.alias_sekyu.models;

public class Event {
    private int id;
    private String title;
    private String venue;
    private String date;
    private String time;

    public Event(int id, String title, String venue, String date, String time) {
        this.id = id;
        this.title = title;
        this.venue = venue;
        this.date = date;
        this.time = time;
    }
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getVenue() {
        return venue;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
}