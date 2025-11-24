package com.fitlife.model;

public class Instructor {
    private int id;
    private String nombre;
    private String especialidad;
    private String email;
    private String telefono; // <--- NUEVO
    private boolean activo;

    // Constructor completo actualizado
    public Instructor(int id, String nombre, String especialidad, String email, String telefono, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
    }

    // Getters y Setters nuevos
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; } // Necesario para editar

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; } // <--- NUEVO
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() { return nombre; }
}