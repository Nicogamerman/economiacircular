package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.VideoArticuloDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.VideoArticulo;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.VideoArticuloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoArticuloServiceTest {

    @Mock private VideoArticuloRepository videoRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private JWTService authService;

    @InjectMocks
    private VideoArticuloService service;

    private Usuario duenio;
    private Articulo articulo;

    @BeforeEach
    void setUp() {
        duenio = new Usuario();
        duenio.setId(1L);
        duenio.setRol("USER");
        articulo = new Articulo();
        articulo.setId(10L);
        articulo.setUsuario(duenio);
        lenient().when(authService.getCurrentUser()).thenReturn(duenio);
        lenient().when(articleRepository.findById(10L)).thenReturn(Optional.of(articulo));
        lenient().when(videoRepository.save(any(VideoArticulo.class))).thenAnswer(inv -> {
            VideoArticulo v = inv.getArgument(0);
            v.setId(7L);
            return v;
        });
    }

    @Test
    void subeUnVideoMp4SinAudio() {
        MockMultipartFile mp4 = new MockMultipartFile("file", "demo.mp4", "video/mp4", new byte[]{1, 2, 3});

        VideoArticuloDto dto = service.subir(10L, mp4, false);

        assertEquals(7L, dto.getId());
        assertEquals(10L, dto.getArticuloId());
        assertEquals("video/mp4", dto.getContentType());
        assertEquals(3L, dto.getTamanoBytes());
        assertFalse(dto.isConAudio());
    }

    @Test
    void reemplazaElVideoExistente() {
        VideoArticulo existente = new VideoArticulo();
        existente.setId(7L);
        when(videoRepository.findByArticulo_Id(10L)).thenReturn(Optional.of(existente));

        service.subir(10L, new MockMultipartFile("file", "nuevo.webm", "video/webm", new byte[]{9}), true);

        verify(videoRepository).save(existente);
        assertEquals("nuevo.webm", existente.getNombreArchivo());
    }

    @Test
    void rechazaFormatosQueNoSonVideo() {
        MockMultipartFile pdf = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[]{1});
        assertThrows(RuntimeException.class, () -> service.subir(10L, pdf, true));
        verify(videoRepository, never()).save(any());
    }

    @Test
    void rechazaVideosMasGrandesQueElLimite() {
        ReflectionTestUtils.setField(service, "maxBytes", 2L);
        MockMultipartFile grande = new MockMultipartFile("file", "x.mp4", "video/mp4", new byte[]{1, 2, 3});
        assertThrows(RuntimeException.class, () -> service.subir(10L, grande, true));
    }

    @Test
    void otroUsuarioNoPuedeSubirVideo() {
        Usuario otro = new Usuario();
        otro.setId(2L);
        otro.setRol("USER");
        when(authService.getCurrentUser()).thenReturn(otro);

        MockMultipartFile mp4 = new MockMultipartFile("file", "demo.mp4", "video/mp4", new byte[]{1});
        assertThrows(RuntimeException.class, () -> service.subir(10L, mp4, true));
    }

    @Test
    void cambiarAudioActualizaLaOpcion() {
        VideoArticulo existente = new VideoArticulo();
        existente.setConAudio(true);
        when(videoRepository.findByArticulo_Id(10L)).thenReturn(Optional.of(existente));

        assertFalse(service.cambiarAudio(10L, false).isConAudio());
    }

    @Test
    void obtenerSinVideoFalla() {
        when(videoRepository.findByArticulo_Id(10L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.obtenerConContenido(10L));
    }
}
