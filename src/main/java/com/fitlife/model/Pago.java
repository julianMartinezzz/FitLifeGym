package com.fitlife.model;
import java.time.LocalDate;

public class Pago {
    private int id;
    private int idCliente;
    private double monto;
    private String metodoPago; // NUEVO
    private String concepto;
    private LocalDate fechaPago;

    // Constructor actualizado
    public Pago(int idCliente, double monto, String metodoPago, String concepto) {
        this.idCliente = idCliente;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.concepto = concepto;
        this.fechaPago = LocalDate.now();
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    // Getters (Agrega el del método)
    public int getIdCliente() { return idCliente; }
    public double getMonto() { return monto; }
    public String getMetodoPago() { return metodoPago; } // NUEVO
    public String getConcepto() { return concepto; }
    public LocalDate getFechaPago() { return fechaPago; }
}