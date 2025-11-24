package com.fitlife.controller;

import com.fitlife.dao.ClaseDAO;
import com.fitlife.model.Clase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class ReservaController {

    @FXML private TableView<Clase> tablaClases;
    @FXML private TableColumn<Clase, String> colClase;
    @FXML private TableColumn<Clase, String> colFecha; // Usaremos diaSemana por ahora
    @FXML private TableColumn<Clase, String> colHora;
    @FXML private TableColumn<Clase, String> colSala;
    @FXML private TableColumn<Clase, Integer> colCupos;
    @FXML private TableColumn<Clase, Void> colAccion;

    private ClaseDAO claseDAO = new ClaseDAO();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colClase.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("diaSemana"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        // Sala no está en el modelo, podemos agregarla o usar un valor fijo visualmente
        colSala.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "Sala A");
            }
        });
        colCupos.setCellValueFactory(new PropertyValueFactory<>("cupoMaximo"));

        // Botón de Acción (Ver detalles o Editar)
        agregarBotonAccion();
    }

    private void cargarDatos() {
        List<Clase> lista = claseDAO.listarClases();
        ObservableList<Clase> datos = FXCollections.observableArrayList(lista);
        tablaClases.setItems(datos);
    }

    private void agregarBotonAccion() {
        Callback<TableColumn<Clase, Void>, TableCell<Clase, Void>> cellFactory = (param) -> {
            final TableCell<Clase, Void> cell = new TableCell<>() {

                // Creamos el botón ELIMINAR
                private final Button btn = new Button("Eliminar");
                {
                    // Estilo Rojo (Peligro)
                    btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 5;");

                    // Acción al hacer clic
                    btn.setOnAction((ActionEvent event) -> {
                        // 1. Obtener la clase de la fila actual
                        Clase claseSeleccionada = getTableView().getItems().get(getIndex());

                        // 2. Llamar al DAO para borrar
                        boolean exito = claseDAO.eliminarClase(claseSeleccionada.getId());

                        if (exito) {
                            // 3. Si se borró, recargar la tabla para que desaparezca visualmente
                            cargarDatos();
                        } else {
                            // Mostrar error (probablemente porque hay alumnos inscritos)
                            System.out.println("No se puede eliminar: Hay reservas activas en esta clase.");
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
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

        colAccion.setCellFactory(cellFactory);
    }

    // --- NAVEGACIÓN DEL MENÚ ---
    @FXML
    public void volverMenu(ActionEvent event) { cambiarVista(event, "MainView.fxml", "Panel Principal"); }
    @FXML
    public void irAClases(ActionEvent event) { cambiarVista(event, "ClaseView.fxml", "Programación de Clases"); }
    @FXML
    public void irAPagos(ActionEvent event) { cambiarVista(event, "PagoView.fxml", "Gestión de Pagos"); }

    private void cambiarVista(ActionEvent event, String fxmlFile, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            Parent root = loader.load();

            // 1. Obtener el escenario actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 2. Cambiar la escena
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // 3. Título
            stage.setTitle("FitLife Gym - " + titulo);

            // 4. ¡ESTA ES LA CLAVE! Forzar maximizado SIEMPRE antes de mostrar
            stage.setMaximized(true);

            // 5. Mostrar
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAPerfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PerfilView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.setTitle("FitLife Gym - Mi Perfil");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}