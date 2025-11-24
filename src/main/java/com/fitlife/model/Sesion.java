package com.fitlife.model;

public class Sesion {
    // Instancia única (Singleton)
    private static Sesion instancia;

    // El usuario que inició sesión
    private Usuario usuarioActivo;

    private Sesion() {}

    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public void setUsuarioActivo(Usuario usuario) {
        this.usuarioActivo = usuario;
    }

    public void cerrarSesion() {
        this.usuarioActivo = null;
    }
}