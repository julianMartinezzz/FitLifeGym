package com.fitlife.controller;

import com.fitlife.dao.ClaseDAO;
import com.fitlife.dao.InstructorDAO;
import com.fitlife.dao.ReservaDAO;
import com.fitlife.model.AsistenteDTO; // <--- Usamos el nuevo DTO
import com.fitlife.model.Clase;
import com.fitlife.model.Sesion;
import com.fitlife.model.Usuario;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell; // <--- Importante
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class InstructorClasesController {

    @FXML private TableView<Clase> tablaClases;
    @FXML private TableColumn<Clase, String> colClase;
    @FXML private TableColumn<Clase, String> colDia;
    @FXML private TableColumn<Clase, String> colHora;
    @FXML private TableColumn<Clase, Integer> colReservas;

    // Cambiamos Cliente por AsistenteDTO
    @FXML private TableView<AsistenteDTO> tablaAsistentes;
    @FXML private TableColumn<AsistenteDTO, String> colAlumno;
    @FXML private TableColumn<AsistenteDTO, String> colDocumento;
    // Nueva columna Checkbox
    @FXML private TableColumn<AsistenteDTO, Boolean> colAsistencia;

    @FXML private Label lblClaseSeleccionada;

    private ClaseDAO claseDAO = new ClaseDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();
    private InstructorDAO instructorDAO = new InstructorDAO();

    private int idInstructorActual = -1;

    @FXML
    public void initialize() {
        // 1. Configurar Columnas Clases
        colClase.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDia.setCellValueFactory(new PropertyValueFactory<>("diaSemana"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        colReservas.setCellValueFactory(new PropertyValueFactory<>("reservasActivas"));

        // 2. Configurar Columnas Asistentes
        colAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));

        // --- CONFIGURACIÓN DEL CHECKBOX DE ASISTENCIA ---
        colAsistencia.setCellValueFactory(cellData -> cellData.getValue().asistioProperty());
        colAsistencia.setCellFactory(CheckBoxTableCell.forTableColumn(colAsistencia));

        // IMPORTANTE: Hacer la tabla editable para que el click funcione
        tablaAsistentes.setEditable(true);

        // Listener: Cuando cambie el checkbox, actualizar BD
        colAsistencia.setOnEditCommit(event -> {
            AsistenteDTO asistente = event.getRowValue();
            boolean nuevoEstado = event.getNewValue();
            // Guardar en BD
            reservaDAO.marcarAsistencia(asistente.getIdReserva(), nuevoEstado);
        });
        // ------------------------------------------------

        // 3. Cargar Instructor
        Usuario usuario = Sesion.getInstancia().getUsuarioActivo();
        if (usuario != null) {
            this.idInstructorActual = instructorDAO.obtenerIdPorEmail(usuario.getUsername());
            cargarClases();
        }

        // 4. Listener de Selección de Clase
        tablaClases.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarAsistentes(newVal);
            }
        });
    }

    private void cargarClases() {
        if (idInstructorActual != -1) {
            List<Clase> misClases = claseDAO.listarClasesPorInstructor(idInstructorActual);
            tablaClases.setItems(FXCollections.observableArrayList(misClases));
        }
    }

    private void cargarAsistentes(Clase clase) {
        lblClaseSeleccionada.setText("Clase: " + clase.getNombre() + " - Marcando Asistencia");
        List<AsistenteDTO> alumnos = reservaDAO.obtenerAsistentesPorClase(clase.getId());
        tablaAsistentes.setItems(FXCollections.observableArrayList(alumnos));
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