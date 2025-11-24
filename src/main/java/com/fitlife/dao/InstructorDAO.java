package com.fitlife.dao;

import com.fitlife.model.Instructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDAO {

    public int obtenerIdPorEmail(String email) {
        String sql = "SELECT id_instructor FROM instructores WHERE email = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_instructor");
            }
        } catch (SQLException e) {
            System.out.println("Error buscando ID instructor: " + e.getMessage());
        }
        return -1;
    }

    public List<Instructor> listarInstructores() {
        List<Instructor> lista = new ArrayList<>();
        String sql = "SELECT * FROM instructores";

        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Instructor(
                        rs.getInt("id_instructor"),
                        rs.getString("nombre"),
                        rs.getString("especialidad"),
                        rs.getString("email"),
                        rs.getString("telefono"), // <--- NUEVO
                        rs.getBoolean("activo")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    // Actualizamos el registro para incluir teléfono
    public boolean registrarInstructorCompleto(String nombre, String especialidad, String email, String telefono) {
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false);

            String sqlInstr = "INSERT INTO instructores (nombre, especialidad, email, telefono, activo) VALUES (?, ?, ?, ?, TRUE)";
            PreparedStatement pstmt1 = conn.prepareStatement(sqlInstr);
            pstmt1.setString(1, nombre);
            pstmt1.setString(2, especialidad);
            pstmt1.setString(3, email);
            pstmt1.setString(4, telefono); // <--- NUEVO
            pstmt1.executeUpdate();

            // Usuario Login (Igual que antes)
            String sqlUser = "INSERT INTO usuarios (username, password, rol) VALUES (?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(sqlUser);
            pstmt2.setString(1, email);
            pstmt2.setString(2, "1234");
            pstmt2.setString(3, "INSTRUCTOR");
            pstmt2.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            System.out.println("Error registro: " + e.getMessage());
            return false;
        }
    }

    // --- NUEVO MÉTODO DE ACTUALIZACIÓN ---
    public boolean actualizarInstructor(Instructor instr) {
        // Nota: No actualizamos el email aquí para no romper el Login (usuario).
        // Si quisieras actualizar el email, tendrías que actualizar también la tabla 'usuarios'.
        // Por simplicidad, permitiremos editar todo menos el email (que es su ID de usuario).

        String sql = "UPDATE instructores SET nombre = ?, especialidad = ?, telefono = ? WHERE id_instructor = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, instr.getNombre());
            pstmt.setString(2, instr.getEspecialidad());
            pstmt.setString(3, instr.getTelefono());
            pstmt.setInt(4, instr.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    // ... (Mantén el método cambiarEstadoInstructor igual que antes) ...
    public boolean cambiarEstadoInstructor(int idInstructor, boolean nuevoEstado) {
        String sql = "UPDATE instructores SET activo = ? WHERE id_instructor = ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, nuevoEstado);
            pstmt.setInt(2, idInstructor);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}