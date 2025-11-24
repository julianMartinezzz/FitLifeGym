package com.fitlife.model;

import java.time.LocalTime;

public class Clase {
    private int id;
    private String nombre;      // Ej: Yoga
    private int idInstructor;   // FK al instructor
    private String diaSemana;   // Lunes, Martes...
    private LocalTime horaInicio;
    private int cupoMaximo;
    private String nombreInstructor;
    private int reservasActivas;

    public Clase() {}

    public Clase(String nombre, int idInstructor, String diaSemana, LocalTime horaInicio, int cupoMaximo) {
        this.nombre = nombre;
        this.idInstructor = idInstructor;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.cupoMaximo = cupoMaximo;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdInstructor() { return idInstructor; }
    public void setIdInstructor(int idInstructor) { this.idInstructor = idInstructor; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public int getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(int cupoMaximo) { this.cupoMaximo = cupoMaximo; }

    public String getNombreInstructor() { return nombreInstructor; }
    public void setNombreInstructor(String nombreInstructor) { this.nombreInstructor = nombreInstructor; }

    public int getReservasActivas() { return reservasActivas; }
    public void setReservasActivas(int reservasActivas) { this.reservasActivas = reservasActivas; }


    public int getCuposDisponibles() { return this.cupoMaximo - this.reservasActivas; }
}
