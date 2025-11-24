package com.fitlife.controller;

import com.fitlife.dao.ClienteDAO;
import com.fitlife.dao.ReservaDAO;
import com.fitlife.model.ReservaDTO;
import com.fitlife.model.Sesion;
import com.fitlife.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class MisReservasController {

    @FXML private TableView<ReservaDTO> tablaActivas;
    @FXML private TableColumn<ReservaDTO, String> colClaseA, colFechaA, colHoraA, colSalaA, colAccionA;

    @FXML private TableView<ReservaDTO> tablaHistorial;
    @FXML private TableColumn<ReservaDTO, String> colClaseH, colFechaH, colHoraH, colEstadoH;

    private ReservaDAO reservaDAO = new ReservaDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private int idClienteActual = -1;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        // Tabla Activas
        colClaseA.setCellValueFactory(new PropertyValueFactory<>("nombreClase"));
        colFechaA.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHoraA.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colSalaA.setCellValueFactory(new PropertyValueFactory<>("sala"));

        // Lógica para poner el botón "Cancelar" en la tabla
        addBotonCancelar();

        // Tabla Historial
        colClaseH.setCellValueFactory(new PropertyValueFactory<>("nombreClase"));
        colFechaH.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHoraH.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colEstadoH.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void cargarDatos() {
        Usuario usuario = Sesion.getInstancia().getUsuarioActivo();
        if (usuario == null) return;

        this.idClienteActual = clienteDAO.obtenerIdPorCorreo(usuario.getUsername());
        if (idClienteActual == -1) return;

        List<ReservaDTO> todas = reservaDAO.listarReservasPorCliente(idClienteActual);

        ObservableList<ReservaDTO> activas = FXCollections.observableArrayList();
        ObservableList<ReservaDTO> historial = FXCollections.observableArrayList();

        for (ReservaDTO r : todas) {
            if (r.getEstado().equals("ACTIVA")) {
                activas.add(r);
            } else {
                historial.add(r);
            }
        }

        tablaActivas.setItems(activas);
        tablaHistorial.setItems(historial);
    }

    // Crea el botón rojo dentro de la tabla
    private void addBotonCancelar() {
        Callback<TableColumn<ReservaDTO, String>, TableCell<ReservaDTO, String>> cellFactory = (param) -> {
            final TableCell<ReservaDTO, String> cell = new TableCell<>() {
                private final Button btn = new Button("Cancelar");

                {
                    btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                    btn.setOnAction((ActionEvent event) -> {
                        ReservaDTO reserva = getTableView().getItems().get(getIndex());
                        cancelarReserva(reserva);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                    }
                }
            };
            return cell;
        };
        colAccionA.setCellFactory(cellFactory);
    }

    private void cancelarReserva(ReservaDTO reserva) {
        // Reutilizamos el método del DAO que ya creamos (cancelar por IDs)
        boolean exito = reservaDAO.cancelarReserva(idClienteActual, reserva.getIdClase());

        if (exito) {
            // Recargamos las tablas para que la reserva pase de "Activa" a "Historial"
            cargarDatos();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo cancelar.");
            alert.show();
        }
    }

    @FXML
    public void volverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.setTitle("FitLife Gym - Panel Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAHorarios(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/HorarioView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMaximized(true);
            stage.setTitle("FitLife Gym - Horarios");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAMisReservas(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/MisReservasView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("FitLife Gym - Mis Reservas");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}