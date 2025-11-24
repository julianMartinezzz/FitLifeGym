package com.fitlife.dao;

import com.fitlife.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario validarLogin(String user, String pass) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, pass);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Si encuentra al usuario, devuelve el objeto con sus datos
                return new Usuario(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("rol")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error en login: " + e.getMessage());
        }
        return null; // Retorna null si no existe o la contraseña está mal
    }

    // Método para registrar usuarios automáticamente
    public boolean crearUsuario(String username, String password, String rol) {
        String sql = "INSERT INTO usuarios (username, password, rol) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, rol);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al crear usuario login: " + e.getMessage());
            return false;
        }
    }
    // Método para cambiar contraseña
    public boolean cambiarPassword(String username, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE username = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevaPassword);
            pstmt.setString(2, username);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }
}