package com.fitlife.model;

public class PagoAdminDTO {
    private String fecha;
    private String nombreCliente;
    private double monto;
    private String metodo;
    private String estado; // Ej: "Completado"

    public PagoAdminDTO(String fecha, String nombreCliente, double monto, String metodo) {
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
        this.monto = monto;
        this.metodo = metodo;
        this.estado = "Completado"; // Por defecto, si está en BD es que se pagó
    }

    // Getters necesarios para la Tabla
    public String getFecha() { return fecha; }
    public String getNombreCliente() { return nombreCliente; }
    public double getMonto() { return monto; }
    public String getMetodo() { return metodo; }
    public String getEstado() { return estado; }
}