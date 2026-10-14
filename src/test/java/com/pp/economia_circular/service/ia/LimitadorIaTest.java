package com.pp.economia_circular.service.ia;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LimitadorIaTest {

    /** Reloj que se puede adelantar a mano. */
    static class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-01-01T10:00:00Z");

        void avanzar(Duration d) { ahora = ahora.plus(d); }

        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    @Test
    void permiteHastaElMaximoPorHoraYLuegoCorta() {
        LimitadorIa limitador = new LimitadorIa(3, new RelojManual());

        limitador.registrar(1L);
        limitador.registrar(1L);
        limitador.registrar(1L);
        IaException ex = assertThrows(IaException.class, () -> limitador.registrar(1L));
        assertFalse(ex.isNoDisponible());
    }

    @Test
    void elLimiteEsPorUsuario() {
        LimitadorIa limitador = new LimitadorIa(1, new RelojManual());

        limitador.registrar(1L);
        assertDoesNotThrow(() -> limitador.registrar(2L));
    }

    @Test
    void pasadaLaHoraSeLiberanLosPedidos() {
        RelojManual reloj = new RelojManual();
        LimitadorIa limitador = new LimitadorIa(1, reloj);

        limitador.registrar(1L);
        reloj.avanzar(Duration.ofMinutes(61));
        assertDoesNotThrow(() -> limitador.registrar(1L));
    }
}
