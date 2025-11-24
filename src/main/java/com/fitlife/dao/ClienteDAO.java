package com.fitlife.dao;

import com.fitlife.model.Cliente;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Statement;

public class ClienteDAO {

    public boolean registrarCliente(Cliente cliente) {
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false); // Transacción

            // 1. Guardar en tabla CLIENTES
            String sqlCliente = "INSERT INTO clientes (documento, nombre, correo, telefono, direccion, tipo_plan, fecha_inicio, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt1 = conn.prepareStatement(sqlCliente);
            pstmt1.setString(1, cliente.getDocumento());
            pstmt1.setString(2, cliente.getNombre());
            pstmt1.setString(3, cliente.getCorreo());
            pstmt1.setString(4, cliente.getTelefono());
            pstmt1.setString(5, cliente.getDireccion());
            pstmt1.setString(6, cliente.getPlan());
            pstmt1.setDate(7, java.sql.Date.valueOf(cliente.getFechaInicio()));
            pstmt1.setBoolean(8, true);
            pstmt1.executeUpdate();

            // 2. Crear automáticamente su USUARIO de Login
            // Usuario = Correo, Pass = Documento, Rol = "CLIENTE"
            String sqlUser = "INSERT INTO usuarios (username, password, rol) VALUES (?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(sqlUser);
            pstmt2.setString(1, cliente.getCorreo()); // Username
            pstmt2.setString(2, cliente.getDocumento()); // Password inicial
            pstmt2.setString(3, "CLIENTE"); // Rol
            pstmt2.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            System.out.println("Error registrando cliente+usuario: " + e.getMessage());
            return false;
        }
    }

    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setDocumento(rs.getString("documento"));
                c.setNombre(rs.getString("nombre"));
                c.setCorreo(rs.getString("correo"));
                c.setPlan(rs.getString("tipo_plan"));

                // Manejo de fechas (pueden ser nulas)
                if (rs.getDate("fecha_inicio") != null) {
                    c.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                }

                // Importante: Recuperar si está activo
                c.setActivo(rs.getBoolean("activo"));

                lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    public boolean cambiarEstadoCliente(int idCliente, boolean nuevoEstado) {
        String sql = "UPDATE clientes SET activo = ? WHERE id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, nuevoEstado);
            pstmt.setInt(2, idCliente);

            int filas = pstmt.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }
    // Método para buscar el ID del cliente basado en su correo/usuario de login
    public int obtenerIdPorCorreo(String correo) {
        String sql = "SELECT id FROM clientes WHERE correo = ?"; // O usa 'documento' si prefieres
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar cliente: " + e.getMessage());
        }
        return -1; // Retorna -1 si no encuentra al cliente
    }



    // Método auxiliar para obtener la fecha de vencimiento directamente
    public java.time.LocalDate obtenerVencimiento(int idCliente) {
        String sql = "SELECT fecha_vencimiento FROM clientes WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getDate(1) != null) {
                return rs.getDate(1).toLocalDate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Cliente buscarPorDocumento(String documento) {
        String sql = "SELECT * FROM clientes WHERE documento = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, documento);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDocumento(rs.getString("documento"));
                return c;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }


}