package com.example.dispatch.service;

import com.example.dispatch.model.Task;
import com.example.dispatch.model.Vehicle;
import com.example.dispatch.util.DataStore;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class DispatchService {
    private final ObservableList<Vehicle> vehicles;

    public DispatchService(ObservableList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    /**
     * 分配车辆给任务（简单策略：选择第一个空闲车辆）
     */
    public Vehicle assign(Task task) {
        try {
            // 获取空闲车辆列表
            List<Vehicle> availableVehicles = DataStore.getAvailableVehicles();

            if (availableVehicles.isEmpty()) {
                System.out.println("没有空闲车辆可用");
                return null;
            }

            // 选择第一个空闲车辆
            Vehicle selectedVehicle = availableVehicles.get(0);

            // 分配车辆给任务
            DataStore.assignVehicleToTask(task.getName(), selectedVehicle.getId());

            System.out.println("成功分配车辆 " + selectedVehicle.getId() + " 给任务 " + task.getName());
            return selectedVehicle;

        } catch (SQLException e) {
            System.err.println("车辆分配失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 智能分配车辆（考虑距离、时间等因素）
     * 这是一个扩展接口，当前使用简单策略
     */
    public Vehicle assignSmart(Task task, String strategy) {
        switch (strategy.toLowerCase()) {
            case "nearest":
                return assignByNearest(task);
            case "fastest":
                return assignByFastest(task);
            case "load_balance":
                return assignByLoadBalance(task);
            default:
                return assign(task); // 默认使用简单策略
        }
    }

    /**
     * 按最近距离分配
     */
    private Vehicle assignByNearest(Task task) {
        // 这里可以实现基于GPS距离的分配逻辑
        // 当前先使用简单策略
        return assign(task);
    }

    /**
     * 按最快到达时间分配
     */
    private Vehicle assignByFastest(Task task) {
        // 这里可以实现基于预计到达时间的分配逻辑
        // 当前先使用简单策略
        return assign(task);
    }

    /**
     * 按负载均衡分配
     */
    private Vehicle assignByLoadBalance(Task task) {
        try {
            // 获取所有空闲车辆
            List<Vehicle> availableVehicles = DataStore.getAvailableVehicles();

            if (availableVehicles.isEmpty()) {
                return null;
            }

            // 统计每辆车的任务数量，选择任务最少的车辆
            Vehicle bestVehicle = availableVehicles.get(0);
            int minTasks = Integer.MAX_VALUE;

            for (Vehicle vehicle : availableVehicles) {
                List<Task> vehicleTasks = DataStore.getTaskDAO().findByVehicleId(vehicle.getId());
                if (vehicleTasks.size() < minTasks) {
                    minTasks = vehicleTasks.size();
                    bestVehicle = vehicle;
                }
            }

            // 分配车辆
            DataStore.assignVehicleToTask(task.getName(), bestVehicle.getId());
            return bestVehicle;

        } catch (SQLException e) {
            System.err.println("负载均衡分配失败: " + e.getMessage());
            return assign(task); // 失败时回退到简单策略
        }
    }

    /**
     * 取消任务分配
     */
    public boolean unassign(Task task) {
        try {
            if (task.getVehicleId() != null) {
                // 取消分配
                DataStore.assignVehicleToTask(task.getName(), null);

                // 将车辆状态改为空闲
                Vehicle vehicle = DataStore.findVehicleById(task.getVehicleId());
                if (vehicle != null) {
                    vehicle.setStatus("空闲");
                    DataStore.updateVehicle(vehicle);
                }

                System.out.println("已取消任务 " + task.getName() + " 的车辆分配");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("取消分配失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 批量分配任务
     */
    public int assignMultiple(List<Task> tasks) {
        int successCount = 0;
        for (Task task : tasks) {
            if (assign(task) != null) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 获取分配建议
     */
    public List<Vehicle> getAssignmentSuggestions(Task task) {
        try {
            // 返回所有空闲车辆作为建议
            return DataStore.getAvailableVehicles();
        } catch (SQLException e) {
            System.err.println("获取分配建议失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
