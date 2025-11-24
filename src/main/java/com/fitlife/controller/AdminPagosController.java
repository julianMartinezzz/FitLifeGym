package com.fitlife.controller;

import com.fitlife.dao.PagoDAO;
import com.fitlife.model.PagoAdminDTO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Importaciones para PDF (OpenPDF)
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Image;

public class AdminPagosController {

    @FXML private TableView<PagoAdminDTO> tablaPagos;
    @FXML private TableColumn<PagoAdminDTO, String> colFecha;
    @FXML private TableColumn<PagoAdminDTO, String> colCliente;
    @FXML private TableColumn<PagoAdminDTO, Double> colMonto;
    @FXML private TableColumn<PagoAdminDTO, String> colMetodo;
    @FXML private TableColumn<PagoAdminDTO, String> colEstado;

    private PagoDAO pagoDAO = new PagoDAO();

    @FXML
    public void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarDatos();
    }

    private void cargarDatos() {
        tablaPagos.setItems(FXCollections.observableArrayList(pagoDAO.listarTodosLosPagosAdmin()));
    }

    // --- ACCIONES PRINCIPALES ---

    @FXML
    public void irANuevoPago(ActionEvent event) {
        // Redirige al formulario de registro de pago (PagoView.fxml)
        navegar(event, "PagoView.fxml", "Registrar Nuevo Pago");
    }

    @FXML
    public void imprimirComprobante() {
        // 1. Obtener el pago seleccionado
        PagoAdminDTO pagoSeleccionado = tablaPagos.getSelectionModel().getSelectedItem();

        if (pagoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Por favor, seleccione un pago de la tabla para imprimir el comprobante.");
            return;
        }

        // 2. Elegir dónde guardar el archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Comprobante PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        // Generar nombre de archivo sugerido
        String nombreArchivo = String.format("Comprobante_%s_%s.pdf",
                pagoSeleccionado.getNombreCliente().replace(" ", ""),
                pagoSeleccionado.getFecha().replace("-", ""));
        fileChooser.setInitialFileName(nombreArchivo);

        File file = fileChooser.showSaveDialog(tablaPagos.getScene().getWindow());

        if (file != null) {
            generarPDF(pagoSeleccionado, file);
        }
    }

    private void generarPDF(PagoAdminDTO pago, File file) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // --- 1. AGREGAR LOGO ---
            try {
                // Buscamos la imagen en la carpeta resources
                String imagePath = "/images/logo_fitlife.png";
                Image logo = Image.getInstance(getClass().getResource(imagePath));

                // Ajustamos el tamaño (ancho, alto) para que no sea gigante
                logo.scaleToFit(100, 100);

                // Lo centramos
                logo.setAlignment(Element.ALIGN_CENTER);

                // Agregamos al documento
                document.add(logo);

                // Un pequeño espacio después del logo
                document.add(new Paragraph(" "));
            } catch (Exception e) {
                System.out.println("No se pudo cargar el logo (verificar ruta): " + e.getMessage());
                // No detenemos el proceso, si falla el logo, el PDF se genera igual sin él.
            }
            // -----------------------

            // Título Principal
            Paragraph titulo = new Paragraph("FITLIFE GYM - COMPROBANTE DE PAGO");
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            // Detalles del Pago (Usando una tabla de 2 columnas)
            PdfPTable detalles = new PdfPTable(2);
            detalles.setWidthPercentage(80);
            detalles.setHorizontalAlignment(Element.ALIGN_CENTER);
            detalles.setSpacingAfter(20);

            // Fila 1: Cliente
            detalles.addCell(crearCelda("Cliente:", java.awt.Color.LIGHT_GRAY));
            detalles.addCell(crearCelda(pago.getNombreCliente(), java.awt.Color.WHITE));

            // Fila 2: Fecha
            detalles.addCell(crearCelda("Fecha de Pago:", java.awt.Color.LIGHT_GRAY));
            detalles.addCell(crearCelda(pago.getFecha(), java.awt.Color.WHITE));

            // Fila 3: Método
            detalles.addCell(crearCelda("Método:", java.awt.Color.LIGHT_GRAY));
            detalles.addCell(crearCelda(pago.getMetodo(), java.awt.Color.WHITE));

            // Fila 4: Monto (Destacado)
            PdfPCell cellMontoEt = crearCelda("Monto Total:", java.awt.Color.DARK_GRAY);
            // Usamos letra blanca para el fondo oscuro
            PdfPCell cellMontoVal = crearCelda(String.format("$%,.0f", pago.getMonto()), java.awt.Color.WHITE);

            // Le damos formato grande y negrita al monto
            com.lowagie.text.Font fontMonto = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
            cellMontoVal.setPhrase(new Phrase(String.format("$%,.0f", pago.getMonto()), fontMonto));

            detalles.addCell(cellMontoEt);
            detalles.addCell(cellMontoVal);

            document.add(detalles);

            // Nota de confirmación
            Paragraph nota = new Paragraph("Este comprobante confirma la recepción del pago y la actualización de la membresía.");
            nota.setAlignment(Paragraph.ALIGN_CENTER);
            nota.setSpacingBefore(20);
            document.add(nota);

            document.close();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Comprobante generado exitosamente.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al generar el PDF: " + e.getMessage());
        }
    }

    // Método auxiliar para crear celdas PDF
    private PdfPCell crearCelda(String texto, java.awt.Color colorFondo) {
        PdfPCell cell = new PdfPCell(new Phrase(texto));
        cell.setBackgroundColor(colorFondo);
        cell.setBorder(0);
        cell.setPadding(5);
        return cell;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Tienen que ser public, void y recibir ActionEvent
    @FXML
    public void volverMenu(ActionEvent event) {
        navegar(event, "MainView.fxml", "Inicio");
    }

    @FXML
    public void irAClases(ActionEvent event) {
        navegar(event, "ClaseView.fxml", "Clases");
    }

    @FXML
    public void irAReservas(ActionEvent event) {
        navegar(event, "ReservaView.fxml", "Reservas");
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
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}