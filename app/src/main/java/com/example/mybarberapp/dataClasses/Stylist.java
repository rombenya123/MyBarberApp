package com.example.mybarberapp.dataClasses;

import java.util.List;

public class Stylist {
    private String id;
    private String name;
    private List<String> availability;
    private String specialty;

    public Stylist() {

    }

    public Stylist(String id, String name, List<String> availability, String specialty) {
        this.id = id;
        this.name = name;
        this.availability = availability;
        this.specialty = specialty;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getAvailability() { return availability; }
    public void setAvailability(List<String> availability) { this.availability = availability; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}

