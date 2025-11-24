package com.fitlife.model;

import java.time.LocalDate;

public class Cliente {
    private int id;
    private String documento;
    private String nombre;
    private String correo;
    private String telefono;
    private String direccion;
    private String plan; // Mensual, Trimestral, etc.
    private LocalDate fechaInicio;
    private boolean activo;

    // Constructor vacío
    public Cliente() {}

    // Constructor con datos (para registrar)
    public Cliente(String documento, String nombre, String correo, String telefono, String direccion, String plan, LocalDate fechaInicio) {
        this.documento = documento;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.plan = plan;
        this.fechaInicio = fechaInicio;
        this.activo = true; // Por defecto activo al registrarse
    }

    // Getters y Setters (Necesarios para acceder a los datos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}