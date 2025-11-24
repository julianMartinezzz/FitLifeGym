package com.fitlife.model;

import java.time.LocalDateTime;

public class ReservaDTO {
    private int idReserva;
    private int idClase;
    private String nombreClase;
    private String fecha; // Formato texto para la tabla
    private String hora;
    private String sala;
    private String estado;

    public ReservaDTO(int idReserva, int idClase, String nombreClase, String fecha, String hora, String sala, String estado) {
        this.idReserva = idReserva;
        this.idClase = idClase;
        this.nombreClase = nombreClase;
        this.fecha = fecha;
        this.hora = hora;
        this.sala = sala;
        this.estado = estado;
    }

    // Getters (Necesarios para la TableView)
    public int getIdReserva() { return idReserva; }
    public int getIdClase() { return idClase; }
    public String getNombreClase() { return nombreClase; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getSala() { return sala; }
    public String getEstado() { return estado; }
}