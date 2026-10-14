package com.pp.economia_circular.service.ia;

/** Error de IA con un mensaje apto para mostrar al usuario. */
public class IaException extends RuntimeException {

    private final boolean noDisponible;

    public IaException(String mensaje) {
        this(mensaje, false);
    }

    public IaException(String mensaje, boolean noDisponible) {
        super(mensaje);
        this.noDisponible = noDisponible;
    }

    /** true cuando la IA no está configurada o el servicio no responde (HTTP 503). */
    public boolean isNoDisponible() {
        return noDisponible;
    }
}
