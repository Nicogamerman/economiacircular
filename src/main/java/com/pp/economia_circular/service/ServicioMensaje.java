package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.CrearMensajeDto;
import com.pp.economia_circular.DTO.RespuestaMensajeDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioMensaje {
    
    @Autowired
    private MensajeRepository mensajeRepository;
    
    @Autowired
    private ArticleRepository articuloRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public RespuestaMensajeDto enviarMensaje(CrearMensajeDto crearDto, Long remitenteId) {
        Usuario remitente = usuarioRepository.findById(remitenteId)
                .orElseThrow(() -> new RuntimeException("Usuario emisor no encontrado"));

        Usuario destinatario = null;
        Articulo articulo = null;
        
        if (crearDto.getDestinatarioId() != null) {
            // Mensaje directo a usuario
            destinatario = usuarioRepository.findById(crearDto.getDestinatarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado"));
        } else if (crearDto.getArticuloId() != null) {
            // Mensaje relacionado con artículo
            articulo = articuloRepository.findById(crearDto.getArticuloId())
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
            destinatario = articulo.getUsuario();
        } else {
            throw new RuntimeException("Debe especificar un receptor o un artículo");
        }
        
        Mensaje mensaje = new Mensaje();
        mensaje.setContenido(crearDto.getContenido());
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setArticulo(articulo);
        
        Mensaje mensajeGuardado = mensajeRepository.save(mensaje);
        return convertirARespuestaDto(mensajeGuardado);
    }
    
    public List<RespuestaMensajeDto> obtenerMisMensajes(Long usuarioId) {
        return mensajeRepository.findConversationsByUser(usuarioId).stream()
                .map(this::convertirARespuestaDto)
                .collect(Collectors.toList());
    }
    
    public List<RespuestaMensajeDto> obtenerConversacionConUsuario(Long usuarioId, Long otroUsuarioId) {
        return mensajeRepository.findConversationBetweenUsers(usuarioId, otroUsuarioId).stream()
                .map(this::convertirARespuestaDto)
                .collect(Collectors.toList());
    }
    
    public List<RespuestaMensajeDto> obtenerMensajesPorArticulo(Long articuloId) {
        return mensajeRepository.findByArticulo_Id(articuloId).stream()
                .map(this::convertirARespuestaDto)
                .collect(Collectors.toList());
    }
    
    public List<RespuestaMensajeDto> obtenerMensajesNoLeidos(Long usuarioId) {
        return mensajeRepository.findUnreadMessagesByUser(usuarioId).stream()
                .map(this::convertirARespuestaDto)
                .collect(Collectors.toList());
    }
    
    public Long contarMensajesNoLeidos(Long usuarioId) {
        return mensajeRepository.countUnreadMessagesByUser(usuarioId);
    }
    
    public RespuestaMensajeDto marcarComoLeido(Long mensajeId, Long usuarioId) {
        Mensaje mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Verificar que el usuario sea el receptor del mensaje
        if (!mensaje.getDestinatario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permisos para marcar este mensaje como leído");
        }
        
        mensaje.setEstado(Mensaje.EstadoMensaje.LEIDO);
        mensaje.setLeidoEn(LocalDateTime.now());
        
        Mensaje mensajeActualizado = mensajeRepository.save(mensaje);
        return convertirARespuestaDto(mensajeActualizado);
    }
    
    public void eliminarMensaje(Long mensajeId, Long usuarioId) {
        Mensaje mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Verificar que el usuario sea el emisor o receptor del mensaje
        if (!mensaje.getRemitente().getId().equals(usuarioId) && 
            !mensaje.getDestinatario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permisos para eliminar este mensaje");
        }
        
        mensaje.setEstado(Mensaje.EstadoMensaje.ELIMINADO);
        mensajeRepository.save(mensaje);
    }
    
    private RespuestaMensajeDto convertirARespuestaDto(Mensaje mensaje) {
        RespuestaMensajeDto dto = new RespuestaMensajeDto();
        dto.setId(mensaje.getId());
        dto.setContenido(mensaje.getContenido());
        dto.setRemitenteId(mensaje.getRemitente().getId());
        dto.setNombreRemitente(mensaje.getRemitente().getEmail()); // Usando email como username por ahora
        dto.setDestinatarioId(mensaje.getDestinatario().getId());
        dto.setNombreDestinatario(mensaje.getDestinatario().getEmail()); // Usando email como username por ahora
        dto.setArticuloId(mensaje.getArticulo() != null ? mensaje.getArticulo().getId() : null);
        dto.setEstado(mensaje.getEstado());
        dto.setCreadoEn(mensaje.getCreadoEn());
        dto.setLeidoEn(mensaje.getLeidoEn());
        return dto;
    }
}
