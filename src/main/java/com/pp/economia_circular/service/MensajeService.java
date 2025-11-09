package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    // 📩 Enviar mensaje
    public Mensaje enviarMensaje(Long destinatarioId, Long articuloId, String contenido) {
        Usuario remitente = authService.getCurrentUser();
        if (remitente == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));

        Articulo articulo = articleRepository.findById(articuloId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        Mensaje mensaje = new Mensaje(contenido, remitente, destinatario, articulo);
        return mensajeRepository.save(mensaje);
    }

    // 💬 Obtener conversación entre dos usuarios
    public List<Mensaje> obtenerConversacion(Long usuarioId1, Long usuarioId2) {
        return mensajeRepository.findConversationBetweenUsers(usuarioId1, usuarioId2);
    }

    // 🧵 Obtener mensajes por artículo
    public List<Mensaje> obtenerMensajesPorArticulo(Long articuloId) {
        return mensajeRepository.findByArticulo_Id(articuloId);
    }

    // ✅ Marcar mensaje como leído
    public Mensaje marcarComoLeido(Long mensajeId) {
        Mensaje mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        mensaje.setEstado(Mensaje.EstadoMensaje.LEIDO);
        mensaje.setLeidoEn(LocalDateTime.now());
        return mensajeRepository.save(mensaje);
    }

    // 🔢 Contar mensajes no leídos del usuario autenticado
    public Long contarMensajesNoLeidos() {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return mensajeRepository.countUnreadMessagesByUser(currentUser.getId());
    }

    // 📥 Obtener mensajes no leídos del usuario autenticado
    public List<Mensaje> obtenerMensajesNoLeidos() {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return mensajeRepository.findUnreadMessagesByUser(currentUser.getId());
    }
}
