package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.DevolucionDto;
import com.pp.economia_circular.entity.*;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.DevolucionRepository;
import com.pp.economia_circular.repositories.SolicitudIntercambioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock private DevolucionRepository devolucionRepository;
    @Mock private SolicitudIntercambioRepository solicitudRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private JWTService authService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private DevolucionService service;

    private Usuario solicitante;
    private Usuario propietario;
    private Usuario extrano;
    private SolicitudIntercambio intercambio;
    private Articulo solicitado;
    private Articulo ofrecido;

    @BeforeEach
    void setUp() {
        solicitante = usuario(1L, "USER");
        propietario = usuario(2L, "USER");
        extrano = usuario(3L, "USER");

        solicitado = articulo(10L, propietario, "Bicicleta");
        ofrecido = articulo(11L, solicitante, "Guitarra");

        intercambio = new SolicitudIntercambio(solicitado, ofrecido, solicitante);
        intercambio.setId(100L);
        intercambio.setEstado(SolicitudIntercambio.EstadoIntercambio.COMPLETADO);
        intercambio.setActualizadoEn(LocalDateTime.now().minusDays(1));

        lenient().when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(inv -> {
            Devolucion d = inv.getArgument(0);
            if (d.getId() == null) d.setId(500L);
            return d;
        });
    }

    private static Usuario usuario(Long id, String rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRol(rol);
        u.setEmail("u" + id + "@test.com");
        return u;
    }

    private static Articulo articulo(Long id, Usuario duenio, String titulo) {
        Articulo a = new Articulo();
        a.setId(id);
        a.setUsuario(duenio);
        a.setTitulo(titulo);
        a.setEstado(Articulo.EstadoArticulo.INTERCAMBIADO);
        return a;
    }

    private Devolucion pendientePedidaPor(Usuario quien) {
        Devolucion d = new Devolucion();
        d.setId(500L);
        d.setSolicitud(intercambio);
        d.setSolicitante(quien);
        d.setMotivo("No funciona");
        return d;
    }

    @Test
    void participantePuedePedirDevolucionDentroDelPlazo() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(intercambio));

        DevolucionDto dto = service.solicitar(100L, "  No funciona  ");

        assertEquals(Devolucion.EstadoDevolucion.PENDIENTE, dto.getEstado());
        assertEquals("No funciona", dto.getMotivo());
        verify(notificacionService).crear(eq(propietario), eq(solicitante), eq(Notificacion.TipoNotificacion.DEVOLUCION),
                anyString(), anyString(), eq(Notificacion.ReferenciaTipo.DEVOLUCION), eq(500L));
    }

    @Test
    void noParticipanteNoPuedePedirDevolucion() {
        when(authService.getCurrentUser()).thenReturn(extrano);
        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(intercambio));

        assertThrows(RuntimeException.class, () -> service.solicitar(100L, "motivo"));
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    void soloIntercambiosCompletadosAdmitenDevolucion() {
        intercambio.setEstado(SolicitudIntercambio.EstadoIntercambio.ACEPTADO);
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(intercambio));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.solicitar(100L, "motivo"));
        assertTrue(ex.getMessage().contains("completado"));
    }

    @Test
    void fueraDePlazoNoSePuedePedir() {
        intercambio.setActualizadoEn(LocalDateTime.now().minusDays(30));
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(intercambio));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.solicitar(100L, "motivo"));
        assertTrue(ex.getMessage().contains("plazo"));
    }

    @Test
    void noSePuedenTenerDosPedidosPendientes() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(intercambio));
        when(devolucionRepository.existsBySolicitud_IdAndEstado(100L, Devolucion.EstadoDevolucion.PENDIENTE)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.solicitar(100L, "motivo"));
    }

    @Test
    void motivoVacioEsRechazado() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(intercambio));

        assertThrows(RuntimeException.class, () -> service.solicitar(100L, "   "));
    }

    @Test
    void aceptarRevierteElIntercambioYReactivaLosArticulos() {
        when(authService.getCurrentUser()).thenReturn(propietario);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(pendientePedidaPor(solicitante)));

        DevolucionDto dto = service.responder(500L, true, "Ok, coordinamos");

        assertEquals(Devolucion.EstadoDevolucion.ACEPTADA, dto.getEstado());
        assertEquals(SolicitudIntercambio.EstadoIntercambio.DEVUELTO, intercambio.getEstado());
        assertEquals(Articulo.EstadoArticulo.DISPONIBLE, solicitado.getEstado());
        assertEquals(Articulo.EstadoArticulo.DISPONIBLE, ofrecido.getEstado());
        assertNotNull(dto.getResueltoEn());
        verify(notificacionService).crear(eq(solicitante), eq(propietario), eq(Notificacion.TipoNotificacion.DEVOLUCION),
                eq("Devolución aceptada"), anyString(), any(), eq(500L));
    }

    @Test
    void rechazarNoTocaElIntercambio() {
        when(authService.getCurrentUser()).thenReturn(propietario);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(pendientePedidaPor(solicitante)));

        DevolucionDto dto = service.responder(500L, false, null);

        assertEquals(Devolucion.EstadoDevolucion.RECHAZADA, dto.getEstado());
        assertEquals(SolicitudIntercambio.EstadoIntercambio.COMPLETADO, intercambio.getEstado());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void quienPidioLaDevolucionNoPuedeResponderla() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(pendientePedidaPor(solicitante)));

        assertThrows(RuntimeException.class, () -> service.responder(500L, true, null));
    }

    @Test
    void adminPuedeResolverUnaDevolucion() {
        Usuario admin = usuario(9L, "ADMIN");
        when(authService.getCurrentUser()).thenReturn(admin);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(pendientePedidaPor(solicitante)));

        assertEquals(Devolucion.EstadoDevolucion.ACEPTADA, service.responder(500L, true, null).getEstado());
    }

    @Test
    void noSePuedeResponderUnaDevolucionYaResuelta() {
        Devolucion resuelta = pendientePedidaPor(solicitante);
        resuelta.setEstado(Devolucion.EstadoDevolucion.RECHAZADA);
        when(authService.getCurrentUser()).thenReturn(propietario);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(resuelta));

        assertThrows(RuntimeException.class, () -> service.responder(500L, true, null));
    }

    @Test
    void soloQuienPidioPuedeCancelar() {
        when(authService.getCurrentUser()).thenReturn(propietario);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(pendientePedidaPor(solicitante)));

        assertThrows(RuntimeException.class, () -> service.cancelar(500L));
    }

    @Test
    void cancelarMarcaLaDevolucionComoCancelada() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(devolucionRepository.findById(500L)).thenReturn(Optional.of(pendientePedidaPor(solicitante)));

        assertEquals(Devolucion.EstadoDevolucion.CANCELADA, service.cancelar(500L).getEstado());
    }

    @Test
    void listarTodasEsSoloParaAdmins() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        assertThrows(RuntimeException.class, () -> service.todas());
    }
}
