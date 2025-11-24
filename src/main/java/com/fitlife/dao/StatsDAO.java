package com.fitlife.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsDAO {

    // 1. Contar Clientes Activos
    public int obtenerClientesActivos() {
        String sql = "SELECT COUNT(*) FROM clientes WHERE activo = TRUE";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 2. Calcular Ingresos del Mes Actual
    public double obtenerIngresosMes() {
        // Suma los montos donde el MES y AÑO de la fecha de pago coinciden con la fecha actual
        String sql = "SELECT SUM(monto) FROM pagos WHERE MONTH(fecha_pago) = MONTH(CURRENT_DATE()) AND YEAR(fecha_pago) = YEAR(CURRENT_DATE())";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // 3. Obtener la Clase más Popular (con más reservas activas)
    public String obtenerClasePopular() {
        String sql = "SELECT c.nombre_clase " +
                "FROM reservas r " +
                "JOIN clases c ON r.id_clase = c.id_clase " +
                "WHERE r.estado = 'ACTIVA' " +
                "GROUP BY c.nombre_clase " +
                "ORDER BY COUNT(*) DESC LIMIT 1";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return "N/A"; // Si no hay reservas aún
    }
}