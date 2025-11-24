package com.fitlife.controller;

import com.fitlife.dao.UsuarioDAO;
import com.fitlife.model.Sesion;
import com.fitlife.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void ingresar(ActionEvent event) {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Por favor ingrese usuario y contraseña.");
            return;
        }

        Usuario usuarioLogueado = usuarioDAO.validarLogin(user, pass);

        if (usuarioLogueado != null) {
            Sesion.getInstancia().setUsuarioActivo(usuarioLogueado);
            irAlMenuPrincipal(event);
        } else {
            lblMensaje.setText("Usuario o contraseña incorrectos.");
        }
    }

    private void irAlMenuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent root = loader.load();

            // 1. Obtener el escenario actual (la ventana del Login)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 2. Cambiar la escena al Menú
            stage.setScene(new Scene(root));
            stage.setTitle("FitLife Gym - Panel Principal");

            // --- EL ARREGLO DEFINITIVO ---
            // Borramos stage.centerOnScreen(); porque eso la achica.

            // Forzamos el redibujado del marco de la ventana:
            stage.setMaximized(false);
            stage.setMaximized(true);

            stage.show();
            // -----------------------------

        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error crítico al cargar el menú.");
        }
    }

    // Método irAPerfil por si acaso copiaste el botón de perfil en el Login (aunque no suele ir ahí)
    @FXML
    public void irAPerfil(ActionEvent event) {
        // Dejo este método vacío o genérico por si el FXML lo pide,
        // aunque en el Login no suele haber botón de perfil.
    }
}