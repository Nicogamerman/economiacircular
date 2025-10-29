package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.CrearMensajeDto;
import com.pp.economia_circular.DTO.RespuestaMensajeDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
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
class ServicioMensajeTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private ArticleRepository articuloRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ServicioMensaje servicioMensaje;

    private Usuario remitente;
    private Usuario destinatario;
    private Articulo articulo;
    private Mensaje mensaje;
    private CrearMensajeDto crearDto;

    @BeforeEach
    void setUp() {
        remitente = new Usuario();
        remitente.setId(1L);
        remitente.setEmail("remitente@example.com");
        remitente.setNombre("Remitente");

        destinatario = new Usuario();
        destinatario.setId(2L);
        destinatario.setEmail("destinatario@example.com");
        destinatario.setNombre("Destinatario");

        articulo = new Articulo();
        articulo.setId(1L);
        articulo.setTitulo("Test Article");
        articulo.setUsuario(destinatario);

        mensaje = new Mensaje();
        mensaje.setId(1L);
        mensaje.setContenido("Test Message");
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setArticulo(articulo);
        mensaje.setEstado(Mensaje.EstadoMensaje.NO_LEIDO);
        mensaje.setCreadoEn(LocalDateTime.now());

        crearDto = new CrearMensajeDto();
        crearDto.setContenido("Test Message");
    }

    @Test
    void enviarMensaje_ConDestinatario_Success() {
        // Arrange
        crearDto.setDestinatarioId(2L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        // Act
        RespuestaMensajeDto result = servicioMensaje.enviarMensaje(crearDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Message", result.getContenido());
        assertEquals(1L, result.getRemitenteId());
        assertEquals(2L, result.getDestinatarioId());
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
    }

    @Test
    void enviarMensaje_ConArticulo_Success() {
        // Arrange
        crearDto.setArticuloId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remitente));
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articulo));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        // Act
        RespuestaMensajeDto result = servicioMensaje.enviarMensaje(crearDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Message", result.getContenido());
        assertEquals(1L, result.getArticuloId());
        assertEquals(2L, result.getDestinatarioId()); // El dueño del artículo
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
    }

    @Test
    void enviarMensaje_SinDestinatarioNiArticulo_ThrowsException() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remitente));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.enviarMensaje(crearDto, 1L));
        assertEquals("Debe especificar un receptor o un artículo", exception.getMessage());
        verify(mensajeRepository, never()).save(any());
    }

    @Test
    void enviarMensaje_RemitenteNoEncontrado_ThrowsException() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.enviarMensaje(crearDto, 1L));
        assertEquals("Usuario emisor no encontrado", exception.getMessage());
    }

    @Test
    void enviarMensaje_DestinatarioNoEncontrado_ThrowsException() {
        // Arrange
        crearDto.setDestinatarioId(999L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.enviarMensaje(crearDto, 1L));
        assertEquals("Usuario receptor no encontrado", exception.getMessage());
    }

    @Test
    void enviarMensaje_ArticuloNoEncontrado_ThrowsException() {
        // Arrange
        crearDto.setArticuloId(999L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remitente));
        when(articuloRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.enviarMensaje(crearDto, 1L));
        assertEquals("Artículo no encontrado", exception.getMessage());
    }

    @Test
    void obtenerMisMensajes_Success() {
        // Arrange
        when(mensajeRepository.findConversationsByUser(1L))
            .thenReturn(Arrays.asList(mensaje));

        // Act
        List<RespuestaMensajeDto> result = servicioMensaje.obtenerMisMensajes(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Message", result.get(0).getContenido());
    }

    @Test
    void obtenerConversacionConUsuario_Success() {
        // Arrange
        when(mensajeRepository.findConversationBetweenUsers(1L, 2L))
            .thenReturn(Arrays.asList(mensaje));

        // Act
        List<RespuestaMensajeDto> result = servicioMensaje.obtenerConversacionConUsuario(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getRemitenteId());
        assertEquals(2L, result.get(0).getDestinatarioId());
    }

    @Test
    void obtenerMensajesPorArticulo_Success() {
        // Arrange
        when(mensajeRepository.findByArticulo_Id(1L))
            .thenReturn(Arrays.asList(mensaje));

        // Act
        List<RespuestaMensajeDto> result = servicioMensaje.obtenerMensajesPorArticulo(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getArticuloId());
    }

    @Test
    void obtenerMensajesNoLeidos_Success() {
        // Arrange
        when(mensajeRepository.findUnreadMessagesByUser(2L))
            .thenReturn(Arrays.asList(mensaje));

        // Act
        List<RespuestaMensajeDto> result = servicioMensaje.obtenerMensajesNoLeidos(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void contarMensajesNoLeidos_Success() {
        // Arrange
        when(mensajeRepository.countUnreadMessagesByUser(2L)).thenReturn(5L);

        // Act
        Long result = servicioMensaje.contarMensajesNoLeidos(2L);

        // Assert
        assertEquals(5L, result);
    }

    @Test
    void marcarComoLeido_Success() {
        // Arrange
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        // Act
        RespuestaMensajeDto result = servicioMensaje.marcarComoLeido(1L, 2L);

        // Assert
        assertNotNull(result);
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
        assertEquals(Mensaje.EstadoMensaje.LEIDO, mensaje.getEstado());
        assertNotNull(mensaje.getLeidoEn());
    }

    @Test
    void marcarComoLeido_NoEsDestinatario_ThrowsException() {
        // Arrange
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.marcarComoLeido(1L, 999L));
        assertEquals("No tienes permisos para marcar este mensaje como leído", exception.getMessage());
    }

    @Test
    void marcarComoLeido_MensajeNoEncontrado_ThrowsException() {
        // Arrange
        when(mensajeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.marcarComoLeido(999L, 2L));
        assertEquals("Mensaje no encontrado", exception.getMessage());
    }

    @Test
    void eliminarMensaje_ComoRemitente_Success() {
        // Arrange
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        // Act
        servicioMensaje.eliminarMensaje(1L, 1L);

        // Assert
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
        assertEquals(Mensaje.EstadoMensaje.ELIMINADO, mensaje.getEstado());
    }

    @Test
    void eliminarMensaje_ComoDestinatario_Success() {
        // Arrange
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        // Act
        servicioMensaje.eliminarMensaje(1L, 2L);

        // Assert
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
        assertEquals(Mensaje.EstadoMensaje.ELIMINADO, mensaje.getEstado());
    }

    @Test
    void eliminarMensaje_SinPermisos_ThrowsException() {
        // Arrange
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.eliminarMensaje(1L, 999L));
        assertEquals("No tienes permisos para eliminar este mensaje", exception.getMessage());
    }

    @Test
    void eliminarMensaje_MensajeNoEncontrado_ThrowsException() {
        // Arrange
        when(mensajeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> servicioMensaje.eliminarMensaje(999L, 1L));
        assertEquals("Mensaje no encontrado", exception.getMessage());
    }
}

