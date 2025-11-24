package com.fitlife.model;

import java.time.LocalDateTime;

public class Reserva {
    private int id;
    private int idCliente;
    private int idClase;
    private LocalDateTime fechaReserva;
    private String estado; // 'ACTIVA', 'CANCELADA'

    public Reserva(int idCliente, int idClase) {
        this.idCliente = idCliente;
        this.idClase = idClase;
        this.fechaReserva = LocalDateTime.now();
        this.estado = "ACTIVA";
    }

    // Getters y Setters
    public int getIdCliente() { return idCliente; }
    public int getIdClase() { return idClase; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public String getEstado() { return estado; }
}