package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.MensajeSoporteEnviarDto;
import com.pp.economia_circular.DTO.MensajeSoporteResponseDto;
import com.pp.economia_circular.DTO.SoporteChatCreateDto;
import com.pp.economia_circular.DTO.SoporteChatResponseDto;
import com.pp.economia_circular.entity.MensajeSoporte;
import com.pp.economia_circular.entity.SoporteChat;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.MensajeSoporteRepository;
import com.pp.economia_circular.repositories.SoporteChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoporteChatServiceTest {

    @Mock
    private SoporteChatRepository chatRepository;

    @Mock
    private MensajeSoporteRepository mensajeRepository;

    @Mock
    private JWTService authService;

    @InjectMocks
    private SoporteChatService soporteService;

    private Usuario usuario;
    private Usuario admin;
    private SoporteChat chat;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setNombre("Laura");
        usuario.setApellido("Torres");
        usuario.setRol("USER");

        admin = new Usuario();
        admin.setId(99L);
        admin.setEmail("admin@test.com");
        admin.setNombre("Admin");
        admin.setApellido("Sistema");
        admin.setRol("ADMIN");

        chat = new SoporteChat();
        chat.setId(1L);
        chat.setUsuario(usuario);
        chat.setAsunto("Problema con imagen");
        chat.setEstado(SoporteChat.EstadoChat.ABIERTO);
        chat.setPrioridad(SoporteChat.PrioridadChat.MEDIA);
    }

    @Test
    void abrirChat_Success() {
        SoporteChatCreateDto dto = new SoporteChatCreateDto();
        dto.setAsunto("Mi consulta");
        dto.setMensajeInicial("Necesito ayuda");
        dto.setPrioridad(SoporteChat.PrioridadChat.ALTA);

        when(authService.getCurrentUser()).thenReturn(usuario);
        when(chatRepository.save(any(SoporteChat.class))).thenReturn(chat);
        when(mensajeRepository.save(any(MensajeSoporte.class))).thenReturn(new MensajeSoporte());

        SoporteChatResponseDto result = soporteService.abrirChat(dto);

        assertNotNull(result);
        verify(chatRepository).save(any(SoporteChat.class));
        verify(mensajeRepository).save(any(MensajeSoporte.class));
    }

    @Test
    void abrirChat_NoAutenticado_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(null);
        SoporteChatCreateDto dto = new SoporteChatCreateDto();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> soporteService.abrirChat(dto));
        assertEquals("Usuario no autenticado", ex.getMessage());
    }

    @Test
    void enviarMensaje_Usuario_Success() {
        MensajeSoporteEnviarDto dto = new MensajeSoporteEnviarDto();
        dto.setContenido("¿Me pueden ayudar?");

        MensajeSoporte msg = new MensajeSoporte(chat, usuario, dto.getContenido());
        msg.setId(1L);

        when(authService.getCurrentUser()).thenReturn(usuario);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(mensajeRepository.save(any())).thenReturn(msg);

        MensajeSoporteResponseDto result = soporteService.enviarMensaje(1L, dto);

        assertNotNull(result);
        verify(mensajeRepository).save(any(MensajeSoporte.class));
    }

    @Test
    void enviarMensaje_ChatCerrado_ThrowsException() {
        chat.setEstado(SoporteChat.EstadoChat.CERRADO);
        MensajeSoporteEnviarDto dto = new MensajeSoporteEnviarDto();
        dto.setContenido("Mensaje");

        when(authService.getCurrentUser()).thenReturn(usuario);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> soporteService.enviarMensaje(1L, dto));
        assertEquals("No se puede enviar mensajes en un chat cerrado", ex.getMessage());
    }

    @Test
    void enviarMensaje_SinAcceso_ThrowsException() {
        Usuario otro = new Usuario();
        otro.setId(5L);
        otro.setRol("USER");

        MensajeSoporteEnviarDto dto = new MensajeSoporteEnviarDto();
        dto.setContenido("Hola");

        when(authService.getCurrentUser()).thenReturn(otro);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> soporteService.enviarMensaje(1L, dto));
        assertEquals("No tienes acceso a este chat", ex.getMessage());
    }

    @Test
    void listarMensajes_Success() {
        MensajeSoporte msg = new MensajeSoporte(chat, usuario, "Hola");
        msg.setId(1L);

        when(authService.getCurrentUser()).thenReturn(usuario);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(mensajeRepository.findByChat_IdOrderByCreadoEnAsc(1L)).thenReturn(Arrays.asList(msg));

        List<MensajeSoporteResponseDto> result = soporteService.listarMensajes(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void cambiarEstado_SoloAdmin_Success() {
        when(authService.getCurrentUser()).thenReturn(admin);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(chatRepository.save(any())).thenReturn(chat);
        when(mensajeRepository.countByChat_IdAndLeidoFalse(1L)).thenReturn(0L);

        SoporteChatResponseDto result = soporteService.cambiarEstado(1L, SoporteChat.EstadoChat.RESUELTO);

        assertNotNull(result);
        verify(chatRepository).save(any());
    }

    @Test
    void cambiarEstado_NoAdmin_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> soporteService.cambiarEstado(1L, SoporteChat.EstadoChat.RESUELTO));
        assertEquals("Solo los administradores pueden cambiar el estado del chat", ex.getMessage());
    }
}
