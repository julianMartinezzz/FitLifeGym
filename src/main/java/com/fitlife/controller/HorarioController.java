package com.fitlife.controller;

import com.fitlife.dao.ClaseDAO;
import com.fitlife.dao.ClienteDAO;
import com.fitlife.dao.ReservaDAO;
import com.fitlife.model.Clase;
import com.fitlife.model.Reserva;
import com.fitlife.model.Sesion;
import com.fitlife.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HorarioController {

    @FXML private FlowPane contenedorTarjetas;

    // Campos de Filtros
    @FXML private ComboBox<String> filtroDia;
    @FXML private TextField filtroInstructor;
    @FXML private TextField filtroBuscar;
    @FXML private Button btnMisReservas;

    private ClaseDAO claseDAO = new ClaseDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();


    // Lista en memoria para filtrar rápido sin ir a la BD a cada rato
    private List<Clase> listaMaestra = new ArrayList<>();

    @FXML
    public void initialize() {
        // 1. LÓGICA DE SEGURIDAD (NUEVO)
        Usuario usuario = Sesion.getInstancia().getUsuarioActivo();
        if (usuario != null && !usuario.getRol().equals("CLIENTE")) {
            // Si NO es cliente (es Admin o Instructor), ocultamos "Mis Reservas"
            btnMisReservas.setVisible(false);
            btnMisReservas.setManaged(false); // Esto hace que el espacio se colapse
        }

        // 2. Cargar datos
        cargarDatosDesdeBD();

        // 3. Configurar Combo
        filtroDia.getItems().addAll("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");

        // 4. Listeners
        filtroBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroInstructor.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void cargarDatosDesdeBD() {
        listaMaestra = claseDAO.listarClases();
        renderizarTarjetas(listaMaestra);
    }

    // --- LÓGICA DE FILTRADO ---
    @FXML
    private void aplicarFiltros() {
        String busqueda = filtroBuscar.getText().toLowerCase();
        String diaSeleccionado = filtroDia.getValue();
        String instructorBusqueda = filtroInstructor.getText().toLowerCase();

        List<Clase> listaFiltrada = listaMaestra.stream()
                // Filtro 1: Nombre de la clase
                .filter(c -> c.getNombre().toLowerCase().contains(busqueda))
                // Filtro 2: Día (si hay uno seleccionado)
                .filter(c -> diaSeleccionado == null || c.getDiaSemana().equalsIgnoreCase(diaSeleccionado))
                // Filtro 3: Nombre del Instructor
                .filter(c -> instructorBusqueda.isEmpty() || c.getNombreInstructor().toLowerCase().contains(instructorBusqueda))
                .collect(Collectors.toList());

        renderizarTarjetas(listaFiltrada);
    }

    @FXML
    private void limpiarFiltros() {
        filtroBuscar.clear();
        filtroInstructor.clear();
        filtroDia.getSelectionModel().clearSelection();
        renderizarTarjetas(listaMaestra);
    }

    // --- RENDERIZADO DE TARJETAS ---
    private void renderizarTarjetas(List<Clase> clasesParaMostrar) {
        contenedorTarjetas.getChildren().clear();

        // ... manejo de lista vacía ...
        if (clasesParaMostrar.isEmpty()) {
            // ... (código para mostrar "No se encontraron clases") ...
            return;
        }

        for (Clase c : clasesParaMostrar) {
            // --- CÁLCULO DE CUPOS REAL ---
            int cuposDisponibles = c.getCuposDisponibles();
            // -----------------------------

            VBox card = new VBox(10);
            card.getStyleClass().add("class-card");

            // ... (Creación de Labels y Títulos) ...

            // Título
            Label lblTitulo = new Label(c.getNombre() + " — Nivel General");
            lblTitulo.getStyleClass().add("label-card-title");

            // Info Horario
            String textoInfo = c.getDiaSemana() + " • " + c.getHoraInicio() + " • Sala A";
            Label lblInfo = new Label(textoInfo);
            lblInfo.getStyleClass().add("label-card-info");

            // Info Instructor
            Label lblInstructor = new Label("Instructor: " + c.getNombreInstructor());
            lblInstructor.getStyleClass().add("label-card-info");

            // Botón de Reserva
            Button btnReservar = new Button("Reservar");
            btnReservar.getStyleClass().add("button-pill");
            btnReservar.setOnAction(e -> procesarReservaRapida(c.getId()));

            // --- LÓGICA DEL BADGE Y EL BOTÓN ---
            Label lblCupo = new Label("Cupos: " + cuposDisponibles + " / " + c.getCupoMaximo());

            if (cuposDisponibles > 5) {
                lblCupo.getStyleClass().add("badge-green"); // Más de 5, buen estado
            } else if (cuposDisponibles > 0) {
                lblCupo.getStyleClass().add("badge-orange"); // Pocos cupos (1 a 5)
            } else {
                lblCupo.setText("Sin Cupo");
                lblCupo.getStyleClass().add("badge-red");
                btnReservar.setDisable(true); // Deshabilita el botón si no hay cupo
            }
            // ------------------------------------

            // Footer
            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            footer.getChildren().addAll(lblCupo, spacer, btnReservar);
            card.getChildren().addAll(lblTitulo, lblInfo, lblInstructor, new Region(), footer);
            contenedorTarjetas.getChildren().add(card);
        }
    }

    // --- LÓGICA DE RESERVA ---
    private void procesarReservaRapida(int idClase) {
        // 1. Verificar sesión
        Usuario usuario = Sesion.getInstancia().getUsuarioActivo();
        if (usuario == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay sesión iniciada.");
            return;
        }

        // 2. Buscar ID de Cliente
        int idCliente = clienteDAO.obtenerIdPorCorreo(usuario.getUsername());
        if (idCliente == -1) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado",
                    "Tu usuario (" + usuario.getUsername() + ") no es un Cliente registrado.\nContacta al administrador.");
            return;
        }

        // 3. Intentar reservar
        Reserva nuevaReserva = new Reserva(idCliente, idClase);
        String resultado = reservaDAO.registrarReserva(nuevaReserva);

        // 4. Feedback
        if (resultado.equals("Exito")) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "¡Reserva Exitosa!", "Tu cupo ha sido reservado.");
            // Opcional: Recargar datos para ver si el cupo bajó (si implementas lógica de resta de cupos)
            // cargarDatosDesdeBD();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "No se pudo reservar", resultado);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- NAVEGACIÓN ---
    @FXML
    public void volverMenu(ActionEvent event) {
        navegar(event, "MainView.fxml", "Panel Principal");
    }

    @FXML
    public void irAMisReservas(ActionEvent event) {
        navegar(event, "MisReservasView.fxml", "Mis Reservas");
    }

    private void navegar(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.setTitle("FitLife Gym - " + titulo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}