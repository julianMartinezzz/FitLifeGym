package com.fitlife.dao;

import com.fitlife.model.Pago;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.fitlife.model.PagoAdminDTO;

public class PagoDAO {

    /**
     * Registra un pago y actualiza la vigencia de la membresía del cliente.
     * Todo ocurre en una sola transacción para seguridad de los datos.
     */
    public boolean registrarPago(Pago pago) {
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            // 1. Desactivar auto-guardado para manejar transacción manual
            conn.setAutoCommit(false);

            // 2. Insertar el registro en la tabla 'pagos'
            String sqlPago = "INSERT INTO pagos (id_cliente, monto, metodo_pago, fecha_pago, concepto) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmtPago = conn.prepareStatement(sqlPago);
            stmtPago.setInt(1, pago.getIdCliente());
            stmtPago.setDouble(2, pago.getMonto());
            stmtPago.setString(3, pago.getMetodoPago()); // Nuevo campo
            stmtPago.setDate(4, java.sql.Date.valueOf(pago.getFechaPago()));
            stmtPago.setString(5, pago.getConcepto());
            stmtPago.executeUpdate();

            // 3. Actualizar la fecha de vencimiento del cliente (+1 Mes)
            // Lógica SQL: Si fecha_vencimiento es nula o pasada, usa HOY + 1 Mes.
            // Si es futura, suma 1 Mes a la fecha que ya tenía.
            String sqlUpdate = "UPDATE clientes SET fecha_vencimiento = DATE_ADD(COALESCE(fecha_vencimiento, CURDATE()), INTERVAL 1 MONTH), activo = TRUE WHERE id = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setInt(1, pago.getIdCliente());
            stmtUpdate.executeUpdate();

            // 4. Confirmar transacción (Guardar todo)
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Error crítico en transacción de pago: " + e.getMessage());
            // Si algo falla, deshacemos todo (Rollback)
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // Restaurar estado de la conexión
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    /**
     * Lista todos los pagos de un cliente específico para el historial.
     */
    public List<Pago> listarPagosPorCliente(int idCliente) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE id_cliente = ? ORDER BY fecha_pago DESC";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Crear el objeto con los datos de la BD
                // Nota: Asegúrate de que el constructor de Pago coincida con esto
                Pago p = new Pago(
                        rs.getInt("id_cliente"),
                        rs.getDouble("monto"),
                        rs.getString("metodo_pago"), // Recuperamos si fue Efectivo/Tarjeta
                        rs.getString("concepto")
                );

                // Actualizamos la fecha del objeto con la fecha real de la BD
                if (rs.getDate("fecha_pago") != null) {
                    // Necesitas agregar este setter en tu modelo Pago si no existe:
                    // public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
                    p.setFechaPago(rs.getDate("fecha_pago").toLocalDate());
                }

                lista.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar pagos: " + e.getMessage());
        }
        return lista;
    }

    public List<PagoAdminDTO> listarTodosLosPagosAdmin() {
        List<PagoAdminDTO> lista = new ArrayList<>();
        // JOIN para obtener el nombre del cliente
        String sql = "SELECT p.fecha_pago, c.nombre, p.monto, p.metodo_pago " +
                "FROM pagos p " +
                "JOIN clientes c ON p.id_cliente = c.id " +
                "ORDER BY p.fecha_pago DESC";

        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new PagoAdminDTO(
                        rs.getDate("fecha_pago").toString(),
                        rs.getString("nombre"),
                        rs.getDouble("monto"),
                        rs.getString("metodo_pago")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar historial admin: " + e.getMessage());
        }
        return lista;
    }

    // Método para validar si ya existe un pago de mensualidad en el mes/año actual
    public boolean existePagoMensualidad(int idCliente) {
        // Buscamos pagos de este cliente, en el mes y año actuales, que contengan "Mensual" o "Plan" en el concepto
        String sql = "SELECT COUNT(*) FROM pagos WHERE id_cliente = ? " +
                "AND MONTH(fecha_pago) = MONTH(CURRENT_DATE()) " +
                "AND YEAR(fecha_pago) = YEAR(CURRENT_DATE()) " +
                "AND (concepto LIKE '%Mensual%' OR concepto LIKE '%Plan%')";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Si hay más de 0, es que ya pagó
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}