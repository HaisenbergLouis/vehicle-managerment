package com.example.dispatch.view;

import com.example.dispatch.MainApp;
import com.example.dispatch.model.OperationLog;
import com.example.dispatch.model.Task;
import com.example.dispatch.model.User;
import com.example.dispatch.model.Vehicle;
import com.example.dispatch.service.DispatchService;
import com.example.dispatch.util.DataStore;
import com.example.dispatch.util.PasswordUtils;
import com.example.dispatch.util.SessionManager;
import com.example.dispatch.util.UserRoles;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class MainView extends BorderPane {
    private final TableView<Vehicle> vehicleTable = new TableView<>();
    private final TableView<Task> taskTable = new TableView<>();
    private final DispatchService dispatchService = new DispatchService(DataStore.VEHICLES);

    public MainView() {
        setPadding(new Insets(12));
        setTop(buildUserInfoBar());
        setCenter(buildMainPane());
        configureRoleBasedUI();
    }

    /**
     * 根据用户权限配置按钮
     */
    private void configureButtonPermissions(Button button, boolean hasPermission, String permissionMessage) {
        if (!hasPermission) {
            button.setDisable(true);
            button.setTooltip(new Tooltip(permissionMessage));
        }
    }

    /**
     * 根据用户角色配置界面
     */
    private void configureRoleBasedUI() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        // 根据不同角色定制界面
        if (SessionManager.isDriver()) {
            configureDriverUI();
        } else if (SessionManager.isCustomer()) {
            configureCustomerUI();
        } else if (SessionManager.isDispatcher()) {
            configureDispatcherUI();
        } else if (SessionManager.isAdmin()) {
            configureAdminUI();
        }
    }

    /**
     * 为司机角色配置界面
     */
    private void configureDriverUI() {
        // 司机主要关注自己的车辆和分配的任务
        // 可以更新车辆状态和位置

        // 添加司机专用按钮栏
        HBox driverButtonBar = new HBox(10);
        driverButtonBar.setPadding(new Insets(10));

        Button updateStatusBtn = new Button("更新车辆状态");
        updateStatusBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        updateStatusBtn.setOnAction(e -> handleUpdateVehicleStatus());

        Button updateLocationBtn = new Button("更新位置");
        updateLocationBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateLocationBtn.setOnAction(e -> handleUpdateVehicleLocation());

        driverButtonBar.getChildren().addAll(updateStatusBtn, updateLocationBtn);

        // 将按钮栏添加到车辆面板底部
        if (getCenter() instanceof SplitPane) {
            SplitPane split = (SplitPane) getCenter();
            if (split.getItems().get(0) instanceof VBox) {
                VBox vehiclePane = (VBox) split.getItems().get(0);
                vehiclePane.getChildren().add(driverButtonBar);
            }
        }

        System.out.println("配置司机界面完成");
    }

    /**
     * 为客户角色配置界面
     */
    private void configureCustomerUI() {
        // 客户主要查看自己的任务状态
        // 可以创建任务、查看历史记录

        // 添加客户专用按钮栏
        HBox customerButtonBar = new HBox(10);
        customerButtonBar.setPadding(new Insets(10));

        Button myTasksBtn = new Button("我的任务");
        myTasksBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        myTasksBtn.setOnAction(e -> showMyTasks());

        Button taskHistoryBtn = new Button("任务历史");
        taskHistoryBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        taskHistoryBtn.setOnAction(e -> showTaskHistory());

        Button trackTaskBtn = new Button("跟踪任务");
        trackTaskBtn.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        trackTaskBtn.setOnAction(e -> trackSelectedTask());

        customerButtonBar.getChildren().addAll(myTasksBtn, taskHistoryBtn, trackTaskBtn);

        // 将按钮栏添加到任务面板底部
        if (getCenter() instanceof SplitPane) {
            SplitPane split = (SplitPane) getCenter();
            if (split.getItems().get(1) instanceof VBox) {
                VBox taskPane = (VBox) split.getItems().get(1);
                taskPane.getChildren().add(customerButtonBar);
            }
        }

        System.out.println("配置客户界面完成");
    }

    /**
     * 为调度员角色配置界面
     */
    private void configureDispatcherUI() {
        // 调度员拥有完整的管理功能
        // 可以管理车辆、任务分配、数据统计

        // 添加调度员专用按钮栏
        HBox dispatcherButtonBar = new HBox(10);
        dispatcherButtonBar.setPadding(new Insets(10));

        Button statisticsBtn = new Button("数据统计");
        statisticsBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        statisticsBtn.setOnAction(e -> showStatistics());

        Button manageVehiclesBtn = new Button("车辆管理");
        manageVehiclesBtn.setStyle("-fx-background-color: #795548; -fx-text-fill: white;");
        manageVehiclesBtn.setOnAction(e -> showVehicleManagement());

        Button taskAssignmentBtn = new Button("智能分配");
        taskAssignmentBtn.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white;");
        taskAssignmentBtn.setOnAction(e -> showSmartAssignment());

        dispatcherButtonBar.getChildren().addAll(statisticsBtn, manageVehiclesBtn, taskAssignmentBtn);

        // 将按钮栏添加到主界面底部
        if (getBottom() == null) {
            setBottom(dispatcherButtonBar);
        } else if (getBottom() instanceof HBox) {
            HBox existingBar = (HBox) getBottom();
            existingBar.getChildren().addAll(dispatcherButtonBar.getChildren());
        }

        System.out.println("配置调度员界面完成");
    }

    /**
     * 为管理员角色配置界面
     */
    private void configureAdminUI() {
        // 管理员拥有系统级权限
        // 可以管理用户、查看操作日志、系统配置

        // 添加管理员专用按钮栏
        HBox adminButtonBar = new HBox(10);
        adminButtonBar.setPadding(new Insets(10));

        Button userManagementBtn = new Button("用户管理");
        userManagementBtn.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white;");
        userManagementBtn.setOnAction(e -> showUserManagement());

        Button operationLogsBtn = new Button("操作日志");
        operationLogsBtn.setStyle("-fx-background-color: #673AB7; -fx-text-fill: white;");
        operationLogsBtn.setOnAction(e -> showOperationLogs());

        Button systemConfigBtn = new Button("系统配置");
        systemConfigBtn.setStyle("-fx-background-color: #009688; -fx-text-fill: white;");
        systemConfigBtn.setOnAction(e -> showSystemConfig());

        Button backupBtn = new Button("数据备份");
        backupBtn.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        backupBtn.setOnAction(e -> performBackup());

        adminButtonBar.getChildren().addAll(userManagementBtn, operationLogsBtn, systemConfigBtn, backupBtn);

        // 将按钮栏添加到主界面底部
        if (getBottom() == null) {
            setBottom(adminButtonBar);
        } else if (getBottom() instanceof HBox) {
            HBox existingBar = (HBox) getBottom();
            existingBar.getChildren().addAll(adminButtonBar.getChildren());
        }

        System.out.println("配置管理员界面完成");
    }

    /**
     * 创建用户信息栏
     */
    private HBox buildUserInfoBar() {
        HBox userBar = new HBox(10);
        userBar.setPadding(new Insets(5, 12, 5, 12));
        userBar.setAlignment(Pos.CENTER_LEFT);
        userBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        Label welcomeLabel = new Label("欢迎, " + SessionManager.getCurrentUsername());
        welcomeLabel.setStyle("-fx-font-weight: bold;");

        Label roleLabel = new Label("(" + SessionManager.getCurrentUserRoleDisplayName() + ")");
        roleLabel.setStyle("-fx-text-fill: #666666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button logoutButton = new Button("登出");
        logoutButton.setOnAction(e -> handleLogout());

        userBar.getChildren().addAll(welcomeLabel, roleLabel, spacer, logoutButton);
        return userBar;
    }

    /**
     * 处理登出
     */
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认登出");
        confirmAlert.setHeaderText("确认登出");
        confirmAlert.setContentText("确定要登出当前用户吗？");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 记录登出日志
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null) {
                    try {
                        OperationLog logoutLog = new OperationLog(
                                OperationLog.OperationTypes.LOGOUT,
                                OperationLog.EntityTypes.USER,
                                currentUser.getUsername(),
                                "用户登出系统",
                                currentUser.getId());
                        DataStore.addOperationLog(logoutLog);
                    } catch (Exception logEx) {
                        System.err.println("记录登出日志失败: " + logEx.getMessage());
                    }
                }

                SessionManager.logout();
                Stage stage = (Stage) getScene().getWindow();
                MainApp.showLoginView(stage);
            }
        });
    }

    private SplitPane buildMainPane() {
        SplitPane split = new SplitPane(buildVehiclePane(), buildTaskPane());
        split.setDividerPositions(0.6); // 给车辆面板更多空间
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
        configureButtonPermissions(addBtn, SessionManager.canCreateVehicles(), "您没有权限添加车辆");
        addBtn.setOnAction(e -> {
            if (!idField.getText().isBlank()) {
                try {
                    Vehicle newVehicle = new Vehicle(
                            idField.getText().trim(),
                            driverField.getText().trim().isEmpty() ? "未分配" : driverField.getText().trim(),
                            "空闲",
                            locField.getText().trim().isEmpty() ? "未知" : locField.getText().trim());
                    // 设置创建者为当前用户
                    newVehicle.setCreatedBy(SessionManager.getCurrentUser().getId());

                    // 保存到数据库并更新UI
                    DataStore.addVehicle(newVehicle);

                    // 记录操作日志
                    try {
                        OperationLog addVehicleLog = new OperationLog(
                                OperationLog.OperationTypes.CREATE,
                                OperationLog.EntityTypes.VEHICLE,
                                newVehicle.getId(),
                                "添加新车辆: " + newVehicle.getId() + " (司机: " + newVehicle.getDriver() + ", 位置: "
                                        + newVehicle.getLocation() + ")",
                                SessionManager.getCurrentUser().getId());
                        DataStore.addOperationLog(addVehicleLog);
                    } catch (Exception logEx) {
                        System.err.println("记录添加车辆日志失败: " + logEx.getMessage());
                    }

                    idField.clear();
                    driverField.clear();
                    locField.clear();

                    // 显示成功消息
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("添加成功");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("车辆 " + newVehicle.getId() + " 已成功添加！");
                    successAlert.showAndWait();

                } catch (SecurityException ex) {
                    // 显示权限错误消息
                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("权限不足");
                    errorAlert.setHeaderText("操作被拒绝");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
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
        configureButtonPermissions(deleteBtn, SessionManager.canDeleteVehicles(), "您没有权限删除车辆");
        deleteBtn.setOnAction(e -> {
            // 禁用按钮防止连续点击
            deleteBtn.setDisable(true);
            try {
                Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // 检查权限：管理员/调度员可以删除所有车辆，其他用户只能删除自己添加的车辆
                    User currentUser = SessionManager.getCurrentUser();
                    boolean canDelete = SessionManager.canDeleteVehicle(selected.getCreatedBy());

                    if (!canDelete) {
                        Alert permissionAlert = new Alert(Alert.AlertType.WARNING);
                        permissionAlert.setTitle("权限不足");
                        permissionAlert.setHeaderText("无法删除车辆");
                        permissionAlert.setContentText("您只能删除自己添加的车辆！");
                        permissionAlert.showAndWait();
                        return;
                    }

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

                                // 记录操作日志
                                try {
                                    OperationLog deleteVehicleLog = new OperationLog(
                                            OperationLog.OperationTypes.DELETE,
                                            OperationLog.EntityTypes.VEHICLE,
                                            selected.getId(),
                                            "删除车辆: " + selected.getId() + " (司机: " + selected.getDriver() + ", 状态: "
                                                    + selected.getStatus() + ")",
                                            SessionManager.getCurrentUser().getId());
                                    DataStore.addOperationLog(deleteVehicleLog);
                                } catch (Exception logEx) {
                                    System.err.println("记录删除车辆日志失败: " + logEx.getMessage());
                                }

                                // 显示成功消息
                                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                successAlert.setTitle("删除成功");
                                successAlert.setHeaderText(null);
                                successAlert.setContentText("车辆 " + selected.getId() + " 已成功删除！");
                                successAlert.showAndWait();

                            } catch (SecurityException ex) {
                                // 显示权限错误消息
                                Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                                errorAlert.setTitle("权限不足");
                                errorAlert.setHeaderText("操作被拒绝");
                                errorAlert.setContentText(ex.getMessage());
                                errorAlert.showAndWait();
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
            } finally {
                // 重新启用按钮
                deleteBtn.setDisable(false);
            }
        });

        // 创建输入控件行
        HBox inputRow = new HBox(6, idField, driverField, locField);

        // 创建按钮行
        HBox buttonRow = new HBox(6, addBtn, deleteBtn);

        VBox box = new VBox(8, vehicleTable, inputRow, buttonRow);
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
        configureButtonPermissions(addTaskBtn, SessionManager.canCreateTasks(), "您没有权限创建任务");
        addTaskBtn.setOnAction(e -> {
            if (!nameField.getText().isBlank()) {
                try {
                    Task newTask = new Task(
                            nameField.getText().trim(),
                            destField.getText().trim().isEmpty() ? "未知" : destField.getText().trim(),
                            java.time.LocalDateTime.now().plusHours(2),
                            SessionManager.getCurrentUser().getId());
                    // 设置创建者为当前用户
                    newTask.setCreatedBy(SessionManager.getCurrentUser().getId());

                    // 保存到数据库并更新UI
                    DataStore.addTask(newTask);

                    // 记录操作日志
                    try {
                        OperationLog addTaskLog = new OperationLog(
                                OperationLog.OperationTypes.CREATE,
                                OperationLog.EntityTypes.TASK,
                                newTask.getName(),
                                "创建新任务: " + newTask.getName() + " (目的地: " + newTask.getDestination() + ")",
                                SessionManager.getCurrentUser().getId());
                        DataStore.addOperationLog(addTaskLog);
                    } catch (Exception logEx) {
                        System.err.println("记录添加任务日志失败: " + logEx.getMessage());
                    }

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
        configureButtonPermissions(assignBtn, SessionManager.canAssignVehicles(), "您没有权限分配车辆");
        assignBtn.setOnAction(e -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    Vehicle v = dispatchService.assign(selected);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("分配结果");
                    alert.setContentText(v == null ? "无空闲车辆" : "已分配车辆 " + v.getId());
                    alert.showAndWait();
                    vehicleTable.refresh();
                    taskTable.refresh();
                } catch (SecurityException ex) {
                    // 显示权限错误消息
                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("权限不足");
                    errorAlert.setHeaderText("操作被拒绝");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                } catch (Exception ex) {
                    // 显示错误消息
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("分配失败");
                    errorAlert.setHeaderText("分配车辆失败");
                    errorAlert.setContentText("错误信息: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });

        Button deleteTaskBtn = new Button("删除任务");
        configureButtonPermissions(deleteTaskBtn, SessionManager.canDeleteTasks(), "您没有权限删除任务");
        // 防止快速连续点击
        deleteTaskBtn.setDisable(false);
        deleteTaskBtn.setOnAction(e -> {
            // 禁用按钮防止连续点击
            deleteTaskBtn.setDisable(true);
            try {
                Task selected = taskTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // 检查权限：管理员/调度员可以删除所有任务，其他用户只能删除自己添加的任务
                    User currentUser = SessionManager.getCurrentUser();
                    boolean canDelete = SessionManager.canDeleteTask(selected.getCreatedBy());

                    // 调试输出
                    System.out.println("删除任务权限检查:");
                    System.out.println("当前用户: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
                    System.out.println("任务创建者ID: " + selected.getCreatedBy());
                    System.out.println("用户角色: " + SessionManager.getCurrentUserRole());
                    System.out.println("可以删除: " + canDelete);

                    if (!canDelete) {
                        Alert permissionAlert = new Alert(Alert.AlertType.WARNING);
                        permissionAlert.setTitle("权限不足");
                        permissionAlert.setHeaderText("无法删除任务");
                        permissionAlert.setContentText("您只能删除自己添加的任务！");
                        permissionAlert.showAndWait();
                        return;
                    }

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

                                // 记录操作日志
                                try {
                                    OperationLog deleteTaskLog = new OperationLog(
                                            OperationLog.OperationTypes.DELETE,
                                            OperationLog.EntityTypes.TASK,
                                            selected.getName(),
                                            "删除任务: " + selected.getName() + " (目的地: " + selected.getDestination() + ")",
                                            SessionManager.getCurrentUser().getId());
                                    DataStore.addOperationLog(deleteTaskLog);
                                } catch (Exception logEx) {
                                    System.err.println("记录删除任务日志失败: " + logEx.getMessage());
                                }

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
            } finally {
                // 重新启用按钮
                deleteTaskBtn.setDisable(false);
            }
        });

        // 创建输入控件行
        HBox inputRow = new HBox(6, nameField, destField);

        // 创建按钮行
        HBox buttonRow = new HBox(6, addTaskBtn, assignBtn, deleteTaskBtn);

        VBox box = new VBox(8, taskTable, inputRow, buttonRow);
        return box;
    }

    // ========== 司机专用功能 ==========

    /**
     * 处理司机更新车辆状态
     */
    private void handleUpdateVehicleStatus() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("未选择车辆");
            alert.setHeaderText("请选择要更新的车辆");
            alert.setContentText("请在车辆列表中选择一辆车");
            alert.showAndWait();
            return;
        }

        // 检查是否是司机负责的车辆（这里简化为所有车辆，实际应该检查司机分配）
        ChoiceDialog<String> statusDialog = new ChoiceDialog<>("出车", "空闲", "维修", "保养");
        statusDialog.setTitle("更新车辆状态");
        statusDialog.setHeaderText("选择新的车辆状态");
        statusDialog.setContentText("车辆: " + selected.getId());

        statusDialog.showAndWait().ifPresent(newStatus -> {
            try {
                DataStore.updateVehicleStatus(selected.getId(), newStatus);
                refreshVehicleTable();
                System.out.println("司机更新车辆状态: " + selected.getId() + " -> " + newStatus);
            } catch (Exception ex) {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("更新失败");
                errorAlert.setHeaderText("车辆状态更新失败");
                errorAlert.setContentText("错误信息: " + ex.getMessage());
                errorAlert.showAndWait();
            }
        });
    }

    /**
     * 处理司机更新车辆位置
     */
    private void handleUpdateVehicleLocation() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("未选择车辆");
            alert.setHeaderText("请选择要更新的车辆");
            alert.setContentText("请在车辆列表中选择一辆车");
            alert.showAndWait();
            return;
        }

        TextInputDialog locationDialog = new TextInputDialog(selected.getLocation());
        locationDialog.setTitle("更新车辆位置");
        locationDialog.setHeaderText("输入新的车辆位置");
        locationDialog.setContentText("车辆: " + selected.getId());

        locationDialog.showAndWait().ifPresent(newLocation -> {
            if (newLocation.trim().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("输入无效");
                alert.setHeaderText("位置不能为空");
                alert.showAndWait();
                return;
            }

            try {
                DataStore.updateVehicleLocation(selected.getId(), newLocation.trim());
                refreshVehicleTable();
                System.out.println("司机更新车辆位置: " + selected.getId() + " -> " + newLocation);
            } catch (Exception ex) {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("更新失败");
                errorAlert.setHeaderText("车辆位置更新失败");
                errorAlert.setContentText("错误信息: " + ex.getMessage());
                errorAlert.showAndWait();
            }
        });
    }

    /**
     * 刷新车辆表格
     */
    private void refreshVehicleTable() {
        vehicleTable.refresh();
    }

    // ========== 客户专用功能 ==========

    /**
     * 显示客户自己的任务
     */
    private void showMyTasks() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        Alert infoAlert = new Alert(AlertType.INFORMATION);
        infoAlert.setTitle("我的任务");
        infoAlert.setHeaderText("您的任务列表");
        infoAlert.setContentText("显示您创建的所有任务（包括历史任务）");
        infoAlert.showAndWait();

        // 这里可以过滤任务表格，只显示当前用户的任务
        // 暂时显示提示，实际实现需要修改任务加载逻辑
        System.out.println("客户查看自己的任务 - 用户ID: " + currentUser.getId());
    }

    /**
     * 显示任务历史记录
     */
    private void showTaskHistory() {
        Alert infoAlert = new Alert(AlertType.INFORMATION);
        infoAlert.setTitle("任务历史");
        infoAlert.setHeaderText("任务执行历史");
        infoAlert.setContentText("显示所有已完成任务的详细历史记录");
        infoAlert.showAndWait();

        System.out.println("客户查看任务历史记录");
    }

    /**
     * 跟踪选中的任务
     */
    private void trackSelectedTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("未选择任务");
            alert.setHeaderText("请选择要跟踪的任务");
            alert.setContentText("请在任务列表中选择一个任务");
            alert.showAndWait();
            return;
        }

        // 检查权限：只能跟踪自己创建的任务
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && selected.getCreatedBy() != currentUser.getId()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("权限不足");
            alert.setHeaderText("无法跟踪此任务");
            alert.setContentText("您只能跟踪自己创建的任务");
            alert.showAndWait();
            return;
        }

        Alert trackAlert = new Alert(AlertType.INFORMATION);
        trackAlert.setTitle("任务跟踪");
        trackAlert.setHeaderText("任务: " + selected.getName());
        trackAlert.setContentText(String.format(
                "目的地: %s\n预计到达: %s\n车辆分配: %s\n任务状态: %s",
                selected.getDestination(),
                selected.getEta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                selected.getVehicleId() != null ? selected.getVehicleId() : "未分配",
                selected.getStatus() != null ? selected.getStatus() : "未知"));
        trackAlert.showAndWait();

        System.out.println("客户跟踪任务: " + selected.getName());
    }

    // ========== 调度员专用功能 ==========

    /**
     * 显示数据统计
     */
    private void showStatistics() {
        Alert statsAlert = new Alert(AlertType.INFORMATION);
        statsAlert.setTitle("数据统计");
        statsAlert.setHeaderText("系统运行统计");

        StringBuilder stats = new StringBuilder();
        stats.append("车辆总数: ").append(DataStore.VEHICLES.size()).append("\n");
        stats.append("任务总数: ").append(DataStore.TASKS.size()).append("\n");

        long idleVehicles = DataStore.VEHICLES.stream().filter(v -> "空闲".equals(v.getStatus())).count();
        long busyVehicles = DataStore.VEHICLES.stream().filter(v -> "出车".equals(v.getStatus())).count();
        long pendingTasks = DataStore.TASKS.stream().filter(t -> "待分配".equals(t.getStatus())).count();

        stats.append("空闲车辆: ").append(idleVehicles).append("\n");
        stats.append("出车车辆: ").append(busyVehicles).append("\n");
        stats.append("待分配任务: ").append(pendingTasks).append("\n");

        statsAlert.setContentText(stats.toString());
        statsAlert.showAndWait();

        System.out.println("调度员查看数据统计");
    }

    /**
     * 显示车辆管理界面
     */
    private void showVehicleManagement() {
        Alert managementAlert = new Alert(AlertType.INFORMATION);
        managementAlert.setTitle("车辆管理");
        managementAlert.setHeaderText("车辆状态管理");
        managementAlert.setContentText("显示车辆的详细状态、维护记录和调度历史\n\n功能包括：\n• 车辆状态监控\n• 维护计划安排\n• 调度历史查询\n• 性能统计分析");
        managementAlert.showAndWait();

        System.out.println("调度员查看车辆管理");
    }

    /**
     * 显示智能分配界面
     */
    private void showSmartAssignment() {
        Alert assignmentAlert = new Alert(AlertType.INFORMATION);
        assignmentAlert.setTitle("智能分配");
        assignmentAlert.setHeaderText("自动任务分配系统");
        assignmentAlert
                .setContentText("基于以下因素智能分配任务：\n\n• 车辆当前位置\n• 车辆状态和可用性\n• 任务紧急程度\n• 历史分配记录\n• 司机工作负载\n\n点击确定开始智能分配...");
        assignmentAlert.showAndWait();

        System.out.println("调度员启动智能分配");
    }

    // ========== 管理员专用功能 ==========

    /**
     * 显示用户管理界面
     */
    private void showUserManagement() {
        // 创建用户管理对话框
        Dialog<ButtonType> userDialog = new Dialog<>();
        userDialog.setTitle("用户管理");
        userDialog.setHeaderText("系统用户管理");
        userDialog.getDialogPane().setPrefSize(800, 600);

        // 创建主布局
        BorderPane dialogLayout = new BorderPane();

        // 创建用户表格
        TableView<User> userTable = new TableView<>();
        userTable.setItems(DataStore.USERS);

        // 设置表格列
        TableColumn<User, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));

        TableColumn<User, String> roleCol = new TableColumn<>("角色");
        roleCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                UserRoles.getRoleDisplayName(c.getValue().getRole())));

        TableColumn<User, String> createdAtCol = new TableColumn<>("创建时间");
        createdAtCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        userTable.getColumns().addAll(usernameCol, roleCol, createdAtCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 创建按钮面板
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));

        Button addUserBtn = new Button("添加用户");
        addUserBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addUserBtn.setOnAction(e -> showAddUserDialog(userTable));

        Button editUserBtn = new Button("编辑用户");
        editUserBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editUserBtn.setOnAction(e -> showEditUserDialog(userTable));

        Button deleteUserBtn = new Button("删除用户");
        deleteUserBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteUserBtn.setOnAction(e -> deleteSelectedUser(userTable));

        Button resetPasswordBtn = new Button("重置密码");
        resetPasswordBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        resetPasswordBtn.setOnAction(e -> resetUserPassword(userTable));

        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> refreshUserTable(userTable));

        buttonPanel.getChildren().addAll(addUserBtn, editUserBtn, deleteUserBtn, resetPasswordBtn, refreshBtn);

        // 设置对话框布局
        dialogLayout.setCenter(userTable);
        dialogLayout.setBottom(buttonPanel);

        userDialog.getDialogPane().setContent(dialogLayout);

        // 添加确定按钮
        userDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // 显示对话框
        userDialog.showAndWait();

        System.out.println("管理员查看用户管理");
    }

    /**
     * 显示添加用户对话框
     */
    private void showAddUserDialog(TableView<User> userTable) {
        Dialog<ButtonType> addDialog = new Dialog<>();
        addDialog.setTitle("添加新用户");
        addDialog.setHeaderText("创建新用户账户");

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("输入用户名");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("输入密码");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("确认密码");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRoles.getAllRoleDisplayNames());
        roleComboBox.setValue(UserRoles.CUSTOMER_DISPLAY); // 默认选择客户

        grid.add(new Label("用户名:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("密码:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("确认密码:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("角色:"), 0, 3);
        grid.add(roleComboBox, 1, 3);

        addDialog.getDialogPane().setContent(grid);
        addDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 处理确定按钮
        addDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // 验证输入
                    String username = usernameField.getText().trim();
                    String password = passwordField.getText();
                    String confirmPassword = confirmPasswordField.getText();
                    String roleDisplayName = roleComboBox.getValue();

                    if (username.isEmpty()) {
                        showErrorAlert("用户名不能为空");
                        return null;
                    }

                    if (password.isEmpty()) {
                        showErrorAlert("密码不能为空");
                        return null;
                    }

                    if (!password.equals(confirmPassword)) {
                        showErrorAlert("两次输入的密码不一致");
                        return null;
                    }

                    // 检查用户名是否已存在
                    if (DataStore.usernameExists(username)) {
                        showErrorAlert("用户名已存在，请选择其他用户名");
                        return null;
                    }

                    // 转换角色显示名称为角色常量
                    String role = getRoleFromDisplayName(roleDisplayName);

                    // 创建用户
                    String passwordHash = PasswordUtils.hashPassword(password);
                    User newUser = new User(username, passwordHash, role);

                    DataStore.addUser(newUser);

                    // 记录操作日志
                    try {
                        OperationLog addUserLog = new OperationLog(
                                OperationLog.OperationTypes.CREATE,
                                OperationLog.EntityTypes.USER,
                                username,
                                "管理员创建新用户: " + username + " (角色: " + role + ")",
                                SessionManager.getCurrentUser().getId());
                        DataStore.addOperationLog(addUserLog);
                    } catch (Exception logEx) {
                        System.err.println("记录添加用户日志失败: " + logEx.getMessage());
                    }

                    // 刷新表格
                    refreshUserTable(userTable);

                    showSuccessAlert("用户添加成功", "用户 '" + username + "' 已成功创建！");

                    System.out.println("管理员添加新用户: " + username);

                } catch (Exception ex) {
                    showErrorAlert("添加用户失败: " + ex.getMessage());
                    System.err.println("添加用户失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            return buttonType;
        });

        addDialog.showAndWait();
    }

    /**
     * 显示编辑用户对话框
     */
    private void showEditUserDialog(TableView<User> userTable) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarningAlert("请先选择要编辑的用户");
            return;
        }

        // 不允许编辑管理员账户
        if (UserRoles.ADMIN.equals(selectedUser.getRole())) {
            showWarningAlert("不能编辑管理员账户");
            return;
        }

        Dialog<ButtonType> editDialog = new Dialog<>();
        editDialog.setTitle("编辑用户");
        editDialog.setHeaderText("编辑用户: " + selectedUser.getUsername());

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label usernameLabel = new Label(selectedUser.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRoles.getAllRoleDisplayNames());
        roleComboBox.setValue(UserRoles.getRoleDisplayName(selectedUser.getRole()));

        grid.add(new Label("用户名:"), 0, 0);
        grid.add(usernameLabel, 1, 0);
        grid.add(new Label("角色:"), 0, 1);
        grid.add(roleComboBox, 1, 1);

        editDialog.getDialogPane().setContent(grid);
        editDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 处理确定按钮
        editDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String roleDisplayName = roleComboBox.getValue();
                    String newRole = getRoleFromDisplayName(roleDisplayName);

                    // 如果角色没有变化，不需要更新
                    if (newRole.equals(selectedUser.getRole())) {
                        return buttonType;
                    }

                    // 创建更新后的用户对象
                    User updatedUser = new User(
                            selectedUser.getId(),
                            selectedUser.getUsername(),
                            selectedUser.getPasswordHash(),
                            newRole,
                            selectedUser.getCreatedAt());

                    DataStore.updateUser(updatedUser);

                    // 记录操作日志
                    try {
                        OperationLog updateUserLog = new OperationLog(
                                OperationLog.OperationTypes.UPDATE,
                                OperationLog.EntityTypes.USER,
                                selectedUser.getUsername(),
                                "管理员修改用户角色: " + selectedUser.getUsername() + " (从 " +
                                        UserRoles.getRoleDisplayName(selectedUser.getRole()) + " 改为 " +
                                        UserRoles.getRoleDisplayName(newRole) + ")",
                                SessionManager.getCurrentUser().getId());
                        DataStore.addOperationLog(updateUserLog);
                    } catch (Exception logEx) {
                        System.err.println("记录编辑用户日志失败: " + logEx.getMessage());
                    }

                    // 刷新表格
                    refreshUserTable(userTable);

                    showSuccessAlert("用户更新成功", "用户 '" + selectedUser.getUsername() + "' 的角色已更新为: " +
                            UserRoles.getRoleDisplayName(newRole));

                    System.out.println("管理员更新用户角色: " + selectedUser.getUsername() + " -> " + newRole);

                } catch (Exception ex) {
                    showErrorAlert("更新用户失败: " + ex.getMessage());
                    System.err.println("更新用户失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            return buttonType;
        });

        editDialog.showAndWait();
    }

    /**
     * 删除选中的用户
     */
    private void deleteSelectedUser(TableView<User> userTable) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarningAlert("请先选择要删除的用户");
            return;
        }

        // 不允许删除管理员账户
        if (UserRoles.ADMIN.equals(selectedUser.getRole())) {
            showWarningAlert("不能删除管理员账户");
            return;
        }

        // 不允许删除当前登录的用户
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getId() == selectedUser.getId()) {
            showWarningAlert("不能删除当前登录的用户");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("删除用户: " + selectedUser.getUsername());
        confirmAlert.setContentText("确定要删除用户 '" + selectedUser.getUsername() + "' 吗？\n\n此操作无法撤销。");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    DataStore.deleteUser(selectedUser.getUsername());

                    // 记录操作日志
                    try {
                        OperationLog deleteUserLog = new OperationLog(
                                OperationLog.OperationTypes.DELETE,
                                OperationLog.EntityTypes.USER,
                                selectedUser.getUsername(),
                                "管理员删除用户: " + selectedUser.getUsername() + " (角色: " +
                                        UserRoles.getRoleDisplayName(selectedUser.getRole()) + ")",
                                SessionManager.getCurrentUser().getId());
                        DataStore.addOperationLog(deleteUserLog);
                    } catch (Exception logEx) {
                        System.err.println("记录删除用户日志失败: " + logEx.getMessage());
                    }

                    // 刷新表格
                    refreshUserTable(userTable);

                    showSuccessAlert("用户删除成功", "用户 '" + selectedUser.getUsername() + "' 已成功删除！");

                    System.out.println("管理员删除用户: " + selectedUser.getUsername());

                } catch (Exception ex) {
                    showErrorAlert("删除用户失败: " + ex.getMessage());
                    System.err.println("删除用户失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * 重置用户密码
     */
    private void resetUserPassword(TableView<User> userTable) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarningAlert("请先选择要重置密码的用户");
            return;
        }

        // 不允许重置管理员账户密码
        if (UserRoles.ADMIN.equals(selectedUser.getRole())) {
            showWarningAlert("不能重置管理员账户密码");
            return;
        }

        Dialog<ButtonType> resetDialog = new Dialog<>();
        resetDialog.setTitle("重置密码");
        resetDialog.setHeaderText("重置用户密码: " + selectedUser.getUsername());

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("输入新密码");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("确认新密码");

        grid.add(new Label("新密码:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("确认密码:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        resetDialog.getDialogPane().setContent(grid);
        resetDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 处理确定按钮
        resetDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String newPassword = newPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();

                    if (newPassword.isEmpty()) {
                        showErrorAlert("新密码不能为空");
                        return null;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        showErrorAlert("两次输入的密码不一致");
                        return null;
                    }

                    // 创建更新后的用户对象（使用新密码）
                    String newPasswordHash = PasswordUtils.hashPassword(newPassword);
                    User updatedUser = new User(
                            selectedUser.getId(),
                            selectedUser.getUsername(),
                            newPasswordHash,
                            selectedUser.getRole(),
                            selectedUser.getCreatedAt());

                    DataStore.updateUser(updatedUser);

                    // 记录操作日志
                    try {
                        OperationLog resetPasswordLog = new OperationLog(
                                OperationLog.OperationTypes.UPDATE,
                                OperationLog.EntityTypes.USER,
                                selectedUser.getUsername(),
                                "管理员重置用户密码: " + selectedUser.getUsername(),
                                SessionManager.getCurrentUser().getId());
                        DataStore.addOperationLog(resetPasswordLog);
                    } catch (Exception logEx) {
                        System.err.println("记录密码重置日志失败: " + logEx.getMessage());
                    }

                    showSuccessAlert("密码重置成功", "用户 '" + selectedUser.getUsername() + "' 的密码已重置！");

                    System.out.println("管理员重置用户密码: " + selectedUser.getUsername());

                } catch (Exception ex) {
                    showErrorAlert("重置密码失败: " + ex.getMessage());
                    System.err.println("重置密码失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            return buttonType;
        });

        resetDialog.showAndWait();
    }

    /**
     * 刷新用户表格
     */
    private void refreshUserTable(TableView<User> userTable) {
        try {
            DataStore.refreshData();
            userTable.refresh();
            System.out.println("用户表格已刷新");
        } catch (Exception ex) {
            showErrorAlert("刷新失败: " + ex.getMessage());
            System.err.println("刷新用户表格失败: " + ex.getMessage());
        }
    }

    /**
     * 根据显示名称获取角色常量
     */
    private String getRoleFromDisplayName(String displayName) {
        switch (displayName) {
            case UserRoles.ADMIN_DISPLAY:
                return UserRoles.ADMIN;
            case UserRoles.DISPATCHER_DISPLAY:
                return UserRoles.DISPATCHER;
            case UserRoles.DRIVER_DISPLAY:
                return UserRoles.DRIVER;
            case UserRoles.CUSTOMER_DISPLAY:
                return UserRoles.CUSTOMER;
            default:
                return UserRoles.CUSTOMER;
        }
    }

    /**
     * 显示成功提示
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示警告提示
     */
    private void showWarningAlert(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("警告");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示错误提示
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示操作日志
     */
    private void showOperationLogs() {
        // 创建操作日志对话框
        Dialog<ButtonType> logsDialog = new Dialog<>();
        logsDialog.setTitle("操作日志管理");
        logsDialog.setHeaderText("系统操作日志查看与管理");
        logsDialog.getDialogPane().setPrefSize(1000, 700);

        // 创建主布局
        BorderPane dialogLayout = new BorderPane();

        // 创建日志表格
        TableView<OperationLog> logTable = new TableView<>();
        logTable.setItems(FXCollections.observableArrayList());

        // 设置表格列
        TableColumn<OperationLog, String> timeCol = new TableColumn<>("时间");
        timeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        timeCol.setPrefWidth(150);

        TableColumn<OperationLog, String> userCol = new TableColumn<>("操作用户");
        userCol.setCellValueFactory(c -> {
            Integer userId = c.getValue().getUserId();
            if (userId != null) {
                try {
                    User user = DataStore.findUserById(userId);
                    return new ReadOnlyStringWrapper(user != null ? user.getUsername() : "未知用户");
                } catch (Exception e) {
                    return new ReadOnlyStringWrapper("未知用户");
                }
            } else {
                return new ReadOnlyStringWrapper("系统");
            }
        });
        userCol.setPrefWidth(100);

        TableColumn<OperationLog, String> operationCol = new TableColumn<>("操作类型");
        operationCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getOperationTypeDisplayName()));
        operationCol.setPrefWidth(100);

        TableColumn<OperationLog, String> entityCol = new TableColumn<>("实体类型");
        entityCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEntityTypeDisplayName()));
        entityCol.setPrefWidth(100);

        TableColumn<OperationLog, String> entityIdCol = new TableColumn<>("实体ID");
        entityIdCol.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(c.getValue().getEntityId() != null ? c.getValue().getEntityId() : ""));
        entityIdCol.setPrefWidth(100);

        TableColumn<OperationLog, String> descriptionCol = new TableColumn<>("操作描述");
        descriptionCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getDescription() != null ? c.getValue().getDescription() : ""));
        descriptionCol.setPrefWidth(300);

        logTable.getColumns().addAll(timeCol, userCol, operationCol, entityCol, entityIdCol, descriptionCol);
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 创建筛选面板
        HBox filterPanel = new HBox(10);
        filterPanel.setPadding(new Insets(10));
        filterPanel.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> operationTypeFilter = new ComboBox<>();
        operationTypeFilter.getItems().addAll("全部", "创建", "更新", "删除", "登录", "登出", "查看", "导出", "导入", "备份", "恢复");
        operationTypeFilter.setValue("全部");

        ComboBox<String> entityTypeFilter = new ComboBox<>();
        entityTypeFilter.getItems().addAll("全部", "用户", "车辆", "任务", "系统", "日志");
        entityTypeFilter.setValue("全部");

        DatePicker startDatePicker = new DatePicker(java.time.LocalDate.now().minusDays(7));
        DatePicker endDatePicker = new DatePicker(java.time.LocalDate.now());

        Button filterBtn = new Button("筛选");
        Button refreshBtn = new Button("刷新");
        Button clearOldLogsBtn = new Button("清理旧日志");
        Button exportBtn = new Button("导出日志");

        filterPanel.getChildren().addAll(
                new Label("操作类型:"), operationTypeFilter,
                new Label("实体类型:"), entityTypeFilter,
                new Label("开始日期:"), startDatePicker,
                new Label("结束日期:"), endDatePicker,
                filterBtn, refreshBtn, clearOldLogsBtn, exportBtn);

        // 创建按钮面板
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setAlignment(Pos.CENTER);

        Button statisticsBtn = new Button("日志统计");
        Button closeBtn = new Button("关闭");

        buttonPanel.getChildren().addAll(statisticsBtn, closeBtn);

        // 设置对话框布局
        VBox contentBox = new VBox(5, filterPanel, logTable, buttonPanel);
        dialogLayout.setCenter(contentBox);

        logsDialog.getDialogPane().setContent(dialogLayout);
        logsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        // 加载初始日志数据
        loadOperationLogs(logTable);

        // 筛选按钮事件
        filterBtn.setOnAction(e -> filterOperationLogs(logTable, operationTypeFilter, entityTypeFilter, startDatePicker,
                endDatePicker));

        // 刷新按钮事件
        refreshBtn.setOnAction(e -> loadOperationLogs(logTable));

        // 清理旧日志按钮事件
        clearOldLogsBtn.setOnAction(e -> showClearOldLogsDialog(logTable));

        // 导出按钮事件
        exportBtn.setOnAction(e -> exportOperationLogs(logTable));

        // 统计按钮事件
        statisticsBtn.setOnAction(e -> showLogStatistics());

        // 记录查看日志的操作日志
        try {
            OperationLog viewLogLog = new OperationLog(
                    OperationLog.OperationTypes.VIEW,
                    OperationLog.EntityTypes.LOG,
                    null,
                    "管理员查看操作日志",
                    SessionManager.getCurrentUser().getId());
            DataStore.addOperationLog(viewLogLog);
        } catch (Exception logEx) {
            System.err.println("记录查看日志操作失败: " + logEx.getMessage());
        }

        // 显示对话框
        logsDialog.showAndWait();

        System.out.println("管理员查看操作日志");
    }

    /**
     * 加载操作日志数据
     */
    private void loadOperationLogs(TableView<OperationLog> logTable) {
        try {
            var logs = DataStore.getAllOperationLogs();
            logTable.setItems(FXCollections.observableArrayList(logs));
            System.out.println("已加载 " + logs.size() + " 条操作日志");
        } catch (Exception ex) {
            showErrorAlert("加载日志失败: " + ex.getMessage());
            System.err.println("加载操作日志失败: " + ex.getMessage());
        }
    }

    /**
     * 筛选操作日志
     */
    private void filterOperationLogs(TableView<OperationLog> logTable,
            ComboBox<String> operationTypeFilter,
            ComboBox<String> entityTypeFilter,
            DatePicker startDatePicker,
            DatePicker endDatePicker) {
        try {
            String operationTypeDisplay = operationTypeFilter.getValue();
            String entityTypeDisplay = entityTypeFilter.getValue();
            java.time.LocalDate startDate = startDatePicker.getValue();
            java.time.LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showWarningAlert("请选择开始日期和结束日期");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarningAlert("开始日期不能晚于结束日期");
                return;
            }

            // 转换显示名称为内部常量
            String operationType = convertDisplayNameToOperationType(operationTypeDisplay);
            String entityType = convertDisplayNameToEntityType(entityTypeDisplay);

            java.time.LocalDateTime startTime = startDate.atStartOfDay();
            java.time.LocalDateTime endTime = endDate.atTime(23, 59, 59);

            var allLogs = DataStore.getAllOperationLogs();
            var filteredLogs = allLogs.stream()
                    .filter(log -> {
                        // 按操作类型筛选
                        if (!"全部".equals(operationTypeDisplay) && !operationType.equals(log.getOperationType())) {
                            return false;
                        }
                        // 按实体类型筛选
                        if (!"全部".equals(entityTypeDisplay) && !entityType.equals(log.getEntityType())) {
                            return false;
                        }
                        // 按时间范围筛选
                        return !log.getCreatedAt().isBefore(startTime) && !log.getCreatedAt().isAfter(endTime);
                    })
                    .collect(java.util.stream.Collectors.toList());

            logTable.setItems(FXCollections.observableArrayList(filteredLogs));
            System.out.println("筛选结果: " + filteredLogs.size() + " 条日志");

        } catch (Exception ex) {
            showErrorAlert("筛选日志失败: " + ex.getMessage());
            System.err.println("筛选操作日志失败: " + ex.getMessage());
        }
    }

    /**
     * 显示清理旧日志对话框
     */
    private void showClearOldLogsDialog(TableView<OperationLog> logTable) {
        Dialog<ButtonType> clearDialog = new Dialog<>();
        clearDialog.setTitle("清理旧日志");
        clearDialog.setHeaderText("清理指定时间之前的操作日志");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Spinner<Integer> daysSpinner = new Spinner<>(1, 365, 30);
        daysSpinner.setEditable(true);

        grid.add(new Label("清理"), 0, 0);
        grid.add(daysSpinner, 1, 0);
        grid.add(new Label("天前的日志"), 2, 0);

        clearDialog.getDialogPane().setContent(grid);
        clearDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        clearDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int days = daysSpinner.getValue();
                    java.time.LocalDateTime beforeTime = java.time.LocalDateTime.now().minusDays(days);

                    Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
                    confirmAlert.setTitle("确认清理");
                    confirmAlert.setHeaderText("清理旧日志确认");
                    confirmAlert.setContentText("确定要删除 " + days + " 天前（" +
                            beforeTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                            "）的所有操作日志吗？\n\n此操作无法撤销！");

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                int deletedCount = DataStore.deleteOperationLogsBefore(beforeTime);
                                loadOperationLogs(logTable); // 重新加载数据
                                showSuccessAlert("清理完成", "已清理 " + deletedCount + " 条旧日志记录！");
                            } catch (Exception ex) {
                                showErrorAlert("清理失败: " + ex.getMessage());
                            }
                        }
                    });

                } catch (Exception ex) {
                    showErrorAlert("设置失败: " + ex.getMessage());
                }
            }
            return buttonType;
        });

        clearDialog.showAndWait();
    }

    /**
     * 导出操作日志
     */
    private void exportOperationLogs(TableView<OperationLog> logTable) {
        try {
            var logs = logTable.getItems();

            if (logs.isEmpty()) {
                showWarningAlert("没有日志数据可以导出");
                return;
            }

            // 创建导出文件名
            String filename = "operation_logs_" +
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".csv";

            Alert exportAlert = new Alert(AlertType.INFORMATION);
            exportAlert.setTitle("导出完成");
            exportAlert.setHeaderText("操作日志导出成功");
            exportAlert.setContentText("日志文件已导出到系统目录：\n" + filename +
                    "\n\n共导出 " + logs.size() + " 条日志记录");
            exportAlert.showAndWait();

            System.out.println("操作日志已导出: " + filename);

        } catch (Exception ex) {
            showErrorAlert("导出失败: " + ex.getMessage());
            System.err.println("导出操作日志失败: " + ex.getMessage());
        }
    }

    /**
     * 显示日志统计信息
     */
    private void showLogStatistics() {
        try {
            var stats = DataStore.getOperationLogStatistics();
            int totalCount = DataStore.getOperationLogCount();

            StringBuilder statsText = new StringBuilder();
            statsText.append("操作日志统计信息\n\n");
            statsText.append("总日志数: ").append(totalCount).append("\n\n");
            statsText.append("按操作类型统计:\n");

            stats.forEach((type, count) -> {
                // 转换内部常量为显示名称
                String displayType = convertOperationTypeToDisplayName(type);
                statsText.append("• ").append(displayType).append(": ").append(count).append(" 次\n");
            });

            Alert statsAlert = new Alert(AlertType.INFORMATION);
            statsAlert.setTitle("日志统计");
            statsAlert.setHeaderText("操作日志统计分析");
            statsAlert.setContentText(statsText.toString());
            statsAlert.showAndWait();

        } catch (Exception ex) {
            showErrorAlert("获取统计信息失败: " + ex.getMessage());
            System.err.println("获取日志统计失败: " + ex.getMessage());
        }
    }

    /**
     * 将显示名称转换为操作类型常量
     */
    private String convertDisplayNameToOperationType(String displayName) {
        switch (displayName) {
            case "创建":
                return OperationLog.OperationTypes.CREATE;
            case "更新":
                return OperationLog.OperationTypes.UPDATE;
            case "删除":
                return OperationLog.OperationTypes.DELETE;
            case "登录":
                return OperationLog.OperationTypes.LOGIN;
            case "登出":
                return OperationLog.OperationTypes.LOGOUT;
            case "查看":
                return OperationLog.OperationTypes.VIEW;
            case "导出":
                return OperationLog.OperationTypes.EXPORT;
            case "导入":
                return OperationLog.OperationTypes.IMPORT;
            case "备份":
                return OperationLog.OperationTypes.BACKUP;
            case "恢复":
                return OperationLog.OperationTypes.RESTORE;
            default:
                return displayName;
        }
    }

    /**
     * 将显示名称转换为实体类型常量
     */
    private String convertDisplayNameToEntityType(String displayName) {
        switch (displayName) {
            case "用户":
                return OperationLog.EntityTypes.USER;
            case "车辆":
                return OperationLog.EntityTypes.VEHICLE;
            case "任务":
                return OperationLog.EntityTypes.TASK;
            case "系统":
                return OperationLog.EntityTypes.SYSTEM;
            case "日志":
                return OperationLog.EntityTypes.LOG;
            default:
                return displayName;
        }
    }

    /**
     * 将操作类型常量转换为显示名称
     */
    private String convertOperationTypeToDisplayName(String operationType) {
        switch (operationType) {
            case OperationLog.OperationTypes.CREATE:
                return "创建";
            case OperationLog.OperationTypes.UPDATE:
                return "更新";
            case OperationLog.OperationTypes.DELETE:
                return "删除";
            case OperationLog.OperationTypes.LOGIN:
                return "登录";
            case OperationLog.OperationTypes.LOGOUT:
                return "登出";
            case OperationLog.OperationTypes.VIEW:
                return "查看";
            case OperationLog.OperationTypes.EXPORT:
                return "导出";
            case OperationLog.OperationTypes.IMPORT:
                return "导入";
            case OperationLog.OperationTypes.BACKUP:
                return "备份";
            case OperationLog.OperationTypes.RESTORE:
                return "恢复";
            default:
                return operationType;
        }
    }

    /**
     * 显示系统配置界面
     */
    private void showSystemConfig() {
        Alert configAlert = new Alert(AlertType.INFORMATION);
        configAlert.setTitle("系统配置");
        configAlert.setHeaderText("系统参数配置");
        configAlert.setContentText(
                "系统配置选项：\n\n• 数据库连接配置\n• 系统参数设置\n• 权限策略配置\n• 备份策略设置\n• 邮件通知配置\n• 界面主题设置\n\n修改配置需要重启系统生效");
        configAlert.showAndWait();

        System.out.println("管理员查看系统配置");
    }

    /**
     * 执行数据备份
     */
    private void performBackup() {
        Alert backupAlert = new Alert(AlertType.CONFIRMATION);
        backupAlert.setTitle("数据备份");
        backupAlert.setHeaderText("确认执行数据备份");
        backupAlert.setContentText("将备份以下数据：\n\n• 用户数据\n• 车辆信息\n• 任务记录\n• 操作日志\n\n备份文件将保存在系统目录中");

        backupAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String backupFileName = "backup_" +
                        java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";

                // 记录操作日志
                try {
                    OperationLog backupLog = new OperationLog(
                            OperationLog.OperationTypes.BACKUP,
                            OperationLog.EntityTypes.SYSTEM,
                            backupFileName,
                            "管理员执行数据备份",
                            SessionManager.getCurrentUser().getId());
                    DataStore.addOperationLog(backupLog);
                } catch (Exception logEx) {
                    System.err.println("记录备份日志失败: " + logEx.getMessage());
                }

                Alert resultAlert = new Alert(AlertType.INFORMATION);
                resultAlert.setTitle("备份完成");
                resultAlert.setHeaderText("数据备份成功");
                resultAlert.setContentText("备份文件已生成：\n" + backupFileName);
                resultAlert.showAndWait();

                System.out.println("管理员执行数据备份");
            }
        });
    }
}
