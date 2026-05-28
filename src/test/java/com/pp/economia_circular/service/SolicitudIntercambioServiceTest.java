package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.CambiarEstadoSolicitudDto;
import com.pp.economia_circular.DTO.SolicitudCreateDto;
import com.pp.economia_circular.DTO.SolicitudResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.SolicitudIntercambioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudIntercambioServiceTest {

    @Mock
    private SolicitudIntercambioRepository solicitudRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private JWTService authService;

    @InjectMocks
    private SolicitudIntercambioService solicitudService;

    private Usuario solicitante;
    private Usuario propietario;
    private Articulo articuloSolicitado;
    private Articulo articuloOfrecido;
    private SolicitudIntercambio solicitud;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario();
        solicitante.setId(1L);
        solicitante.setEmail("solicitante@test.com");
        solicitante.setNombre("Ana");
        solicitante.setApellido("Garcia");
        solicitante.setRol("USER");

        propietario = new Usuario();
        propietario.setId(2L);
        propietario.setEmail("propietario@test.com");
        propietario.setNombre("Carlos");
        propietario.setApellido("Lopez");
        propietario.setRol("USER");

        articuloSolicitado = new Articulo();
        articuloSolicitado.setId(10L);
        articuloSolicitado.setTitulo("Bicicleta");
        articuloSolicitado.setEstado(Articulo.EstadoArticulo.DISPONIBLE);
        articuloSolicitado.setUsuario(propietario);

        articuloOfrecido = new Articulo();
        articuloOfrecido.setId(20L);
        articuloOfrecido.setTitulo("Raqueta");
        articuloOfrecido.setEstado(Articulo.EstadoArticulo.DISPONIBLE);
        articuloOfrecido.setUsuario(solicitante);

        solicitud = new SolicitudIntercambio(articuloSolicitado, articuloOfrecido, solicitante);
        solicitud.setId(1L);
    }

    @Test
    void crear_Success() {
        SolicitudCreateDto dto = new SolicitudCreateDto();
        dto.setArticuloSolicitadoId(10L);
        dto.setArticuloOfrecidoId(20L);

        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articuloSolicitado));
        when(articleRepository.findById(20L)).thenReturn(Optional.of(articuloOfrecido));
        when(solicitudRepository.existeSolicitudPendiente(1L, 10L, 20L)).thenReturn(false);
        when(solicitudRepository.save(any())).thenReturn(solicitud);

        SolicitudResponseDto result = solicitudService.crear(dto);

        assertNotNull(result);
        verify(solicitudRepository).save(any());
    }

    @Test
    void crear_PropioArticulo_ThrowsException() {
        articuloSolicitado.setUsuario(solicitante);
        SolicitudCreateDto dto = new SolicitudCreateDto();
        dto.setArticuloSolicitadoId(10L);
        dto.setArticuloOfrecidoId(20L);

        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articuloSolicitado));
        when(articleRepository.findById(20L)).thenReturn(Optional.of(articuloOfrecido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> solicitudService.crear(dto));
        assertEquals("No puedes solicitar tu propio artículo", ex.getMessage());
    }

    @Test
    void crear_ArticuloOfrecidoNoPertenece_ThrowsException() {
        articuloOfrecido.setUsuario(propietario);
        SolicitudCreateDto dto = new SolicitudCreateDto();
        dto.setArticuloSolicitadoId(10L);
        dto.setArticuloOfrecidoId(20L);

        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articuloSolicitado));
        when(articleRepository.findById(20L)).thenReturn(Optional.of(articuloOfrecido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> solicitudService.crear(dto));
        assertEquals("El artículo ofrecido no te pertenece", ex.getMessage());
    }

    @Test
    void crear_SolicitudDuplicada_ThrowsException() {
        SolicitudCreateDto dto = new SolicitudCreateDto();
        dto.setArticuloSolicitadoId(10L);
        dto.setArticuloOfrecidoId(20L);

        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articuloSolicitado));
        when(articleRepository.findById(20L)).thenReturn(Optional.of(articuloOfrecido));
        when(solicitudRepository.existeSolicitudPendiente(1L, 10L, 20L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> solicitudService.crear(dto));
        assertEquals("Ya existe una solicitud pendiente para este intercambio", ex.getMessage());
    }

    @Test
    void cambiarEstado_Aceptar_Success() {
        CambiarEstadoSolicitudDto dto = new CambiarEstadoSolicitudDto();
        dto.setNuevoEstado(SolicitudIntercambio.EstadoIntercambio.ACEPTADO);

        when(authService.getCurrentUser()).thenReturn(propietario);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any())).thenReturn(solicitud);

        SolicitudResponseDto result = solicitudService.cambiarEstado(1L, dto);

        assertNotNull(result);
        verify(solicitudRepository).save(any());
    }

    @Test
    void cambiarEstado_SolicitanteCancela_Success() {
        CambiarEstadoSolicitudDto dto = new CambiarEstadoSolicitudDto();
        dto.setNuevoEstado(SolicitudIntercambio.EstadoIntercambio.CANCELADO);

        when(authService.getCurrentUser()).thenReturn(solicitante);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any())).thenReturn(solicitud);

        SolicitudResponseDto result = solicitudService.cambiarEstado(1L, dto);

        assertNotNull(result);
    }

    @Test
    void cambiarEstado_PropietarioNoPuedeCancelar_ThrowsException() {
        CambiarEstadoSolicitudDto dto = new CambiarEstadoSolicitudDto();
        dto.setNuevoEstado(SolicitudIntercambio.EstadoIntercambio.CANCELADO);

        when(authService.getCurrentUser()).thenReturn(propietario);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> solicitudService.cambiarEstado(1L, dto));
        assertEquals("Solo el solicitante puede cancelar la solicitud", ex.getMessage());
    }

    @Test
    void listarMisSolicitudes_Success() {
        when(authService.getCurrentUser()).thenReturn(solicitante);
        Page<SolicitudIntercambio> page = new PageImpl<>(Arrays.asList(solicitud));
        when(solicitudRepository.findBySolicitante_Id(eq(1L), any())).thenReturn(page);

        Page<SolicitudResponseDto> result = solicitudService.listarMisSolicitudes(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void crear_NoAutenticado_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(null);
        SolicitudCreateDto dto = new SolicitudCreateDto();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> solicitudService.crear(dto));
        assertEquals("Usuario no autenticado", ex.getMessage());
    }
}
