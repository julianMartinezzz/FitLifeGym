package com.fitlife.dao;

import com.fitlife.model.Reserva;
import java.sql.*;
import com.fitlife.model.ReservaDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class ReservaDAO {

    // Método principal: Intenta reservar
    public String registrarReserva(Reserva reserva) {
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();

            // 1. VALIDAR CUPO: Contar cuántas reservas activas tiene esa clase
            String sqlConteo = "SELECT COUNT(*) FROM reservas WHERE id_clase = ? AND estado = 'ACTIVA'";
            PreparedStatement stmtConteo = conn.prepareStatement(sqlConteo);
            stmtConteo.setInt(1, reserva.getIdClase());
            ResultSet rs = stmtConteo.executeQuery();
            rs.next();
            int reservasActuales = rs.getInt(1);

            // 2. OBTENER CUPO MÁXIMO DE LA CLASE
            String sqlCupo = "SELECT cupo_maximo FROM clases WHERE id_clase = ?";
            PreparedStatement stmtCupo = conn.prepareStatement(sqlCupo);
            stmtCupo.setInt(1, reserva.getIdClase());
            ResultSet rsCupo = stmtCupo.executeQuery();

            if (!rsCupo.next()) return "Error: La clase no existe.";
            int cupoMaximo = rsCupo.getInt(1);

            // 3. COMPARAR
            if (reservasActuales >= cupoMaximo) {
                return "Error: La clase está llena (Sobrecupo).";
            }

            // 4. VALIDAR DUPLICADO (Si el cliente ya reservó esa clase)
            String sqlDuplicado = "SELECT id_reserva FROM reservas WHERE id_cliente = ? AND id_clase = ? AND estado = 'ACTIVA'";
            PreparedStatement stmtDup = conn.prepareStatement(sqlDuplicado);
            stmtDup.setInt(1, reserva.getIdCliente());
            stmtDup.setInt(2, reserva.getIdClase());
            if (stmtDup.executeQuery().next()) {
                return "Error: El cliente ya tiene una reserva en esta clase.";
            }

            // 5. INSERTAR RESERVA
            String sqlInsert = "INSERT INTO reservas (id_cliente, id_clase, fecha_reserva, estado) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setInt(1, reserva.getIdCliente());
            stmtInsert.setInt(2, reserva.getIdClase());
            stmtInsert.setTimestamp(3, Timestamp.valueOf(reserva.getFechaReserva()));
            stmtInsert.setString(4, reserva.getEstado());

            stmtInsert.executeUpdate();
            return "Exito"; // Código clave para saber que funcionó

        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        }
    }

    public boolean cancelarReserva(int idCliente, int idClase) {
        // Solo cancelamos si está actualmente 'ACTIVA'
        String sql = "UPDATE reservas SET estado = 'CANCELADA' WHERE id_cliente = ? AND id_clase = ? AND estado = 'ACTIVA'";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            pstmt.setInt(2, idClase);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0; // Retorna true si encontró y canceló la reserva

        } catch (SQLException e) {
            System.out.println("Error al cancelar reserva: " + e.getMessage());
            return false;
        }
    }

    public List<ReservaDTO> listarReservasPorCliente(int idCliente) {
        List<ReservaDTO> lista = new ArrayList<>();
        // Hacemos JOIN para saber el nombre de la clase y el horario
        String sql = "SELECT r.id_reserva, r.id_clase, c.nombre_clase, r.fecha_reserva, c.hora_inicio, r.estado " +
                "FROM reservas r " +
                "JOIN clases c ON r.id_clase = c.id_clase " +
                "WHERE r.id_cliente = ? " +
                "ORDER BY r.fecha_reserva DESC"; // Las más recientes primero

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

            while (rs.next()) {
                // Convertimos los datos crudos a formato bonito para la tabla
                LocalDateTime fechaRaw = rs.getTimestamp("fecha_reserva").toLocalDateTime();
                String fechaStr = fechaRaw.format(dateFmt);
                String horaStr = rs.getTime("hora_inicio").toLocalTime().format(timeFmt); // Usamos la hora de la clase

                lista.add(new ReservaDTO(
                        rs.getInt("id_reserva"),
                        rs.getInt("id_clase"),
                        rs.getString("nombre_clase"),
                        fechaStr,
                        horaStr,
                        "Sala Principal", // Dato fijo o agrégalo a tu tabla clases si quieres
                        rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar reservas: " + e.getMessage());
        }
        return lista;
    }

    // Método actualizado: Retorna AsistenteDTO con el estado de asistencia
    public List<com.fitlife.model.AsistenteDTO> obtenerAsistentesPorClase(int idClase) {
        List<com.fitlife.model.AsistenteDTO> lista = new ArrayList<>();

        // Traemos id_reserva y la columna asistio
        String sql = "SELECT r.id_reserva, c.nombre, c.documento, r.asistio " +
                "FROM reservas r " +
                "JOIN clientes c ON r.id_cliente = c.id " +
                "WHERE r.id_clase = ? AND r.estado = 'ACTIVA' " +
                "ORDER BY c.nombre ASC";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idClase);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(new com.fitlife.model.AsistenteDTO(
                        rs.getInt("id_reserva"),
                        rs.getString("nombre"),
                        rs.getString("documento"),
                        rs.getBoolean("asistio")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asistentes: " + e.getMessage());
        }
        return lista;
    }

    // NUEVO MÉTODO: Actualizar asistencia
    public boolean marcarAsistencia(int idReserva, boolean asistio) {
        String sql = "UPDATE reservas SET asistio = ? WHERE id_reserva = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, asistio);
            pstmt.setInt(2, idReserva);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}