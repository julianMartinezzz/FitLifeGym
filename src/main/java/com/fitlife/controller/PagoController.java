package com.fitlife.controller;

import com.fitlife.dao.ClienteDAO;
import com.fitlife.dao.PagoDAO;
import com.fitlife.model.Cliente;
import com.fitlife.model.Pago;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class PagoController {

    @FXML private TextField txtDocumento;
    @FXML private Label lblNombreCliente;

    // 1. CAMBIO: Ya no usamos TextField txtMonto, ahora usamos ComboBox
    @FXML private ComboBox<String> comboPlanPago;

    @FXML private ComboBox<String> comboMetodo;
    @FXML private TextField txtConcepto;
    @FXML private Label lblMensaje;

    private PagoDAO pagoDAO = new PagoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();

    private int idClienteEncontrado = -1;

    @FXML
    public void initialize() {
        // Llenar métodos de pago
        comboMetodo.getItems().addAll("Efectivo", "Tarjeta de Crédito", "Tarjeta de Débito", "Transferencia");

        // 2. CAMBIO: Llenar los planes con sus precios fijos (Igual que en el cliente)
        comboPlanPago.getItems().addAll(
                "Mensualidad - $80.000",
                "Trimestre - $220.000",
                "Anual - $800.000",
                "Clase Única / Visita - $15.000" // Opcional: Agregué este por si acaso
        );

        // Opcional: Auto-rellenar concepto al seleccionar plan
        comboPlanPago.setOnAction(e -> {
            if(comboPlanPago.getValue() != null) {
                // Pone el nombre del plan en el concepto automáticamente
                txtConcepto.setText("Pago: " + comboPlanPago.getValue());
            }
        });
    }

    @FXML
    private void buscarCliente() {
        String doc = txtDocumento.getText();

        if (doc.isEmpty()) {
            mostrarError("Por favor ingrese un documento.");
            return;
        }
        if (!doc.matches("\\d+")) {
            mostrarError("El documento solo puede contener números.");
            return;
        }

        Cliente cliente = clienteDAO.buscarPorDocumento(doc);

        if (cliente != null) {
            idClienteEncontrado = cliente.getId();
            lblNombreCliente.setText(cliente.getNombre());
            lblNombreCliente.setTextFill(Color.web("#27ae60"));
            lblMensaje.setText("");
        } else {
            idClienteEncontrado = -1;
            lblNombreCliente.setText("Cliente no encontrado");
            lblNombreCliente.setTextFill(Color.RED);
        }
    }

    @FXML
    private void guardarPago() {
        if (idClienteEncontrado == -1) {
            mostrarError("Primero busque y seleccione un cliente válido.");
            return;
        }

        // 3. CAMBIO: Obtener datos del ComboBox
        String planSeleccionado = comboPlanPago.getValue();
        String metodo = comboMetodo.getValue();
        String concepto = txtConcepto.getText();

        if (planSeleccionado == null || metodo == null || concepto.isEmpty()) {
            mostrarError("Seleccione el Plan, Método de Pago y verifique el Concepto.");
            return;
        }

        // VALIDACIÓN ANTI-DUPLICADOS (Si es mensualidad)
        if (planSeleccionado.contains("Mensualidad") || planSeleccionado.contains("Trimestre")) {
            if (pagoDAO.existePagoMensualidad(idClienteEncontrado)) {
                mostrarError("BLOQUEADO: Este cliente ya tiene un pago registrado para este mes.");
                return;
            }
        }

        // 4. CAMBIO: Lógica para extraer el precio del texto seleccionado
        double monto = 0;
        if (planSeleccionado.contains("80.000")) monto = 80000;
        else if (planSeleccionado.contains("220.000")) monto = 220000;
        else if (planSeleccionado.contains("800.000")) monto = 800000;
        else if (planSeleccionado.contains("15.000")) monto = 15000;

        // Crear y Guardar
        try {
            Pago pago = new Pago(idClienteEncontrado, monto, metodo, concepto);

            if (pagoDAO.registrarPago(pago)) {
                lblMensaje.setTextFill(Color.GREEN);
                lblMensaje.setText("¡Pago registrado exitosamente!");
                limpiarFormulario();
            } else {
                mostrarError("Error al guardar en la base de datos.");
            }

        } catch (Exception e) {
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }

    private void mostrarError(String msg) {
        lblMensaje.setTextFill(Color.RED);
        lblMensaje.setText(msg);
    }

    private void limpiarFormulario() {
        comboPlanPago.getSelectionModel().clearSelection(); // Limpiar plan
        txtConcepto.clear();
        comboMetodo.getSelectionModel().clearSelection();
        // No limpiamos cliente para permitir pagos seguidos si es necesario, o puedes limpiar:
        // txtDocumento.clear(); lblNombreCliente.setText("---"); idClienteEncontrado = -1;
    }

    // --- NAVEGACIÓN ---
    @FXML public void volverMenu(ActionEvent e) { navegar(e, "MainView.fxml", "Inicio"); }
    @FXML public void irAClases(ActionEvent e) { navegar(e, "ClaseView.fxml", "Clases"); }
    @FXML public void irAReservas(ActionEvent e) { navegar(e, "ReservaView.fxml", "Reservas"); }

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