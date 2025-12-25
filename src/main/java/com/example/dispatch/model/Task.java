package com.example.dispatch.model;

import java.time.LocalDateTime;

public class Task {
    private final String name;
    private final String destination;
    private final LocalDateTime eta;
    private String vehicleId;
    private String status; // 任务状态
    private int createdBy; // 创建者用户ID

    public Task(String name, String destination, LocalDateTime eta) {
        this.name = name;
        this.destination = destination;
        this.eta = eta;
        this.status = "待分配"; // 默认状态
        this.createdBy = 0; // 默认值
    }

    public Task(String name, String destination, LocalDateTime eta, int createdBy) {
        this.name = name;
        this.destination = destination;
        this.eta = eta;
        this.status = "待分配"; // 默认状态
        this.createdBy = createdBy;
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

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
