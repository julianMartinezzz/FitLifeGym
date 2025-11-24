package com.fitlife.controller;

import com.fitlife.dao.ClienteDAO;
import com.fitlife.dao.PagoDAO;
import com.fitlife.model.Pago;
import com.fitlife.model.Sesion;
import com.fitlife.model.Usuario;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MisPagosController {

    @FXML private Label lblEstadoPlan;
    @FXML private Label lblVencimiento;
    @FXML private ComboBox<String> comboPlanes;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, String> colFecha;
    @FXML private TableColumn<Pago, String> colConcepto;
    @FXML private TableColumn<Pago, Double> colMonto;

    private PagoDAO pagoDAO = new PagoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private int idClienteActual = -1;

    @FXML
    public void initialize() {
        // Configurar Tabla
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        // Llenar Combo
        comboPlanes.getItems().addAll(
                "Plan Mensual - $80.000",
                "Plan Trimestral - $220.000",
                "Plan Anual - $800.000"
        );

        cargarDatosCliente();
    }

    private void cargarDatosCliente() {
        Usuario usuario = Sesion.getInstancia().getUsuarioActivo();
        if (usuario == null) return;

        idClienteActual = clienteDAO.obtenerIdPorCorreo(usuario.getUsername());
        if (idClienteActual == -1) return;

        // 1. Cargar Vencimiento
        LocalDate vencimiento = clienteDAO.obtenerVencimiento(idClienteActual);
        if (vencimiento != null) {
            lblVencimiento.setText(vencimiento.toString());

            if (vencimiento.isBefore(LocalDate.now())) {
                lblEstadoPlan.setText("VENCIDO");
                lblEstadoPlan.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-font-weight: bold;");
            } else {
                lblEstadoPlan.setText("ACTIVO");
                lblEstadoPlan.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
        } else {
            lblVencimiento.setText("Sin datos");
        }

        // 2. Cargar Historial
        List<Pago> historial = pagoDAO.listarPagosPorCliente(idClienteActual);
        tablaPagos.setItems(FXCollections.observableArrayList(historial));
    }

    @FXML
    private void realizarPago() {
        String planSeleccionado = comboPlanes.getValue();

        // 1. Validar que haya seleccionado un plan
        if (planSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un plan para pagar.");
            return;
        }

        // 2. VALIDACIÓN ANTI-DUPLICADOS (Nuevo)
        // Verificamos si ya pagó este mes para no cobrarle doble
        if (pagoDAO.existePagoMensualidad(idClienteActual)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Pago Rechazado. Ya tienes una membresía activa pagada este mes.");
            return;
        }

        // 3. Extraer monto (Declaramos la variable UNA SOLA VEZ aquí)
        double monto = 0;
        if (planSeleccionado.contains("80.000")) monto = 80000;
        else if (planSeleccionado.contains("220.000")) monto = 220000;
        else if (planSeleccionado.contains("800.000")) monto = 800000;

        // 4. Crear el objeto Pago
        Pago nuevoPago = new Pago(
                idClienteActual,
                monto,
                "Plataforma Online", // Método de pago fijo para web
                "Renovación Web: " + planSeleccionado.split("-")[0]
        );

        // 5. Guardar en Base de Datos
        if (pagoDAO.registrarPago(nuevoPago)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "¡Pago Exitoso! Su membresía ha sido renovada.");
            cargarDatosCliente(); // Refrescar la pantalla
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al procesar el pago.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Pagos");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- NAVEGACIÓN ---
    @FXML public void volverMenu(ActionEvent e) { navegar(e, "MainView.fxml", "Inicio"); }
    @FXML public void irAClases(ActionEvent e) { navegar(e, "HorarioView.fxml", "Clases"); }
    @FXML public void irAMisReservas(ActionEvent e) { navegar(e, "MisReservasView.fxml", "Mis Reservas"); }

    private void navegar(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.setTitle("FitLife Gym - " + titulo);
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}