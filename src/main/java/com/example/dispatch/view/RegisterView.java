package com.example.dispatch.view;

import com.example.dispatch.model.User;
import com.example.dispatch.service.AuthService;
import com.example.dispatch.util.SessionManager;
import com.example.dispatch.util.UserRoles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class RegisterView extends VBox {
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();
    private final ComboBox<String> roleComboBox = new ComboBox<>();
    private final Button registerButton = new Button("注册");
    private final Button backButton = new Button("返回登录");
    private final Label statusLabel = new Label();

    private final AuthService authService = new AuthService();

    public RegisterView() {
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setPrefWidth(350);

        // 标题
        Label titleLabel = new Label("用户注册");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 创建注册表单
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
        passwordField.setPromptText("请输入密码（至少6位）");
        passwordField.setPrefWidth(200);

        // 确认密码
        Label confirmPasswordLabel = new Label("确认密码:");
        confirmPasswordField.setPromptText("请再次输入密码");
        confirmPasswordField.setPrefWidth(200);

        // 角色选择
        Label roleLabel = new Label("角色:");
        roleComboBox.getItems().addAll(UserRoles.getAllRoleDisplayNames());
        roleComboBox.setValue(UserRoles.CUSTOMER_DISPLAY); // 默认选择客户
        roleComboBox.setPrefWidth(200);

        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(passwordLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);
        formGrid.add(confirmPasswordLabel, 0, 2);
        formGrid.add(confirmPasswordField, 1, 2);
        formGrid.add(roleLabel, 0, 3);
        formGrid.add(roleComboBox, 1, 3);

        // 按钮行
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        registerButton.setPrefWidth(80);
        backButton.setPrefWidth(80);
        buttonBox.getChildren().addAll(registerButton, backButton);

        // 状态标签
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setWrapText(true);

        getChildren().addAll(titleLabel, formGrid, buttonBox, statusLabel);

        // 设置事件处理器
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // 注册按钮事件
        registerButton.setOnAction(e -> handleRegister());

        // 返回登录按钮事件
        backButton.setOnAction(e -> handleBackToLogin());

        // 回车键注册
        confirmPasswordField.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String roleDisplayName = roleComboBox.getValue();

        // 将显示名称转换为内部角色代码
        String role = convertDisplayNameToRole(roleDisplayName);

        // 验证输入
        if (username.isEmpty()) {
            showError("请输入用户名");
            return;
        }
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        if (password.length() < 6) {
            showError("密码长度至少为6位");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            return;
        }
        if (role == null) {
            showError("请选择有效的角色");
            return;
        }

        try {
            registerButton.setDisable(true);
            statusLabel.setText("正在注册...");

            User newUser = authService.register(username, password, role);

            // 注册成功后自动登录
            SessionManager.setCurrentUser(newUser);
            showSuccess("注册成功！欢迎 " + newUser.getUsername());

            // 在JavaFX Application Thread中执行界面切换
            javafx.application.Platform.runLater(this::onRegisterSuccess);

        } catch (SQLException ex) {
            showError("数据库错误: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } finally {
            registerButton.setDisable(false);
        }
    }

    private void handleBackToLogin() {
        // 返回登录界面，并设置回调
        LoginView loginView = new LoginView();
        loginView.setOnLoginSuccess(onRegisterSuccessCallback);
        getScene().setRoot(loginView);
        loginView.requestFocus();
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(message);
    }

    // 注册成功回调，由MainApp设置
    private Runnable onRegisterSuccessCallback;

    public void setOnRegisterSuccess(Runnable callback) {
        this.onRegisterSuccessCallback = callback;
    }

    private void onRegisterSuccess() {
        if (onRegisterSuccessCallback != null) {
            onRegisterSuccessCallback.run();
        }
    }

    // 获取焦点
    public void requestFocus() {
        usernameField.requestFocus();
    }

    /**
     * 将角色显示名称转换为内部角色代码
     */
    private String convertDisplayNameToRole(String displayName) {
        if (UserRoles.CUSTOMER_DISPLAY.equals(displayName)) {
            return UserRoles.CUSTOMER;
        } else if (UserRoles.DRIVER_DISPLAY.equals(displayName)) {
            return UserRoles.DRIVER;
        } else if (UserRoles.DISPATCHER_DISPLAY.equals(displayName)) {
            return UserRoles.DISPATCHER;
        } else if (UserRoles.ADMIN_DISPLAY.equals(displayName)) {
            return UserRoles.ADMIN;
        }
        return null; // 无效的角色显示名称
    }
}