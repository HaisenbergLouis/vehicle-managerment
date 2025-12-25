package com.example.dispatch.view;

import com.example.dispatch.model.OperationLog;
import com.example.dispatch.model.User;
import com.example.dispatch.service.AuthService;
import com.example.dispatch.util.DataStore;
import com.example.dispatch.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class LoginView extends VBox {
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("登录");
    private final Button registerButton = new Button("注册");
    private final Label statusLabel = new Label();

    private final AuthService authService = new AuthService();

    public LoginView() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setPrefWidth(350);

        // 标题
        Label titleLabel = new Label("车辆调度管理系统");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 创建登录表单
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // 用户名
        Label usernameLabel = new Label("用户名:");
        usernameField.setPromptText("请输入用户名");
        usernameField.setPrefWidth(200);

        // 密码
        Label passwordLabel = new Label("密码:");
        passwordField.setPromptText("请输入密码");
        passwordField.setPrefWidth(200);

        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(passwordLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);

        // 按钮行
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        loginButton.setPrefWidth(80);
        registerButton.setPrefWidth(80);
        buttonBox.getChildren().addAll(loginButton, registerButton);

        // 状态标签
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setWrapText(true);

        getChildren().addAll(titleLabel, formGrid, buttonBox, statusLabel);

        // 设置事件处理器
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // 登录按钮事件
        loginButton.setOnAction(e -> handleLogin());

        // 注册按钮事件
        registerButton.setOnAction(e -> handleRegister());

        // 回车键登录
        passwordField.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("请输入用户名和密码");
            return;
        }

        try {
            loginButton.setDisable(true);
            statusLabel.setText("正在登录...");

            User user = authService.login(username, password);

            if (user != null) {
                // 登录成功
                SessionManager.setCurrentUser(user);

                // 记录登录日志
                try {
                    OperationLog loginLog = new OperationLog(
                            OperationLog.OperationTypes.LOGIN,
                            OperationLog.EntityTypes.USER,
                            user.getUsername(),
                            "用户登录系统: " + user.getUsername() + " (角色: " +
                                    com.example.dispatch.util.UserRoles.getRoleDisplayName(user.getRole()) + ")",
                            user.getId());
                    DataStore.addOperationLog(loginLog);
                } catch (Exception logEx) {
                    System.err.println("记录登录日志失败: " + logEx.getMessage());
                }

                showSuccess("登录成功！欢迎 " + user.getUsername());

                // 在JavaFX Application Thread中执行界面切换
                javafx.application.Platform.runLater(this::onLoginSuccess);
            } else {
                showError("用户名或密码错误");
            }

        } catch (SQLException ex) {
            showError("数据库错误: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void handleRegister() {
        // 切换到注册界面，并设置回调
        RegisterView registerView = new RegisterView();
        registerView.setOnRegisterSuccess(onLoginSuccessCallback);
        getScene().setRoot(registerView);
        registerView.requestFocus();
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(message);
    }

    // 登录成功回调，由MainApp设置
    private Runnable onLoginSuccessCallback;

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccessCallback = callback;
    }

    private void onLoginSuccess() {
        if (onLoginSuccessCallback != null) {
            onLoginSuccessCallback.run();
        }
    }

    // 获取焦点
    public void requestFocus() {
        usernameField.requestFocus();
    }
}