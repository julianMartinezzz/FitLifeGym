package com.fitlife.controller;

import com.fitlife.dao.ClienteDAO;
import com.fitlife.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import javafx.scene.control.Alert;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import java.io.File;

public class ReporteController {

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colDocumento;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colPlan;
    @FXML private TableColumn<Cliente, Boolean> colEstado;

    private ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    public void initialize() {
        // Configurar las columnas para que sepan qué atributo leer de la clase Cliente
        // Los nombres en comillas deben coincidir EXACTAMENTE con los atributos de Cliente.java (ej: "nombre")
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPlan.setCellValueFactory(new PropertyValueFactory<>("plan"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));

        cargarDatos();
    }

    @FXML
    public void cargarDatos() {
        // 1. Traer la lista de la BD
        List<Cliente> lista = clienteDAO.listarClientes();

        // 2. Convertirla a formato JavaFX (ObservableList)
        ObservableList<Cliente> datosTabla = FXCollections.observableArrayList(lista);

        // 3. Ponerla en la tabla
        tablaClientes.setItems(datosTabla);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleEstado() {
        // 1. Obtener el cliente seleccionado en la tabla
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Por favor seleccione un cliente de la tabla.");
            return;
        }

        // 2. Calcular el nuevo estado (Lo contrario al actual)
        boolean nuevoEstado = !seleccionado.isActivo();

        // 3. Llamar al DAO
        if (clienteDAO.cambiarEstadoCliente(seleccionado.getId(), nuevoEstado)) {
            mostrarAlerta("Estado actualizado correctamente.");
            cargarDatos(); // Refrescar la tabla para ver el cambio
        } else {
            mostrarAlerta("Error al actualizar en base de datos.");
        }
    }

    // Método auxiliar para mensajes rápidos
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    @FXML
    public void exportarPDF() {
        // 1. Elegir dónde guardar el archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName("Reporte_Clientes_" + java.time.LocalDate.now() + ".pdf");

        // Obtener la ventana actual para mostrar el diálogo
        File file = fileChooser.showSaveDialog(tablaClientes.getScene().getWindow());

        if (file != null) {
            try {
                // 2. Crear el documento PDF
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // 3. Agregar Título
                Paragraph titulo = new Paragraph("Reporte de Clientes - FitLife Gym");
                titulo.setAlignment(Paragraph.ALIGN_CENTER);
                titulo.setSpacingAfter(20);
                document.add(titulo);

                // 4. Crear Tabla PDF (5 columnas)
                PdfPTable pdfTable = new PdfPTable(5);
                pdfTable.setWidthPercentage(100);

                // Encabezados
                String[] headers = {"ID", "Documento", "Nombre", "Membresía", "Activo"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header));
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                    pdfTable.addCell(cell);
                }

                // 5. Llenar con datos de la Tabla JavaFX
                for (Cliente c : tablaClientes.getItems()) {
                    pdfTable.addCell(String.valueOf(c.getId()));
                    pdfTable.addCell(c.getDocumento());
                    pdfTable.addCell(c.getNombre());
                    pdfTable.addCell(c.getPlan());
                    pdfTable.addCell(c.isActivo() ? "Sí" : "No");
                }

                document.add(pdfTable);
                document.close();

                mostrarAlerta("Reporte generado exitosamente.");

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error al generar el PDF: " + e.getMessage());
            }
        }
    }

}