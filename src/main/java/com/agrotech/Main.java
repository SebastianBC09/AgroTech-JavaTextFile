package com.agrotech;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    private static final String APP_TITLE = "AgroTech - Sistema de Riego";
    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        try {
            // Configurar la ventana principal
            setupMainWindow(stage);

            // Cargar y mostrar la vista inicial
            showInitialView(stage);

            // Configurar comportamiento al cerrar
            setupCloseHandler(stage);

        } catch (Exception e) {
            showErrorAndExit("Error al iniciar la aplicación", e);
        }
    }

    private void setupMainWindow(Stage stage) {
        // Configurar título y dimensiones mínimas
        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // Intentar cargar el ícono de la aplicación
        try {
            stage.getIcons().add(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))
            ));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el ícono de la aplicación: " + e.getMessage());
        }
    }

    private void showInitialView(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome-view.fxml"));
        Parent root = loader.load();

        // Aplicar estilos CSS si existen
        Scene scene = new Scene(root);
        String css = Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.show();
    }

    private void setupCloseHandler(Stage stage) {
        stage.setOnCloseRequest(event -> {
            // Limpiar recursos y realizar tareas de cierre
            cleanup();
            Platform.exit();
        });
    }

    private void cleanup() {
        try {
            // Aquí puedes agregar lógica de limpieza
            // Por ejemplo, cerrar conexiones, guardar estado, etc.
            System.out.println("Realizando limpieza antes de cerrar...");
        } catch (Exception e) {
            System.err.println("Error durante la limpieza: " + e.getMessage());
        }
    }

    private void showErrorAndExit(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Fatal");
        alert.setHeaderText(header);
        alert.setContentText("Detalles: " + e.getMessage());

        // Imprimir stack trace para debugging
        e.printStackTrace();

        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void stop() {
        // Este método se llama automáticamente al cerrar la aplicación
        System.out.println("Aplicación finalizada");
    }

    public static void main(String[] args) {
        // Configurar propiedades del sistema si es necesario
        System.setProperty("javafx.preloader", "com.agrotech.preloader.AppPreloader");

        // Iniciar la aplicación
        launch(args);
    }
}