package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.*;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeAudioRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeAudioServiceTest {

    @Mock private MensajeRepository mensajeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private JWTService authService;
    @Mock private MensajeAudioRepository mensajeAudioRepository;

    @InjectMocks
    private MensajeService service;

    private Usuario remitente;
    private Usuario destinatario;
    private Articulo articulo;

    @BeforeEach
    void setUp() {
        remitente = new Usuario();
        remitente.setId(1L);
        destinatario = new Usuario();
        destinatario.setId(2L);
        articulo = new Articulo();
        articulo.setId(10L);
        articulo.setUsuario(destinatario);
        lenient().when(authService.getCurrentUser()).thenReturn(remitente);
        lenient().when(usuarioRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        lenient().when(articleRepository.findById(10L)).thenReturn(Optional.of(articulo));
        lenient().when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
            Mensaje m = inv.getArgument(0);
            if (m.getId() == null) m.setId(50L);
            return m;
        });
    }

    @Test
    void enviaUnMensajeDeVozConSuAudio() {
        // Los navegadores mandan el content-type con codec: "audio/webm;codecs=opus"
        MockMultipartFile audio = new MockMultipartFile("audio", "voz.webm", "audio/webm;codecs=opus", new byte[]{1, 2});

        Mensaje mensaje = service.enviarAudio(2L, 10L, audio, 12);

        assertEquals(Mensaje.TipoMensaje.AUDIO, mensaje.getTipo());
        assertEquals(12, mensaje.getAudioDuracionSeg());
        ArgumentCaptor<MensajeAudio> captor = ArgumentCaptor.forClass(MensajeAudio.class);
        verify(mensajeAudioRepository).save(captor.capture());
        assertEquals("audio/webm", captor.getValue().getContentType());
        assertArrayEquals(new byte[]{1, 2}, captor.getValue().getContenido());
        assertSame(mensaje, captor.getValue().getMensaje());
    }

    @Test
    void rechazaArchivosQueNoSonAudio() {
        MockMultipartFile img = new MockMultipartFile("audio", "x.png", "image/png", new byte[]{1});
        assertThrows(RuntimeException.class, () -> service.enviarAudio(2L, 10L, img, 5));
        verify(mensajeAudioRepository, never()).save(any());
    }

    @Test
    void rechazaAudiosDemasiadoLargos() {
        MockMultipartFile audio = new MockMultipartFile("audio", "voz.webm", "audio/webm", new byte[]{1});
        assertThrows(RuntimeException.class,
                () -> service.enviarAudio(2L, 10L, audio, MensajeService.MAX_AUDIO_SEGUNDOS + 1));
    }

    @Test
    void soloLosParticipantesPuedenEscucharElAudio() {
        Mensaje mensaje = new Mensaje("🎤", remitente, destinatario, articulo);
        mensaje.setId(50L);
        when(mensajeRepository.findById(50L)).thenReturn(Optional.of(mensaje));

        Usuario intruso = new Usuario();
        intruso.setId(9L);
        when(authService.getCurrentUser()).thenReturn(intruso);

        assertThrows(RuntimeException.class, () -> service.obtenerAudio(50L));
    }

    @Test
    void elDestinatarioPuedeEscucharElAudio() {
        Mensaje mensaje = new Mensaje("🎤", remitente, destinatario, articulo);
        mensaje.setId(50L);
        MensajeAudio audio = new MensajeAudio();
        audio.setContentType("audio/webm");
        when(mensajeRepository.findById(50L)).thenReturn(Optional.of(mensaje));
        when(mensajeAudioRepository.findByMensaje_Id(50L)).thenReturn(Optional.of(audio));
        when(authService.getCurrentUser()).thenReturn(destinatario);

        assertSame(audio, service.obtenerAudio(50L));
    }
}
