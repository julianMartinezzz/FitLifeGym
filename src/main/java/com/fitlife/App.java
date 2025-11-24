package com.fitlife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image; // <--- IMPORTANTE
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // CAMBIO: Cargamos SplashView en lugar de LoginView
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/view/SplashView.fxml"));
        Parent root = fxmlLoader.load();

        // Estilo sin bordes (transparente/sin botones de cerrar)
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Icono (Opcional en el splash, pero buena práctica)
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo_fitlife.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {}

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}