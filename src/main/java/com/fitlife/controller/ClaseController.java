package com.fitlife.controller;

import com.fitlife.dao.ClaseDAO;
import com.fitlife.dao.InstructorDAO;
import com.fitlife.model.Clase;
import com.fitlife.model.Instructor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ClaseController {

    @FXML private ComboBox<String> comboNombre;
    @FXML private ComboBox<String> comboDia;
    @FXML private ComboBox<Instructor> comboInstructor;
    @FXML private TextField txtHora;
    @FXML private TextField txtCupo;
    @FXML private Label lblMensaje;

    private ClaseDAO claseDAO = new ClaseDAO();
    private InstructorDAO instructorDAO = new InstructorDAO();

    @FXML
    public void initialize() {
        // Cargar listas
        comboNombre.getItems().addAll("Spinning", "Yoga", "Pilates", "Funcional", "Zumba", "Boxeo");
        comboDia.getItems().addAll("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");
        comboInstructor.getItems().addAll(instructorDAO.listarInstructores());
    }

    @FXML
    private void guardarClase() {
        // 1. Validar campos vacíos
        if (comboNombre.getValue() == null || comboDia.getValue() == null ||
                comboInstructor.getValue() == null || txtHora.getText().isEmpty() || txtCupo.getText().isEmpty()) {

            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        try {
            // 2. Validar Hora (Formato HH:MM)
            LocalTime hora;
            try {
                hora = LocalTime.parse(txtHora.getText());
            } catch (DateTimeParseException e) {
                mostrarError("Hora inválida. Use el formato HH:MM (ej: 08:30).");
                return;
            }

            // 3. Validar Cupo (Solo números)
            int cupo;
            try {
                cupo = Integer.parseInt(txtCupo.getText());
                if (cupo <= 0) {
                    mostrarError("El cupo debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El cupo debe ser un número válido.");
                return;
            }

            // 4. Crear objeto
            int idInstructor = comboInstructor.getValue().getId();
            Clase nuevaClase = new Clase(
                    comboNombre.getValue(),
                    idInstructor,
                    comboDia.getValue(),
                    hora,
                    cupo
            );

            // 5. Guardar en BD
            if (claseDAO.registrarClase(nuevaClase)) {
                lblMensaje.setTextFill(Color.GREEN);
                lblMensaje.setText("¡Clase programada correctamente!");
                limpiarCampos();
            } else {
                mostrarError("Error al conectar con la base de datos.");
            }

        } catch (Exception e) {
            mostrarError("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setTextFill(Color.RED);
        lblMensaje.setText(mensaje);
    }

    private void limpiarCampos() {
        txtHora.clear();
        txtCupo.clear();
        comboNombre.getSelectionModel().clearSelection();
        comboDia.getSelectionModel().clearSelection();
        comboInstructor.getSelectionModel().clearSelection();
    }

    // --- NAVEGACIÓN (Sidebar) ---
    @FXML public void volverMenu(ActionEvent e) { navegar(e, "MainView.fxml", "Inicio"); }
    @FXML public void irAPerfil(ActionEvent e) { navegar(e, "PerfilView.fxml", "Mi Perfil"); }

    private void navegar(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.setTitle("FitLife Gym - " + titulo);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}