package com.example.dispatch.view;

import com.example.dispatch.model.Task;
import com.example.dispatch.model.Vehicle;
import com.example.dispatch.service.DispatchService;
import com.example.dispatch.util.DataStore;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class MainView extends BorderPane {
    private final TableView<Vehicle> vehicleTable = new TableView<>();
    private final TableView<Task> taskTable = new TableView<>();
    private final DispatchService dispatchService = new DispatchService(DataStore.VEHICLES);

    public MainView() {
        setPadding(new Insets(12));
        setCenter(buildMainPane());
    }

    private SplitPane buildMainPane() {
        SplitPane split = new SplitPane(buildVehiclePane(), buildTaskPane());
        split.setDividerPositions(0.5);
        return split;
    }

    private VBox buildVehiclePane() {
        TableColumn<Vehicle, String> idCol = new TableColumn<>("车牌");
        idCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getId()));
        TableColumn<Vehicle, String> driverCol = new TableColumn<>("司机");
        driverCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDriver()));
        TableColumn<Vehicle, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStatus()));
        TableColumn<Vehicle, String> locCol = new TableColumn<>("位置");
        locCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getLocation()));

        vehicleTable.getColumns().addAll(idCol, driverCol, statusCol, locCol);
        vehicleTable.setItems(DataStore.VEHICLES);

        TextField idField = new TextField();
        idField.setPromptText("车牌号");
        TextField driverField = new TextField();
        driverField.setPromptText("司机");
        TextField locField = new TextField();
        locField.setPromptText("位置");
        Button addBtn = new Button("添加车辆");
        addBtn.setOnAction(e -> {
            if (!idField.getText().isBlank()) {
                try {
                    Vehicle newVehicle = new Vehicle(
                            idField.getText().trim(),
                            driverField.getText().trim().isEmpty() ? "未分配" : driverField.getText().trim(),
                            "空闲",
                            locField.getText().trim().isEmpty() ? "未知" : locField.getText().trim());

                    // 保存到数据库并更新UI
                    DataStore.addVehicle(newVehicle);

                    idField.clear();
                    driverField.clear();
                    locField.clear();

                    // 显示成功消息
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("添加成功");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("车辆 " + newVehicle.getId() + " 已成功添加！");
                    successAlert.showAndWait();

                } catch (Exception ex) {
                    // 显示错误消息
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("添加失败");
                    errorAlert.setHeaderText("添加车辆失败");
                    errorAlert.setContentText("错误信息: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });

        Button deleteBtn = new Button("删除车辆");
        deleteBtn.setOnAction(e -> {
            Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // 显示确认对话框
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("确认删除");
                confirmAlert.setHeaderText("删除车辆");
                confirmAlert.setContentText("确定要删除车辆 " + selected.getId() + " 吗？\n此操作无法撤销。");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            // 删除车辆
                            DataStore.deleteVehicle(selected.getId());

                            // 显示成功消息
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("删除成功");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("车辆 " + selected.getId() + " 已成功删除！");
                            successAlert.showAndWait();

                        } catch (Exception ex) {
                            // 显示错误消息
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("删除失败");
                            errorAlert.setHeaderText("删除车辆失败");
                            errorAlert.setContentText("错误信息: " + ex.getMessage());
                            errorAlert.showAndWait();
                        }
                    }
                });
            } else {
                // 显示提示消息
                Alert infoAlert = new Alert(Alert.AlertType.WARNING);
                infoAlert.setTitle("未选择车辆");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("请先选择要删除的车辆！");
                infoAlert.showAndWait();
            }
        });

        VBox box = new VBox(8, vehicleTable,
                new HBox(6, idField, driverField, locField, addBtn, deleteBtn));
        box.setPadding(new Insets(0, 8, 0, 0));
        return box;
    }

    private VBox buildTaskPane() {
        TableColumn<Task, String> nameCol = new TableColumn<>("任务");
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        TableColumn<Task, String> destCol = new TableColumn<>("目的地");
        destCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDestination()));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        TableColumn<Task, String> etaCol = new TableColumn<>("ETA");
        etaCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEta().format(fmt)));
        TableColumn<Task, String> vehicleCol = new TableColumn<>("车辆");
        vehicleCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getVehicleId() == null ? "未分配" : c.getValue().getVehicleId()));

        taskTable.getColumns().addAll(nameCol, destCol, etaCol, vehicleCol);
        taskTable.setItems(DataStore.TASKS);

        TextField nameField = new TextField();
        nameField.setPromptText("任务名");
        TextField destField = new TextField();
        destField.setPromptText("目的地");
        Button addTaskBtn = new Button("新增任务");
        addTaskBtn.setOnAction(e -> {
            if (!nameField.getText().isBlank()) {
                try {
                    Task newTask = new Task(
                            nameField.getText().trim(),
                            destField.getText().trim().isEmpty() ? "未知" : destField.getText().trim(),
                            java.time.LocalDateTime.now().plusHours(2));

                    // 保存到数据库并更新UI
                    DataStore.addTask(newTask);

                    nameField.clear();
                    destField.clear();

                    // 显示成功消息
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("添加成功");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("任务 '" + newTask.getName() + "' 已成功添加！");
                    successAlert.showAndWait();

                } catch (Exception ex) {
                    // 显示错误消息
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("添加失败");
                    errorAlert.setHeaderText("添加任务失败");
                    errorAlert.setContentText("错误信息: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });

        Button assignBtn = new Button("分配车辆");
        assignBtn.setOnAction(e -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Vehicle v = dispatchService.assign(selected);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("分配结果");
                alert.setContentText(v == null ? "无空闲车辆" : "已分配车辆 " + v.getId());
                alert.showAndWait();
                vehicleTable.refresh();
                taskTable.refresh();
            }
        });

        Button deleteTaskBtn = new Button("删除任务");
        deleteTaskBtn.setOnAction(e -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // 显示确认对话框
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("确认删除");
                confirmAlert.setHeaderText("删除任务");
                confirmAlert.setContentText("确定要删除任务 '" + selected.getName() + "' 吗？\n此操作无法撤销。");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            // 删除任务
                            DataStore.deleteTask(selected.getName());

                            // 显示成功消息
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("删除成功");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("任务 '" + selected.getName() + "' 已成功删除！");
                            successAlert.showAndWait();

                        } catch (Exception ex) {
                            // 显示错误消息
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("删除失败");
                            errorAlert.setHeaderText("删除任务失败");
                            errorAlert.setContentText("错误信息: " + ex.getMessage());
                            errorAlert.showAndWait();
                        }
                    }
                });
            } else {
                // 显示提示消息
                Alert infoAlert = new Alert(Alert.AlertType.WARNING);
                infoAlert.setTitle("未选择任务");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("请先选择要删除的任务！");
                infoAlert.showAndWait();
            }
        });

        VBox box = new VBox(8, taskTable,
                new HBox(6, nameField, destField, addTaskBtn, assignBtn, deleteTaskBtn));
        return box;
    }
}
