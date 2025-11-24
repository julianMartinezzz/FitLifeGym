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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class PerfilController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;

    @FXML private PasswordField txtPassActual;
    @FXML private PasswordField txtPassNueva;
    @FXML private PasswordField txtPassConfirmar;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuarioLogueado;

    @FXML
    public void initialize() {
        // Cargar datos de la sesión
        usuarioLogueado = Sesion.getInstancia().getUsuarioActivo();

        if (usuarioLogueado != null) {
            lblUsuario.setText(usuarioLogueado.getUsername());
            lblRol.setText(usuarioLogueado.getRol());
        }
    }

    @FXML
    private void cambiarPassword() {
        String actual = txtPassActual.getText();
        String nueva = txtPassNueva.getText();
        String confirmar = txtPassConfirmar.getText();

        // 1. Validar Vacíos
        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            mostrarMensaje("Por favor complete todos los campos.", Color.RED);
            return;
        }

        // 2. Validar Contraseña Actual
        if (!actual.equals(usuarioLogueado.getPassword())) {
            mostrarMensaje("La contraseña actual es incorrecta.", Color.RED);
            return;
        }

        // 3. Validar Coincidencia
        if (!nueva.equals(confirmar)) {
            mostrarMensaje("Las nuevas contraseñas no coinciden.", Color.RED);
            return;
        }

        // 4. VALIDACIÓN DE SEGURIDAD (NUEVO)
        if (nueva.length() < 4) {
            mostrarMensaje("La contraseña debe tener al menos 4 caracteres.", Color.RED);
            return;
        }

        // Opcional: Evitar que la contraseña sea igual al usuario
        if (nueva.equals(usuarioLogueado.getUsername())) {
            mostrarMensaje("La contraseña no puede ser igual al usuario.", Color.RED);
            return;
        }

        // 5. Guardar
        if (usuarioDAO.cambiarPassword(usuarioLogueado.getUsername(), nueva)) {
            mostrarMensaje("¡Contraseña actualizada con éxito!", Color.GREEN);
            usuarioLogueado.setPassword(nueva); // Actualizar sesión
            limpiarCampos();
        } else {
            mostrarMensaje("Error al conectar con la base de datos.", Color.RED);
        }
    }




    private void mostrarMensaje(String msg, Color color) {
        lblMensaje.setText(msg);
        lblMensaje.setTextFill(color);
    }

    private void limpiarCampos() {
        txtPassActual.clear();
        txtPassNueva.clear();
        txtPassConfirmar.clear();
    }

    // --- NAVEGACIÓN ---
    @FXML public void volverMenu(ActionEvent e) { navegar(e, "MainView.fxml", "Inicio"); }

    @FXML
    public void salir(ActionEvent event) {
        Sesion.getInstancia().cerrarSesion();
        navegar(event, "LoginView.fxml", "Inicio de Sesión");
    }

    private void navegar(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Cambiar la escena
            stage.setScene(new Scene(root));

            // --- EL TRUCO PARA FORZAR PANTALLA COMPLETA ---
            stage.setMaximized(false); // Primero lo apagamos
            stage.setMaximized(true);  // Luego lo encendemos (esto fuerza el repintado)
            // -----------------------------------------------

            stage.setTitle("FitLife Gym - " + titulo);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}