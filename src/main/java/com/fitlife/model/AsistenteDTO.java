package com.fitlife.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AsistenteDTO {
    private int idReserva;
    private String nombreCliente;
    private String documento;
    private BooleanProperty asistio; // Usamos Property para que el Checkbox detecte cambios

    public AsistenteDTO(int idReserva, String nombreCliente, String documento, boolean asistioInicial) {
        this.idReserva = idReserva;
        this.nombreCliente = nombreCliente;
        this.documento = documento;
        this.asistio = new SimpleBooleanProperty(asistioInicial);
    }

    // Getters y Setters normales
    public int getIdReserva() { return idReserva; }
    public String getNombreCliente() { return nombreCliente; }
    public String getDocumento() { return documento; }

    // Getters especiales para JavaFX Properties
    public BooleanProperty asistioProperty() { return asistio; }
    public boolean isAsistio() { return asistio.get(); }
    public void setAsistio(boolean asistio) { this.asistio.set(asistio); }
}