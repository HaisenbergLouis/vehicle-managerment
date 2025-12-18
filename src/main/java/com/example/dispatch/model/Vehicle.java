package com.example.dispatch.model;

public class Vehicle {
    private final String id;
    private String driver;
    private String status;
    private String location;

    public Vehicle(String id, String driver, String status, String location) {
        this.id = id;
        this.driver = driver;
        this.status = status;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

