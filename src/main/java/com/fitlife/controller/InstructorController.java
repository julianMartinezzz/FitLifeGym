package com.fitlife.controller;

import com.fitlife.dao.InstructorDAO;
import com.fitlife.model.Instructor;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

public class InstructorController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEspecialidad;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono; // <--- NUEVO
    @FXML private Label lblMensaje;

    @FXML private TableView<Instructor> tablaInstructores;
    @FXML private TableColumn<Instructor, String> colNombre;
    @FXML private TableColumn<Instructor, String> colEspec;
    @FXML private TableColumn<Instructor, String> colEmail;   // <--- NUEVO VISUAL
    @FXML private TableColumn<Instructor, String> colTelefono; // <--- NUEVO VISUAL
    @FXML private TableColumn<Instructor, Boolean> colEstado;

    @FXML private Button btnGuardar;     // Para alternar visibilidad si quieres
    @FXML private Button btnActualizar;  // Para habilitar/deshabilitar

    private InstructorDAO instructorDAO = new InstructorDAO();
    private Instructor instructorSeleccionado = null; // Para saber a quién editamos

    @FXML
    public void initialize() {
        // Configurar columnas
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEspec.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));

        cargarTabla();

        // Listener: Cuando seleccionan una fila, subimos los datos al formulario
        tablaInstructores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDatosFormulario(newSelection);
            }
        });

        // Estado inicial botones
        btnActualizar.setDisable(true);
    }

    private void cargarTabla() {
        tablaInstructores.setItems(FXCollections.observableArrayList(instructorDAO.listarInstructores()));
    }

    private void cargarDatosFormulario(Instructor instructor) {
        this.instructorSeleccionado = instructor;

        txtNombre.setText(instructor.getNombre());
        txtEspecialidad.setText(instructor.getEspecialidad());
        txtEmail.setText(instructor.getEmail());
        txtTelefono.setText(instructor.getTelefono());

        // Bloqueamos email porque es el ID de usuario
        txtEmail.setDisable(true);

        // Habilitamos botón actualizar, deshabilitamos guardar (para no duplicar)
        btnActualizar.setDisable(false);
        btnGuardar.setDisable(true);
    }

    @FXML
    private void limpiarFormulario() {
        txtNombre.clear();
        txtEspecialidad.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtEmail.setDisable(false); // Volvemos a habilitar

        instructorSeleccionado = null;
        btnActualizar.setDisable(true);
        btnGuardar.setDisable(false);
        tablaInstructores.getSelectionModel().clearSelection();
    }

    @FXML
    private void guardarInstructor() {
        String nombre = txtNombre.getText();
        String especialidad = txtEspecialidad.getText();
        String email = txtEmail.getText();
        String telefono = txtTelefono.getText();

        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            mostrarMensaje("Complete todos los campos obligatorios.", Color.RED);
            return;
        }
        if (!nombre.matches("^[a-zA-ZñÑáéíóúÁÉÍÓÚ ]+$")) {
            mostrarMensaje("Nombre inválido (sin números).", Color.RED);
            return;
        }
        // (Agrega tus validaciones de email aquí)

        if (instructorDAO.registrarInstructorCompleto(nombre, especialidad, email, telefono)) {
            mostrarMensaje("¡Instructor registrado!", Color.GREEN);
            limpiarFormulario();
            cargarTabla();
        } else {
            mostrarMensaje("Error: Verifique si el correo ya existe.", Color.RED);
        }
    }

    @FXML
    private void actualizarInstructor() {
        if (instructorSeleccionado == null) return;

        // Solo actualizamos los campos permitidos
        instructorSeleccionado.setNombre(txtNombre.getText());
        instructorSeleccionado.setEspecialidad(txtEspecialidad.getText());
        instructorSeleccionado.setTelefono(txtTelefono.getText());

        if (instructorDAO.actualizarInstructor(instructorSeleccionado)) {
            mostrarMensaje("Datos actualizados correctamente.", Color.GREEN);
            limpiarFormulario();
            cargarTabla();
        } else {
            mostrarMensaje("Error al actualizar en BD.", Color.RED);
        }
    }

    // Reutilizamos el toggle de antes
    @FXML
    public void toggleEstado() {
        // ... (Mismo código que tenías en la respuesta anterior) ...
        // Copia el método toggleEstado de la respuesta anterior aquí
        // Asegúrate de llamar a cargarTabla() al final.
    }

    private void mostrarMensaje(String msg, Color color) {
        lblMensaje.setTextFill(color);
        lblMensaje.setText(msg);
    }

    @FXML public void volverMenu(ActionEvent e) { navegar(e, "MainView.fxml"); }

    private void navegar(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
        } catch (IOException e) { e.printStackTrace(); }
    }
}