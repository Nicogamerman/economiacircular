package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.*;
import com.pp.economia_circular.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeguimientoServiceTest {

    @Mock private SeguimientoRepository seguimientoRepository;
    @Mock private InteresEventoRepository interesEventoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private JWTService authService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private SeguimientoService service;

    private Usuario yo;
    private Usuario oferente;

    @BeforeEach
    void setUp() {
        yo = usuario(1L, "Ana", "ana@test.com");
        oferente = usuario(2L, "Beto", "beto@test.com");
    }

    private static Usuario usuario(Long id, String nombre, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);
        u.setEmail(email);
        return u;
    }

    @Test
    void seguirOferenteGuardaElSeguimientoYDevuelveEstado() {
        when(authService.getCurrentUser()).thenReturn(yo);
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(seguimientoRepository.existsByUsuario_IdAndTipoAndObjetivoId(1L, Seguimiento.TipoSeguimiento.OFERENTE, 2L))
                .thenReturn(false, true);
        when(seguimientoRepository.countByTipoAndObjetivoId(Seguimiento.TipoSeguimiento.OFERENTE, 2L)).thenReturn(1L);

        Map<String, Object> estado = service.seguir(Seguimiento.TipoSeguimiento.OFERENTE, 2L);

        ArgumentCaptor<Seguimiento> captor = ArgumentCaptor.forClass(Seguimiento.class);
        verify(seguimientoRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().getObjetivoId());
        assertEquals(yo, captor.getValue().getUsuario());
        assertEquals(true, estado.get("siguiendo"));
        assertEquals(1L, estado.get("seguidores"));
    }

    @Test
    void seguirDosVecesNoDuplica() {
        when(authService.getCurrentUser()).thenReturn(yo);
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(seguimientoRepository.existsByUsuario_IdAndTipoAndObjetivoId(1L, Seguimiento.TipoSeguimiento.OFERENTE, 2L))
                .thenReturn(true);

        service.seguir(Seguimiento.TipoSeguimiento.OFERENTE, 2L);

        verify(seguimientoRepository, never()).save(any());
    }

    @Test
    void noSePuedeSeguirASiMismo() {
        when(authService.getCurrentUser()).thenReturn(yo);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.seguir(Seguimiento.TipoSeguimiento.OFERENTE, 1L));
        assertTrue(ex.getMessage().contains("vos mismo"));
    }

    @Test
    void seguirEventoInexistenteFalla() {
        when(authService.getCurrentUser()).thenReturn(yo);
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.seguir(Seguimiento.TipoSeguimiento.EVENTO, 99L));
        verify(seguimientoRepository, never()).save(any());
    }

    @Test
    void seguirSinSesionFalla() {
        when(authService.getCurrentUser()).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.seguir(Seguimiento.TipoSeguimiento.OFERENTE, 2L));
    }

    @Test
    void dejarDeSeguirBorraElSeguimiento() {
        Seguimiento s = new Seguimiento(yo, Seguimiento.TipoSeguimiento.OFERENTE, 2L);
        when(authService.getCurrentUser()).thenReturn(yo);
        when(seguimientoRepository.findByUsuario_IdAndTipoAndObjetivoId(1L, Seguimiento.TipoSeguimiento.OFERENTE, 2L))
                .thenReturn(Optional.of(s));

        Map<String, Object> estado = service.dejarDeSeguir(Seguimiento.TipoSeguimiento.OFERENTE, 2L);

        verify(seguimientoRepository).delete(s);
        assertEquals(false, estado.get("siguiendo"));
    }

    @Test
    void estadoSinSesionInformaSeguidoresPeroNoSiguiendo() {
        when(authService.getCurrentUser()).thenReturn(null);
        when(seguimientoRepository.countByTipoAndObjetivoId(Seguimiento.TipoSeguimiento.EVENTO, 5L)).thenReturn(7L);

        Map<String, Object> estado = service.estado(Seguimiento.TipoSeguimiento.EVENTO, 5L);

        assertEquals(false, estado.get("siguiendo"));
        assertEquals(7L, estado.get("seguidores"));
    }

    @Test
    void feedVacioSiNoSigueANadie() {
        when(authService.getCurrentUser()).thenReturn(yo);
        when(seguimientoRepository.findObjetivoIds(1L, Seguimiento.TipoSeguimiento.OFERENTE)).thenReturn(Collections.emptyList());

        Page<?> feed = service.feed(PageRequest.of(0, 10));

        assertTrue(feed.isEmpty());
        verify(articleRepository, never()).findByUsuario_IdInAndEstadoOrderByCreadoEnDesc(any(), any(), any());
    }

    @Test
    void feedTraeArticulosDisponiblesDeLosOferentesSeguidos() {
        Articulo a = new Articulo();
        a.setId(10L);
        a.setTitulo("Bicicleta");
        a.setUsuario(oferente);
        when(authService.getCurrentUser()).thenReturn(yo);
        when(seguimientoRepository.findObjetivoIds(1L, Seguimiento.TipoSeguimiento.OFERENTE)).thenReturn(Collections.singletonList(2L));
        when(articleRepository.findByUsuario_IdInAndEstadoOrderByCreadoEnDesc(
                eq(Collections.singletonList(2L)), eq(Articulo.EstadoArticulo.DISPONIBLE), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(a)));

        Page<com.pp.economia_circular.DTO.ArticleResponseDto> feed = service.feed(PageRequest.of(0, 10));

        assertEquals(1, feed.getTotalElements());
        assertEquals("Bicicleta", feed.getContent().get(0).getTitle());
    }

    @Test
    void actualizarInteresesReemplazaLosAnterioresSinDuplicados() {
        when(authService.getCurrentUser()).thenReturn(yo);

        List<Event.EventType> resultado = service.actualizarIntereses(
                Arrays.asList(Event.EventType.FAIR, Event.EventType.FAIR, null, Event.EventType.WORKSHOP));

        verify(interesEventoRepository).deleteByUsuarioId(1L);
        verify(interesEventoRepository, times(2)).save(any(InteresEvento.class));
        assertEquals(Arrays.asList(Event.EventType.FAIR, Event.EventType.WORKSHOP), resultado);
    }

    @Test
    void misSeguimientosDevuelveOferentesYEventos() {
        Event feria = new Event();
        feria.setId(5L);
        feria.setEventName("Feria verde");
        feria.setEventDate(LocalDateTime.now().plusDays(3));
        when(authService.getCurrentUser()).thenReturn(yo);
        when(seguimientoRepository.findByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Arrays.asList(
                new Seguimiento(yo, Seguimiento.TipoSeguimiento.OFERENTE, 2L),
                new Seguimiento(yo, Seguimiento.TipoSeguimiento.EVENTO, 5L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(oferente));
        when(eventRepository.findById(5L)).thenReturn(Optional.of(feria));
        when(interesEventoRepository.findByUsuario_Id(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> resultado = service.misSeguimientos();

        List<?> oferentes = (List<?>) resultado.get("oferentes");
        List<?> eventos = (List<?>) resultado.get("eventos");
        assertEquals(1, oferentes.size());
        assertEquals(1, eventos.size());
        assertEquals("Feria verde", ((Map<?, ?>) eventos.get(0)).get("nombre"));
    }

    @Test
    void nuevoArticuloNotificaATodosLosSeguidores() {
        Articulo a = new Articulo();
        a.setId(10L);
        a.setTitulo("Lámpara");
        a.setUsuario(oferente);
        Usuario otro = usuario(3L, "Caro", "caro@test.com");
        when(seguimientoRepository.findSeguidores(Seguimiento.TipoSeguimiento.OFERENTE, 2L)).thenReturn(Arrays.asList(
                new Seguimiento(yo, Seguimiento.TipoSeguimiento.OFERENTE, 2L),
                new Seguimiento(otro, Seguimiento.TipoSeguimiento.OFERENTE, 2L)));

        int notificados = service.notificarNuevoArticulo(a);

        assertEquals(2, notificados);
        verify(notificacionService, times(2)).crear(any(Usuario.class), eq(oferente),
                eq(Notificacion.TipoNotificacion.NUEVO_ARTICULO_SEGUIDO), anyString(), eq("Lámpara"),
                eq(Notificacion.ReferenciaTipo.ARTICULO), eq(10L));
    }

    @Test
    void eventoNuevoNotificaALosInteresadosEnEseTipo() {
        Event feria = new Event();
        feria.setId(5L);
        feria.setEventName("Feria de trueque");
        feria.setEventType(Event.EventType.FAIR);
        feria.setUsuario(oferente);
        when(interesEventoRepository.findInteresados(Event.EventType.FAIR))
                .thenReturn(Collections.singletonList(new InteresEvento(yo, Event.EventType.FAIR)));

        assertEquals(1, service.notificarEventoNuevo(feria));
        verify(notificacionService).crear(eq(yo), eq(oferente), eq(Notificacion.TipoNotificacion.EVENTO_NUEVO),
                contains("Feria de trueque"), anyString(), eq(Notificacion.ReferenciaTipo.EVENTO), eq(5L));
    }

    @Test
    void eventoActualizadoNotificaALosSeguidoresDelEvento() {
        Event feria = new Event();
        feria.setId(5L);
        feria.setEventName("Feria de trueque");
        when(seguimientoRepository.findSeguidores(Seguimiento.TipoSeguimiento.EVENTO, 5L))
                .thenReturn(Collections.singletonList(new Seguimiento(yo, Seguimiento.TipoSeguimiento.EVENTO, 5L)));

        assertEquals(1, service.notificarEventoActualizado(feria, "Cambió la fecha."));
        verify(notificacionService).crear(eq(yo), any(), eq(Notificacion.TipoNotificacion.EVENTO_ACTUALIZADO),
                anyString(), startsWith("Cambió la fecha."), eq(Notificacion.ReferenciaTipo.EVENTO), eq(5L));
    }
}
