package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.entity.MensajeAudio;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeAudioRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    @Autowired(required = false)
    private NotificacionService notificacionService;

    @Autowired(required = false)
    private MensajeAudioRepository mensajeAudioRepository;

    static final List<String> TIPOS_AUDIO = Arrays.asList(
            "audio/webm", "audio/ogg", "audio/mp4", "audio/mpeg", "audio/wav", "audio/x-m4a", "audio/aac");
    static final long MAX_AUDIO_BYTES = 5L * 1024 * 1024;
    static final int MAX_AUDIO_SEGUNDOS = 180;

    // 🎤 Enviar mensaje de voz
    public Mensaje enviarAudio(Long destinatarioId, Long articuloId, MultipartFile audio, Integer duracionSeg) {
        if (mensajeAudioRepository == null) throw new RuntimeException("Mensajes de audio no disponibles");
        if (audio == null || audio.isEmpty()) throw new RuntimeException("El audio está vacío");
        String contentType = audio.getContentType() != null ? audio.getContentType().split(";")[0].trim() : null;
        if (contentType == null || !TIPOS_AUDIO.contains(contentType)) {
            throw new RuntimeException("Formato de audio no permitido");
        }
        if (audio.getSize() > MAX_AUDIO_BYTES) throw new RuntimeException("El audio no puede superar 5 MB");
        if (duracionSeg != null && duracionSeg > MAX_AUDIO_SEGUNDOS) {
            throw new RuntimeException("El audio no puede durar más de 3 minutos");
        }

        byte[] contenido;
        try {
            contenido = audio.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el audio");
        }

        Mensaje mensaje = enviarMensaje(destinatarioId, articuloId, "🎤 Mensaje de voz");
        mensaje.setTipo(Mensaje.TipoMensaje.AUDIO);
        mensaje.setAudioDuracionSeg(duracionSeg);
        Mensaje guardado = mensajeRepository.save(mensaje);

        MensajeAudio mensajeAudio = new MensajeAudio();
        mensajeAudio.setMensaje(guardado);
        mensajeAudio.setContentType(contentType);
        mensajeAudio.setContenido(contenido);
        mensajeAudioRepository.save(mensajeAudio);
        return guardado;
    }

    // 🔊 Audio de un mensaje (solo para remitente o destinatario)
    @Transactional(readOnly = true)
    public MensajeAudio obtenerAudio(Long mensajeId) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Usuario no autenticado");
        if (mensajeAudioRepository == null) throw new RuntimeException("Mensajes de audio no disponibles");

        Mensaje mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        boolean participa = (mensaje.getRemitente() != null && mensaje.getRemitente().getId().equals(currentUser.getId()))
                || (mensaje.getDestinatario() != null && mensaje.getDestinatario().getId().equals(currentUser.getId()));
        if (!participa) throw new RuntimeException("No tenés permiso para escuchar este mensaje");

        return mensajeAudioRepository.findByMensaje_Id(mensajeId)
                .orElseThrow(() -> new RuntimeException("El mensaje no tiene audio"));
    }

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
        Mensaje guardado = mensajeRepository.save(mensaje);

        if (notificacionService != null) {
            String tituloArticulo = articulo.getTitulo() != null ? articulo.getTitulo() : "tu artículo";
            String resumen = contenido != null && contenido.length() > 120
                    ? contenido.substring(0, 117) + "..."
                    : contenido;
            notificacionService.crear(
                    destinatario,
                    remitente,
                    Notificacion.TipoNotificacion.MENSAJE_NUEVO,
                    "Nuevo mensaje sobre \"" + tituloArticulo + "\"",
                    resumen,
                    Notificacion.ReferenciaTipo.MENSAJE,
                    guardado.getId()
            );
        }

        return guardado;
    }

    // 💬 Obtener conversación entre dos usuarios
    public List<Mensaje> obtenerConversacion(Long usuarioId1, Long usuarioId2) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Usuario no autenticado");

        boolean esAdmin = currentUser.getRol() != null && currentUser.getRol().contains("ADMIN");
        boolean esParticipante = currentUser.getId().equals(usuarioId1) || currentUser.getId().equals(usuarioId2);
        if (!esAdmin && !esParticipante) {
            throw new RuntimeException("No tenés permiso para ver esta conversación");
        }

        return mensajeRepository.findConversationBetweenUsers(usuarioId1, usuarioId2);
    }

    // 🧵 Obtener mensajes por artículo
    public List<Mensaje> obtenerMensajesPorArticulo(Long articuloId) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Usuario no autenticado");

        Articulo articulo = articleRepository.findById(articuloId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        boolean esAdmin = currentUser.getRol() != null && currentUser.getRol().contains("ADMIN");
        boolean esPropietario = articulo.getUsuario().getId().equals(currentUser.getId());

        List<Mensaje> mensajes = mensajeRepository.findByArticulo_Id(articuloId);

        if (!esAdmin && !esPropietario) {
            List<Mensaje> propios = mensajes.stream()
                    .filter(m -> m.getRemitente().getId().equals(currentUser.getId())
                              || m.getDestinatario().getId().equals(currentUser.getId()))
                    .collect(java.util.stream.Collectors.toList());
            if (propios.isEmpty()) {
                throw new RuntimeException("No tenés permiso para ver estos mensajes");
            }
            return propios;
        }

        return mensajes;
    }

    // ✅ Marcar mensaje como leído
    public Mensaje marcarComoLeido(Long mensajeId) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Usuario no autenticado");

        Mensaje mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        if (!mensaje.getDestinatario().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No podés marcar como leído un mensaje que no es tuyo");
        }

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

    // 🗂️ Todos los mensajes (enviados y recibidos) del usuario autenticado, más recientes primero
    public List<Mensaje> obtenerMisMensajes() {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return mensajeRepository.findConversationsByUser(currentUser.getId());
    }
}
