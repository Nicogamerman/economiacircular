package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.PoliticaCreateDto;
import com.pp.economia_circular.DTO.PoliticaResponseDto;
import com.pp.economia_circular.entity.Politica;
import com.pp.economia_circular.repositories.PoliticaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoliticaServiceTest {

    @Mock
    private PoliticaRepository politicaRepository;

    @InjectMocks
    private PoliticaService politicaService;

    private Politica politica;
    private PoliticaCreateDto createDto;

    @BeforeEach
    void setUp() {
        politica = new Politica();
        politica.setId(1L);
        politica.setTipo(Politica.TipoPolitica.TERMINOS_USO);
        politica.setTitulo("Términos de uso");
        politica.setContenido("Contenido de los términos...");
        politica.setActivo(true);
        politica.setOrden(1);
        politica.setCreadoEn(LocalDateTime.now());
        politica.setActualizadoEn(LocalDateTime.now());

        createDto = new PoliticaCreateDto();
        createDto.setTipo(Politica.TipoPolitica.TERMINOS_USO);
        createDto.setTitulo("Términos de uso");
        createDto.setContenido("Contenido de los términos...");
        createDto.setActivo(true);
        createDto.setOrden(1);
    }

    @Test
    void listarActivas_SinFiltro_Success() {
        when(politicaRepository.findByActivoTrueOrderByOrdenAscCreadoEnAsc())
                .thenReturn(Arrays.asList(politica));

        List<PoliticaResponseDto> result = politicaService.listarActivas(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Términos de uso", result.get(0).getTitulo());
    }

    @Test
    void listarActivas_ConFiltroTipo_Success() {
        when(politicaRepository.findByTipoAndActivoTrueOrderByOrdenAsc(Politica.TipoPolitica.TERMINOS_USO))
                .thenReturn(Arrays.asList(politica));

        List<PoliticaResponseDto> result = politicaService.listarActivas(Politica.TipoPolitica.TERMINOS_USO);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtener_Success() {
        when(politicaRepository.findById(1L)).thenReturn(Optional.of(politica));

        PoliticaResponseDto result = politicaService.obtener(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(Politica.TipoPolitica.TERMINOS_USO, result.getTipo());
    }

    @Test
    void obtener_NoEncontrado_ThrowsException() {
        when(politicaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> politicaService.obtener(999L));
        assertEquals("Política no encontrada", ex.getMessage());
    }

    @Test
    void crear_Success() {
        when(politicaRepository.save(any(Politica.class))).thenReturn(politica);

        PoliticaResponseDto result = politicaService.crear(createDto);

        assertNotNull(result);
        verify(politicaRepository).save(any(Politica.class));
    }

    @Test
    void actualizar_Success() {
        when(politicaRepository.findById(1L)).thenReturn(Optional.of(politica));
        when(politicaRepository.save(any(Politica.class))).thenReturn(politica);

        PoliticaResponseDto result = politicaService.actualizar(1L, createDto);

        assertNotNull(result);
        verify(politicaRepository).save(any(Politica.class));
    }

    @Test
    void eliminar_Success() {
        when(politicaRepository.findById(1L)).thenReturn(Optional.of(politica));
        doNothing().when(politicaRepository).delete(politica);

        politicaService.eliminar(1L);

        verify(politicaRepository).delete(politica);
    }

    @Test
    void toggleActivo_DesactivaActiva_Success() {
        politica.setActivo(true);
        when(politicaRepository.findById(1L)).thenReturn(Optional.of(politica));
        when(politicaRepository.save(any(Politica.class))).thenAnswer(inv -> inv.getArgument(0));

        PoliticaResponseDto result = politicaService.toggleActivo(1L);

        assertFalse(result.isActivo());
    }

    @Test
    void toggleActivo_ActivaDesactivada_Success() {
        politica.setActivo(false);
        when(politicaRepository.findById(1L)).thenReturn(Optional.of(politica));
        when(politicaRepository.save(any(Politica.class))).thenAnswer(inv -> inv.getArgument(0));

        PoliticaResponseDto result = politicaService.toggleActivo(1L);

        assertTrue(result.isActivo());
    }
}
