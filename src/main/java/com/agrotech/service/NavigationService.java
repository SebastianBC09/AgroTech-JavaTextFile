package com.agrotech.service;

import com.agrotech.controller.DashboardController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationService {
    private static NavigationService instance;

    private NavigationService() {}

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public void navigateToDashboard(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
        Parent dashboardView = loader.load();

        // Obtener el controlador
        DashboardController dashboardController = loader.getController();
        // Inicializar datos
        dashboardController.initializeWithData();

        Scene scene = new Scene(dashboardView);
        stage.setScene(scene);
        stage.show();
    }

    public void navigateToWelcome(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome-view.fxml"));
        Parent welcomeView = loader.load();

        Scene scene = new Scene(welcomeView);
        stage.setScene(scene);
        stage.show();
    }
}