package com.example.dispatch.model;

import java.time.LocalDateTime;

public class Task {
    private final String name;
    private final String destination;
    private final LocalDateTime eta;
    private String vehicleId;

    public Task(String name, String destination, LocalDateTime eta) {
        this.name = name;
        this.destination = destination;
        this.eta = eta;
    }

    public String getName() {
        return name;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getEta() {
        return eta;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void assignVehicle(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}
