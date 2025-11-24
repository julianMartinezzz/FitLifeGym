package com.fitlife.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SplashController {

    @FXML private ProgressBar barraProgreso;
    @FXML private Label lblEstado;

    @FXML
    public void initialize() {
        // Ejecutamos en un hilo secundario para la animación
        new Thread(() -> {
            try {
                // Hacemos un ciclo de 0 a 100 para que sea SUAVE
                for (int i = 0; i <= 100; i++) {
                    double progreso = i / 100.0;

                    // Actualizamos textos según el porcentaje para dar feedback
                    String mensaje = "Cargando...";
                    if (i < 30) mensaje = "Conectando con base de datos...";
                    else if (i < 60) mensaje = "Cargando perfiles de usuario...";
                    else if (i < 90) mensaje = "Iniciando interfaz gráfica...";
                    else mensaje = "¡Bienvenido!";

                    // Actualizamos la UI (Barra y Texto)
                    actualizarUI(progreso, mensaje);

                    // Pequeña pausa de 30ms entre cada % (30ms * 100 = 3 segundos aprox de carga)
                    Thread.sleep(30);
                }

                // Al llegar al 100%, abrimos el Login
                Platform.runLater(this::abrirLogin);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void actualizarUI(double progreso, String mensaje) {
        Platform.runLater(() -> {
            barraProgreso.setProgress(progreso);
            lblEstado.setText(mensaje);
        });
    }

    private void abrirLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("FitLife Gym - Inicio de Sesión");
            stage.setScene(new Scene(root));

            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo_fitlife.png")));
            } catch (Exception e) {}

            stage.setMaximized(true);
            stage.show();

            // Cerrar Splash
            ((Stage) lblEstado.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}