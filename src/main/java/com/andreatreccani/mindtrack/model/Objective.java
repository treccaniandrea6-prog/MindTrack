package com.andreatreccani.mindtrack.model;

public class Objective {

    private final int id;
    private final String title;
    private final String period;
    private final int coins;
    private boolean available;

    public Objective(int id, String title, String period, int coins, boolean available) {
        this.id = id;
        this.title = title;
        this.period = period;
        this.coins = coins;
        this.available = available;
    }

    public int id() { return id; }
    public String title() { return title; }
    public String period() { return period; }
    public int coins() { return coins; }
    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}