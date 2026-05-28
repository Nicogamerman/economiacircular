package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.MensajeSoporteEnviarDto;
import com.pp.economia_circular.DTO.MensajeSoporteResponseDto;
import com.pp.economia_circular.DTO.SoporteChatCreateDto;
import com.pp.economia_circular.DTO.SoporteChatResponseDto;
import com.pp.economia_circular.entity.MensajeSoporte;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.SoporteChat;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.MensajeSoporteRepository;
import com.pp.economia_circular.repositories.SoporteChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SoporteChatService {

    @Autowired
    private SoporteChatRepository chatRepository;

    @Autowired
    private MensajeSoporteRepository mensajeRepository;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private NotificacionService notificacionService;

    public SoporteChatResponseDto abrirChat(SoporteChatCreateDto dto) {
        Usuario usuario = requerirUsuario();

        SoporteChat chat = new SoporteChat();
        chat.setUsuario(usuario);
        chat.setAsunto(dto.getAsunto().trim());
        if (dto.getPrioridad() != null) chat.setPrioridad(dto.getPrioridad());

        SoporteChat saved = chatRepository.save(chat);

        MensajeSoporte primerMensaje = new MensajeSoporte(saved, usuario, dto.getMensajeInicial().trim());
        mensajeRepository.save(primerMensaje);

        return SoporteChatResponseDto.from(saved, 0);
    }

    public MensajeSoporteResponseDto enviarMensaje(Long chatId, MensajeSoporteEnviarDto dto) {
        Usuario emisor = requerirUsuario();
        SoporteChat chat = obtenerChatConAcceso(chatId, emisor);

        if (chat.getEstado() == SoporteChat.EstadoChat.CERRADO) {
            throw new RuntimeException("No se puede enviar mensajes en un chat cerrado");
        }

        MensajeSoporte mensaje = new MensajeSoporte(chat, emisor, dto.getContenido().trim());
        MensajeSoporte saved = mensajeRepository.save(mensaje);

        boolean esAdmin = emisor.getRol() != null && emisor.getRol().contains("ADMIN");
        if (chat.getEstado() == SoporteChat.EstadoChat.ABIERTO && esAdmin) {
            chat.setEstado(SoporteChat.EstadoChat.EN_PROGRESO);
            chatRepository.save(chat);
        }

        if (notificacionService != null) {
            Usuario destinatario = esAdmin ? chat.getUsuario() : null;
            if (destinatario != null) {
                notificacionService.crear(
                        destinatario,
                        emisor,
                        Notificacion.TipoNotificacion.MENSAJE_NUEVO,
                        "Nueva respuesta de soporte",
                        "Tienes una nueva respuesta en tu chat: " + chat.getAsunto(),
                        Notificacion.ReferenciaTipo.MENSAJE,
                        saved.getId()
                );
            }
        }

        return new MensajeSoporteResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<SoporteChatResponseDto> listarMisChats(Pageable pageable) {
        Usuario usuario = requerirUsuario();
        return chatRepository.findByUsuario_Id(usuario.getId(), pageable)
                .map(c -> SoporteChatResponseDto.from(c,
                        mensajeRepository.countByChat_IdAndLeidoFalse(c.getId())));
    }

    @Transactional(readOnly = true)
    public Page<SoporteChatResponseDto> listarTodos(SoporteChat.EstadoChat estado, Pageable pageable) {
        Page<SoporteChat> page = estado != null
                ? chatRepository.findByEstado(estado, pageable)
                : chatRepository.findAllOrderByActualizadoEnDesc(pageable);
        return page.map(c -> SoporteChatResponseDto.from(c,
                mensajeRepository.countByChat_IdAndLeidoFalse(c.getId())));
    }

    @Transactional(readOnly = true)
    public List<MensajeSoporteResponseDto> listarMensajes(Long chatId) {
        Usuario usuario = requerirUsuario();
        obtenerChatConAcceso(chatId, usuario);
        return mensajeRepository.findByChat_IdOrderByCreadoEnAsc(chatId).stream()
                .map(MensajeSoporteResponseDto::new)
                .collect(Collectors.toList());
    }

    public SoporteChatResponseDto cambiarEstado(Long chatId, SoporteChat.EstadoChat nuevoEstado) {
        Usuario usuario = requerirUsuario();
        if (usuario.getRol() == null || !usuario.getRol().contains("ADMIN")) {
            throw new RuntimeException("Solo los administradores pueden cambiar el estado del chat");
        }
        SoporteChat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        chat.setEstado(nuevoEstado);
        SoporteChat updated = chatRepository.save(chat);

        if (notificacionService != null && !chat.getUsuario().getId().equals(usuario.getId())) {
            notificacionService.crear(
                    chat.getUsuario(),
                    usuario,
                    Notificacion.TipoNotificacion.SISTEMA,
                    "Estado de soporte actualizado",
                    "Tu solicitud \"" + chat.getAsunto() + "\" cambió a: " + nuevoEstado.name(),
                    Notificacion.ReferenciaTipo.MENSAJE,
                    chatId
            );
        }

        long noLeidos = mensajeRepository.countByChat_IdAndLeidoFalse(chatId);
        return SoporteChatResponseDto.from(updated, noLeidos);
    }

    public int marcarMensajesLeidos(Long chatId) {
        Usuario usuario = requerirUsuario();
        obtenerChatConAcceso(chatId, usuario);
        return mensajeRepository.marcarLeidosEnChat(chatId, usuario.getId());
    }

    private SoporteChat obtenerChatConAcceso(Long chatId, Usuario usuario) {
        SoporteChat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        boolean esAdmin = usuario.getRol() != null && usuario.getRol().contains("ADMIN");
        boolean esOwner = chat.getUsuario().getId().equals(usuario.getId());
        if (!esOwner && !esAdmin) {
            throw new RuntimeException("No tienes acceso a este chat");
        }
        return chat;
    }

    private Usuario requerirUsuario() {
        Usuario u = authService.getCurrentUser();
        if (u == null) throw new RuntimeException("Usuario no autenticado");
        return u;
    }
}
