package com.fitlife.controller;

import com.fitlife.dao.ClienteDAO;
import com.fitlife.model.Cliente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class ClienteController {

    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<String> comboPlan;
    @FXML private DatePicker dateInicio;
    @FXML private Label lblMensaje;

    private ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    public void initialize() {
        comboPlan.getItems().addAll("MENSUAL", "TRIMESTRAL", "ANUAL", "CLASE_UNICA");
        dateInicio.setValue(LocalDate.now());

        // Opcional: Forzar que el campo documento solo acepte números mientras escribes
        txtDocumento.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtDocumento.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void guardarCliente() {
        // 1. Obtener datos de los campos
        String doc = txtDocumento.getText();
        String nombre = txtNombre.getText();
        String correo = txtCorreo.getText();
        String tel = txtTelefono.getText();
        String dir = txtDireccion.getText(); // <--- ESTA ES LA LÍNEA QUE FALTABA
        String plan = comboPlan.getValue();
        LocalDate fecha = dateInicio.getValue();

        // 2. Validar Campos Vacíos Generales
        if (doc.isEmpty() || nombre.isEmpty() || correo.isEmpty() || plan == null) {
            mostrarError("Por favor llene los campos obligatorios (*).");
            return;
        }

        // 3. VALIDACIÓN DOCUMENTO (8-10 dígitos numéricos estricto)
        if (!doc.matches("\\d{8,10}")) {
            mostrarError("Cédula inválida: Debe tener entre 8 y 10 números, sin letras.");
            return;
        }

        // 4. VALIDACIÓN NOMBRE (Solo letras y espacios)
        if (!nombre.matches("^[a-zA-ZñÑáéíóúÁÉÍÓÚ ]+$")) {
            mostrarError("El nombre solo puede contener letras y espacios.");
            return;
        }

        // 5. VALIDACIÓN CORREO (Formato estricto)
        if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            mostrarError("Formato de correo inválido (ej: usuario@email.com).");
            return;
        }

        // 6. VALIDACIÓN TELÉFONO (Solo números)
        if (!tel.isEmpty() && !tel.matches("\\d+")) {
            mostrarError("El teléfono solo debe contener números.");
            return;
        }

        // 7. Crear Cliente
        Cliente nuevoCliente = new Cliente(doc, nombre, correo, tel, dir, plan, fecha);

        // 8. Guardar en BD
        if (clienteDAO.registrarCliente(nuevoCliente)) {
            lblMensaje.setTextFill(Color.GREEN);
            lblMensaje.setText("¡Cliente registrado exitosamente!");
            limpiarFormulario();
        } else {
            mostrarError("Error: El documento o correo ya existen.");
        }
    }

    private void mostrarError(String msg) {
        lblMensaje.setTextFill(Color.RED);
        lblMensaje.setText(msg);
    }

    private void limpiarFormulario() {
        txtDocumento.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        comboPlan.getSelectionModel().clearSelection();
        dateInicio.setValue(LocalDate.now());
    }

    // --- NAVEGACIÓN ---
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