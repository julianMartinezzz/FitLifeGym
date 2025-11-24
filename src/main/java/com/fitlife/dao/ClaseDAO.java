package com.fitlife.dao;

import com.fitlife.model.Clase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Statement;

public class ClaseDAO {

    public boolean registrarClase(Clase clase) {
        // Nota: Asumiremos una hora fin automática (1 hora después de inicio) para simplificar
        String sql = "INSERT INTO clases (nombre_clase, id_instructor, dia_semana, hora_inicio, hora_fin, cupo_maximo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clase.getNombre());
            pstmt.setInt(2, clase.getIdInstructor());
            pstmt.setString(3, clase.getDiaSemana());
            pstmt.setTime(4, Time.valueOf(clase.getHoraInicio()));
            pstmt.setTime(5, Time.valueOf(clase.getHoraInicio().plusHours(1))); // Fin automático +1 hora
            pstmt.setInt(6, clase.getCupoMaximo());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al crear clase: " + e.getMessage());
            return false;
        }
    }

    public List<Clase> listarClases() {
        List<Clase> lista = new ArrayList<>();

        // CONSULTA AVANZADA: Cuenta las reservas activas por cada clase
        String sql = "SELECT c.*, i.nombre as nombre_profe, " +
                "COALESCE(COUNT(r.id_reserva), 0) AS num_reservas " + // COALESCE maneja clases sin reservas (las cuenta como 0)
                "FROM clases c " +
                "LEFT JOIN instructores i ON c.id_instructor = i.id_instructor " +
                "LEFT JOIN reservas r ON c.id_clase = r.id_clase AND r.estado = 'ACTIVA' " + // Solo contamos ACTIVAS
                "GROUP BY c.id_clase, c.nombre_clase, i.nombre, c.cupo_maximo, c.hora_inicio, c.dia_semana, c.hora_fin, c.id_instructor"; // Agrupamos por todas las columnas

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql); // Cambiado a PreparedStatement
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Clase clase = new Clase();
                clase.setId(rs.getInt("id_clase"));
                clase.setNombre(rs.getString("nombre_clase"));
                clase.setIdInstructor(rs.getInt("id_instructor"));
                clase.setNombreInstructor(rs.getString("nombre_profe"));
                clase.setDiaSemana(rs.getString("dia_semana"));

                if (rs.getTime("hora_inicio") != null) {
                    clase.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
                }

                clase.setCupoMaximo(rs.getInt("cupo_maximo"));

                // --- AQUI GUARDAMOS EL CONTEO REAL ---
                clase.setReservasActivas(rs.getInt("num_reservas"));
                // ------------------------------------

                lista.add(clase);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar clases con reservas: " + e.getMessage());
        }
        return lista;
    }

    // Método para eliminar una clase por su ID
    public boolean eliminarClase(int idClase) {
        // Nota: Si hay reservas, esto podría fallar por seguridad (Foreign Key).
        // Para este proyecto, intentaremos borrar.
        String sql = "DELETE FROM clases WHERE id_clase = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idClase);
            int filas = pstmt.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar clase: " + e.getMessage());
            return false;
        }
    }

    // Método para que el Instructor vea SOLO sus clases asignadas
    public List<Clase> listarClasesPorInstructor(int idInstructor) {
        List<Clase> lista = new ArrayList<>();
        // Reutilizamos la lógica de contar reservas
        String sql = "SELECT c.*, i.nombre as nombre_profe, " +
                "COALESCE(COUNT(r.id_reserva), 0) AS num_reservas " +
                "FROM clases c " +
                "JOIN instructores i ON c.id_instructor = i.id_instructor " +
                "LEFT JOIN reservas r ON c.id_clase = r.id_clase AND r.estado = 'ACTIVA' " +
                "WHERE c.id_instructor = ? " + // <--- EL FILTRO CLAVE
                "GROUP BY c.id_clase, c.nombre_clase, i.nombre, c.cupo_maximo, c.hora_inicio, c.dia_semana, c.hora_fin, c.id_instructor";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idInstructor); // Pasamos el ID del profe conectado
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Clase clase = new Clase();
                clase.setId(rs.getInt("id_clase"));
                clase.setNombre(rs.getString("nombre_clase"));
                clase.setDiaSemana(rs.getString("dia_semana"));
                if (rs.getTime("hora_inicio") != null) {
                    clase.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
                }
                clase.setCupoMaximo(rs.getInt("cupo_maximo"));
                clase.setReservasActivas(rs.getInt("num_reservas"));

                // No necesitamos setNombreInstructor porque ya sabe que es él mismo,
                // pero lo dejamos por compatibilidad si lo usas en la tabla.
                clase.setNombreInstructor(rs.getString("nombre_profe"));

                lista.add(clase);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar clases del instructor: " + e.getMessage());
        }
        return lista;
    }
}