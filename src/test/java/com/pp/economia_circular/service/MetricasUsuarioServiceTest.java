package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Seguimiento;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricasUsuarioServiceTest {

    @Mock private ArticleRepository articleRepository;
    @Mock private SolicitudIntercambioRepository solicitudRepository;
    @Mock private VistaArticuloRepository vistaRepository;
    @Mock private FavoritoRepository favoritoRepository;
    @Mock private SeguimientoRepository seguimientoRepository;
    @Mock private ValoracionRepository valoracionRepository;
    @Mock private JWTService authService;

    @InjectMocks
    private MetricasUsuarioService service;

    private Usuario yo;
    private Usuario otro;

    @BeforeEach
    void setUp() {
        yo = new Usuario();
        yo.setId(1L);
        otro = new Usuario();
        otro.setId(2L);
    }

    private Articulo articulo(Long id, Usuario duenio, Articulo.CategoriaArticulo cat, Articulo.EstadoArticulo estado) {
        Articulo a = new Articulo();
        a.setId(id);
        a.setUsuario(duenio);
        a.setCategoria(cat);
        a.setEstado(estado);
        a.setCreadoEn(LocalDateTime.now());
        return a;
    }

    private SolicitudIntercambio intercambio(Articulo solicitado, Articulo ofrecido, Usuario solicitante,
                                             SolicitudIntercambio.EstadoIntercambio estado) {
        SolicitudIntercambio s = new SolicitudIntercambio(solicitado, ofrecido, solicitante);
        s.setEstado(estado);
        s.setActualizadoEn(LocalDateTime.now());
        return s;
    }

    @Test
    @SuppressWarnings("unchecked")
    void calculaMetricasDeOferenteIntercambiosEImpacto() {
        Articulo miElectronico = articulo(10L, yo, Articulo.CategoriaArticulo.ELECTRONICOS, Articulo.EstadoArticulo.INTERCAMBIADO);
        Articulo miRopa = articulo(11L, yo, Articulo.CategoriaArticulo.ROPA, Articulo.EstadoArticulo.DISPONIBLE);
        Articulo suMueble = articulo(20L, otro, Articulo.CategoriaArticulo.MUEBLES, Articulo.EstadoArticulo.INTERCAMBIADO);
        Articulo suLibro = articulo(21L, otro, Articulo.CategoriaArticulo.LIBROS, Articulo.EstadoArticulo.DISPONIBLE);

        List<SolicitudIntercambio> intercambios = Arrays.asList(
                // yo pedí su mueble ofreciendo mi electrónico, completado
                intercambio(suMueble, miElectronico, yo, SolicitudIntercambio.EstadoIntercambio.COMPLETADO),
                // él pidió mi ropa, la rechacé
                intercambio(miRopa, suLibro, otro, SolicitudIntercambio.EstadoIntercambio.RECHAZADO),
                // él pidió mi ropa, la acepté
                intercambio(miRopa, suLibro, otro, SolicitudIntercambio.EstadoIntercambio.ACEPTADO),
                // él pidió mi ropa, sigue pendiente (no cuenta para la tasa)
                intercambio(miRopa, suLibro, otro, SolicitudIntercambio.EstadoIntercambio.PENDIENTE));

        when(authService.getCurrentUser()).thenReturn(yo);
        when(articleRepository.findByUsuario_Id(1L)).thenReturn(Arrays.asList(miElectronico, miRopa));
        when(solicitudRepository.findAllByParticipante(1L)).thenReturn(intercambios);
        when(vistaRepository.countByPropietario(1L)).thenReturn(42L);
        when(favoritoRepository.countByPropietario(1L)).thenReturn(5L);
        when(seguimientoRepository.countByTipoAndObjetivoId(Seguimiento.TipoSeguimiento.OFERENTE, 1L)).thenReturn(3L);
        when(valoracionRepository.calcularPromedioDeUsuario(1L)).thenReturn(4.333);
        when(valoracionRepository.contarValoracionesDeUsuario(1L)).thenReturn(3L);

        Map<String, Object> m = service.misMetricas();

        Map<String, Object> oferente = (Map<String, Object>) m.get("oferente");
        assertEquals(2L, oferente.get("articulosPublicados"));
        assertEquals(42L, oferente.get("vistasTotales"));
        assertEquals(5L, oferente.get("favoritosRecibidos"));
        assertEquals(3L, oferente.get("seguidores"));
        assertEquals(4.3, oferente.get("valoracionPromedio"));
        Map<String, Long> porEstado = (Map<String, Long>) oferente.get("articulosPorEstado");
        assertEquals(1L, porEstado.get("DISPONIBLE"));
        assertEquals(1L, porEstado.get("INTERCAMBIADO"));

        Map<String, Object> inter = (Map<String, Object>) m.get("intercambios");
        assertEquals(4L, inter.get("total"));
        assertEquals(1L, inter.get("enviados"));
        assertEquals(3L, inter.get("recibidos"));
        assertEquals(1L, inter.get("completados"));
        // 2 recibidas respondidas (1 rechazada, 1 aceptada) => 50 %
        assertEquals(50.0, inter.get("tasaAceptacion"));

        Map<String, Object> impacto = (Map<String, Object>) m.get("impacto");
        // El artículo propio reutilizado es el electrónico ofrecido: 3 kg CO2, 1.5 kg residuos
        assertEquals(1L, impacto.get("articulosReutilizados"));
        assertEquals(3.0, impacto.get("co2EvitadoKg"));
        assertEquals(1.5, impacto.get("residuosEvitadosKg"));

        List<Map<String, Object>> actividad = (List<Map<String, Object>>) m.get("actividadMensual");
        assertEquals(MetricasUsuarioService.MESES_ACTIVIDAD, actividad.size());
        Map<String, Object> mesActual = actividad.get(actividad.size() - 1);
        assertEquals(2L, mesActual.get("articulosPublicados"));
        assertEquals(1L, mesActual.get("intercambiosCompletados"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sinActividadDevuelveCerosYTasaNula() {
        when(authService.getCurrentUser()).thenReturn(yo);

        Map<String, Object> m = service.misMetricas();

        Map<String, Object> inter = (Map<String, Object>) m.get("intercambios");
        assertEquals(0L, inter.get("total"));
        assertNull(inter.get("tasaAceptacion"));
        Map<String, Object> impacto = (Map<String, Object>) m.get("impacto");
        assertEquals(0.0, impacto.get("co2EvitadoKg"));
    }

    @Test
    void sinSesionFalla() {
        when(authService.getCurrentUser()).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.misMetricas());
    }
}
