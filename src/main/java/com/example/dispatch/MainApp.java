package com.example.dispatch;

import com.example.dispatch.util.DataStore;
import com.example.dispatch.view.LoginView;
import com.example.dispatch.view.MainView;
import com.example.dispatch.view.RegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static MainApp instance;
    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;

    public MainApp() {
        instance = this;
    }

    public static MainApp getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) {
        try {
            this.primaryStage = stage;
            System.out.println("正在启动车辆调度管理系统...");

            // 初始化数据库和数据存储
            DataStore.initialize();

            // 创建场景
            createScenes();

            stage.setTitle("车辆调度管理系统");
            stage.setScene(loginScene);

            // 程序关闭时清理资源
            stage.setOnCloseRequest(event -> {
                System.out.println("正在关闭系统...");
                DataStore.shutdown();
                System.out.println("系统已关闭");
            });

            stage.show();
            System.out.println("车辆调度管理系统启动完成");

        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            e.printStackTrace();

            // 显示错误对话框
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("启动失败");
            alert.setHeaderText("系统启动失败");
            alert.setContentText("错误信息: " + e.getMessage() + "\n\n请检查数据库配置。");
            alert.showAndWait();

            // 退出程序
            System.exit(1);
        }
    }

    /**
     * 创建所有场景
     */
    private void createScenes() {
        // 创建登录界面
        LoginView loginView = new LoginView();
        loginView.setOnLoginSuccess(this::showMainView);

        // 创建注册界面（在LoginView中处理）
        RegisterView registerView = new RegisterView();
        registerView.setOnRegisterSuccess(this::showMainView);

        // 创建场景
        loginScene = new Scene(loginView, 400, 300);
        mainScene = new Scene(createMainView(), 1000, 600);
    }

    /**
     * 创建主界面视图
     */
    private MainView createMainView() {
        MainView mainView = new MainView();
        return mainView;
    }

    /**
     * 显示主界面
     */
    private void showMainView() {
        // 重新创建主界面以确保显示最新用户信息
        mainScene = new Scene(createMainView(), 1000, 600);

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("车辆调度管理系统 - " + com.example.dispatch.util.SessionManager.getCurrentUsername());
        primaryStage.setMaximized(true); // 最大化主界面
    }

    /**
     * 返回登录界面（用于登出）
     */
    public static void showLoginView(Stage stage) {
        MainApp app = getInstance();
        if (app != null) {
            app.primaryStage = stage;
            app.showLoginScene();
        } else {
            // 如果实例不存在，创建新的
            MainApp newApp = new MainApp();
            newApp.primaryStage = stage;
            newApp.showLoginScene();
        }
    }

    /**
     * 显示登录场景
     */
    private void showLoginScene() {
        // 重新创建登录界面
        LoginView loginView = new LoginView();
        loginView.setOnLoginSuccess(this::showMainView);

        loginScene = new Scene(loginView, 400, 300);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("车辆调度管理系统");
        primaryStage.setMaximized(false);
        loginView.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
