package com.fitlife.controller;

import com.fitlife.model.Sesion;
import com.fitlife.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // Botones Admin
    @FXML private Button btnClientes;
    @FXML private Button btnInstructores;
    @FXML private Button btnClases;      // Programar
    @FXML private Button btnReservas;    // Gestión Global
    @FXML private Button btnReportes;

    // Botones Cliente
    @FXML private Button btnPagos;
    @FXML private Button btnMisReservas; // <--- NUEVO BOTÓN

    // Botones Instructor
    @FXML private Button btnMisClases;   // <--- BOTÓN QUE SE COLABA

    // Panel Stats
    @FXML private HBox panelStats;
    @FXML private Label lblClientes;
    @FXML private Label lblIngresos;
    @FXML private Label lblClasePop;

    private com.fitlife.dao.StatsDAO statsDAO = new com.fitlife.dao.StatsDAO();

    @FXML
    public void initialize() {
        Usuario usuario = Sesion.getInstancia().getUsuarioActivo();

        if (usuario != null) {
            String rol = usuario.getRol().toUpperCase();

            // 1. CONFIGURACIÓN DASHBOARD (Métricas)
            if (rol.equals("ADMIN")) {
                cargarMetricas();
            } else {
                if (panelStats != null) {
                    panelStats.setVisible(false);
                    panelStats.setManaged(false);
                }
            }

            // 2. CONFIGURACIÓN DE VISIBILIDAD DE BOTONES
            // Estrategia: Apagamos todo primero, y prendemos solo lo que corresponde
            apagarTodosLosBotones();

            switch (rol) {
                case "ADMIN":
                    mostrarBoton(btnClientes, true);
                    mostrarBoton(btnInstructores, true);
                    mostrarBoton(btnClases, true);
                    mostrarBoton(btnReservas, true);
                    mostrarBoton(btnReportes, true);
                    mostrarBoton(btnPagos, true); // Admin puede gestionar pagos
                    break;

                case "INSTRUCTOR":
                    mostrarBoton(btnMisClases, true); // Solo ve sus clases
                    break;

                case "CLIENTE":
                    mostrarBoton(btnPagos, true);       // Ver sus pagos
                    mostrarBoton(btnMisReservas, true); // Ver sus reservas
                    break;
            }
        }
    }

    // Método para reiniciar el estado visual (todo oculto)
    private void apagarTodosLosBotones() {
        mostrarBoton(btnClientes, false);
        mostrarBoton(btnInstructores, false);
        mostrarBoton(btnClases, false);
        mostrarBoton(btnReservas, false);
        mostrarBoton(btnReportes, false);
        mostrarBoton(btnPagos, false);
        mostrarBoton(btnMisReservas, false);
        mostrarBoton(btnMisClases, false);
    }

    private void mostrarBoton(Button btn, boolean mostrar) {
        if (btn != null) {
            btn.setVisible(mostrar);
            btn.setManaged(mostrar); // Si es false, colapsa el espacio
        }
    }

    private void cargarMetricas() {
        int totalClientes = statsDAO.obtenerClientesActivos();
        lblClientes.setText(String.valueOf(totalClientes));

        double ingresos = statsDAO.obtenerIngresosMes();
        lblIngresos.setText(String.format("$%,.0f", ingresos));

        String claseTop = statsDAO.obtenerClasePopular();
        lblClasePop.setText(claseTop);
    }

    // --- NAVEGACIÓN ---

    @FXML public void irAClientes(ActionEvent e) { cambiarVista(e, "ClienteView.fxml", "Gestión de Clientes"); }
    @FXML public void irAInstructores(ActionEvent e) { cambiarVista(e, "InstructorView.fxml", "Gestión de Entrenadores"); }
    @FXML public void irAClases(ActionEvent e) { cambiarVista(e, "ClaseView.fxml", "Programación de Clases"); }
    @FXML public void irAReservas(ActionEvent e) { cambiarVista(e, "ReservaView.fxml", "Gestión de Reservas (Admin)"); }
    @FXML public void irAReportes(ActionEvent e) { cambiarVista(e, "ReporteView.fxml", "Reportes del Sistema"); }

    @FXML public void irAHorarios(ActionEvent e) { cambiarVista(e, "HorarioView.fxml", "Horarios Disponibles"); }

    // Navegación Cliente
    @FXML public void irAMisReservas(ActionEvent e) { cambiarVista(e, "MisReservasView.fxml", "Mis Reservas"); }

    // Navegación Instructor
    @FXML public void irAMisClasesInstructor(ActionEvent e) { cambiarVista(e, "InstructorClasesView.fxml", "Mis Clases y Asistencia"); }

    @FXML
    public void irAPagos(ActionEvent event) {
        Usuario u = Sesion.getInstancia().getUsuarioActivo();
        if (u != null && u.getRol().equals("CLIENTE")) {
            cambiarVista(event, "MisPagosView.fxml", "Mis Pagos");
        } else {
            cambiarVista(event, "AdminPagosView.fxml", "Administración de Pagos");
        }
    }

    @FXML
    public void irAPerfil(ActionEvent event) {
        cambiarVista(event, "PerfilView.fxml", "Mi Perfil");
    }

    @FXML
    public void salir(ActionEvent event) {
        Sesion.getInstancia().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FitLife Gym - Inicio de Sesión");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cambiarVista(ActionEvent event, String fxmlFile, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FitLife Gym - " + titulo);
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}