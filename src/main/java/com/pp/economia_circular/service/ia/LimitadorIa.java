package com.pp.economia_circular.service.ia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite simple de pedidos de IA por usuario (ventana deslizante de una hora),
 * para acotar el costo de la API. Vive en memoria: alcanza para una sola instancia.
 */
@Component
public class LimitadorIa {

    private static final long VENTANA_MS = 60L * 60 * 1000;

    private final int maxPorHora;
    private final Clock clock;
    private final Map<Long, Deque<Long>> pedidos = new ConcurrentHashMap<>();

    @Autowired
    public LimitadorIa(@Value("${app.ia.max-pedidos-por-hora:30}") int maxPorHora) {
        this(maxPorHora, Clock.systemUTC());
    }

    LimitadorIa(int maxPorHora, Clock clock) {
        this.maxPorHora = maxPorHora;
        this.clock = clock;
    }

    /** Registra un pedido o lanza IaException si el usuario superó el límite. */
    public void registrar(Long usuarioId) {
        long ahora = clock.millis();
        Deque<Long> historial = pedidos.computeIfAbsent(usuarioId, id -> new ArrayDeque<>());
        synchronized (historial) {
            while (!historial.isEmpty() && historial.peekFirst() <= ahora - VENTANA_MS) {
                historial.pollFirst();
            }
            if (historial.size() >= maxPorHora) {
                throw new IaException("Llegaste al límite de consultas a la IA por hora. Probá más tarde.");
            }
            historial.addLast(ahora);
        }
    }
}
