package com.example.dispatch;

import com.example.dispatch.util.DataStore;
import com.example.dispatch.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("正在启动车辆调度管理系统...");

            // 初始化数据库和数据存储
            DataStore.initialize();

            // 创建主界面
            MainView root = new MainView();
            Scene scene = new Scene(root, 1000, 600);
            stage.setTitle("车辆调度管理系统");
            stage.setScene(scene);

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

    public static void main(String[] args) {
        launch(args);
    }
}
