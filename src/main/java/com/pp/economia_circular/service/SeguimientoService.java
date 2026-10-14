package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.entity.*;
import com.pp.economia_circular.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Seguir oferentes y eventos/ferias, preferencias de tipos de evento
 * y las notificaciones que se derivan de eso.
 */
@Service
@Transactional
public class SeguimientoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private InteresEventoRepository interesEventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private NotificacionService notificacionService;

    // @Lazy: ArticleService también depende de este servicio (avisos a seguidores)
    @Lazy
    @Autowired(required = false)
    private ArticleService articleService;

    // ---------- Seguir / dejar de seguir ----------

    public Map<String, Object> seguir(Seguimiento.TipoSeguimiento tipo, Long objetivoId) {
        Usuario usuario = requerirUsuario();
        validarObjetivo(usuario, tipo, objetivoId);

        if (!seguimientoRepository.existsByUsuario_IdAndTipoAndObjetivoId(usuario.getId(), tipo, objetivoId)) {
            seguimientoRepository.save(new Seguimiento(usuario, tipo, objetivoId));
        }
        return estado(usuario.getId(), tipo, objetivoId);
    }

    public Map<String, Object> dejarDeSeguir(Seguimiento.TipoSeguimiento tipo, Long objetivoId) {
        Usuario usuario = requerirUsuario();
        seguimientoRepository.findByUsuario_IdAndTipoAndObjetivoId(usuario.getId(), tipo, objetivoId)
                .ifPresent(seguimientoRepository::delete);
        return estado(usuario.getId(), tipo, objetivoId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> estado(Seguimiento.TipoSeguimiento tipo, Long objetivoId) {
        Usuario usuario = authService.getCurrentUser();
        return estado(usuario != null ? usuario.getId() : null, tipo, objetivoId);
    }

    private Map<String, Object> estado(Long usuarioId, Seguimiento.TipoSeguimiento tipo, Long objetivoId) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("tipo", tipo);
        resultado.put("objetivoId", objetivoId);
        resultado.put("siguiendo", usuarioId != null
                && seguimientoRepository.existsByUsuario_IdAndTipoAndObjetivoId(usuarioId, tipo, objetivoId));
        resultado.put("seguidores", seguimientoRepository.countByTipoAndObjetivoId(tipo, objetivoId));
        return resultado;
    }

    private void validarObjetivo(Usuario usuario, Seguimiento.TipoSeguimiento tipo, Long objetivoId) {
        if (objetivoId == null) throw new RuntimeException("Falta el id a seguir");
        if (tipo == Seguimiento.TipoSeguimiento.OFERENTE) {
            if (usuario.getId().equals(objetivoId)) {
                throw new RuntimeException("No podés seguirte a vos mismo");
            }
            if (!usuarioRepository.existsById(objetivoId)) {
                throw new RuntimeException("Oferente no encontrado");
            }
        } else if (!eventRepository.existsById(objetivoId)) {
            throw new RuntimeException("Evento no encontrado");
        }
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public Map<String, Object> misSeguimientos() {
        Usuario usuario = requerirUsuario();
        List<Seguimiento> seguimientos = seguimientoRepository.findByUsuario_IdOrderByCreadoEnDesc(usuario.getId());

        List<Map<String, Object>> oferentes = new ArrayList<>();
        List<Map<String, Object>> eventos = new ArrayList<>();

        for (Seguimiento s : seguimientos) {
            if (s.getTipo() == Seguimiento.TipoSeguimiento.OFERENTE) {
                usuarioRepository.findById(s.getObjetivoId()).ifPresent(o -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", o.getId());
                    item.put("nombre", nombreCompleto(o));
                    item.put("email", o.getEmail());
                    item.put("articulosDisponibles",
                            articleRepository.countByUsuario_IdAndEstado(o.getId(), Articulo.EstadoArticulo.DISPONIBLE));
                    item.put("siguiendoDesde", s.getCreadoEn());
                    oferentes.add(item);
                });
            } else {
                eventRepository.findById(s.getObjetivoId()).ifPresent(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", e.getId());
                    item.put("nombre", e.getEventName());
                    item.put("fecha", e.getEventDate());
                    item.put("lugar", e.getLocation());
                    item.put("tipo", e.getEventType());
                    item.put("estado", e.getStatus());
                    eventos.add(item);
                });
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("oferentes", oferentes);
        resultado.put("eventos", eventos);
        resultado.put("intereses", misIntereses());
        return resultado;
    }

    /** Artículos disponibles publicados por los oferentes que sigo, del más nuevo al más viejo. */
    @Transactional(readOnly = true)
    public Page<ArticleResponseDto> feed(Pageable pageable) {
        Usuario usuario = requerirUsuario();
        List<Long> oferentes = seguimientoRepository.findObjetivoIds(usuario.getId(), Seguimiento.TipoSeguimiento.OFERENTE);
        if (oferentes.isEmpty()) {
            return Page.empty(pageable);
        }
        return articleRepository
                .findByUsuario_IdInAndEstadoOrderByCreadoEnDesc(oferentes, Articulo.EstadoArticulo.DISPONIBLE, pageable)
                .map(a -> articleService != null ? articleService.convertToResponseDto(a) : resumen(a));
    }

    private ArticleResponseDto resumen(Articulo a) {
        ArticleResponseDto dto = new ArticleResponseDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitulo());
        dto.setDescription(a.getDescripcion());
        dto.setCategory(a.getCategoria());
        dto.setStatus(a.getEstado());
        return dto;
    }

    // ---------- Intereses por tipo de evento ----------

    @Transactional(readOnly = true)
    public List<Event.EventType> misIntereses() {
        Usuario usuario = requerirUsuario();
        return interesEventoRepository.findByUsuario_Id(usuario.getId()).stream()
                .map(InteresEvento::getTipoEvento)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Event.EventType> actualizarIntereses(Collection<Event.EventType> tipos) {
        Usuario usuario = requerirUsuario();
        interesEventoRepository.deleteByUsuarioId(usuario.getId());
        Set<Event.EventType> unicos = EnumSet.noneOf(Event.EventType.class);
        if (tipos != null) {
            for (Event.EventType tipo : tipos) {
                if (tipo != null) unicos.add(tipo);
            }
        }
        for (Event.EventType tipo : unicos) {
            interesEventoRepository.save(new InteresEvento(usuario, tipo));
        }
        return new ArrayList<>(unicos);
    }

    // ---------- Notificaciones derivadas ----------

    /** Avisa a los seguidores de un oferente que publicó (o le aprobaron) un artículo. */
    public int notificarNuevoArticulo(Articulo articulo) {
        if (notificacionService == null || articulo == null || articulo.getUsuario() == null) return 0;
        Usuario oferente = articulo.getUsuario();
        List<Seguimiento> seguidores = seguimientoRepository.findSeguidores(
                Seguimiento.TipoSeguimiento.OFERENTE, oferente.getId());
        for (Seguimiento s : seguidores) {
            notificacionService.crear(
                    s.getUsuario(), oferente,
                    Notificacion.TipoNotificacion.NUEVO_ARTICULO_SEGUIDO,
                    nombreCompleto(oferente) + " publicó un artículo nuevo",
                    articulo.getTitulo(),
                    Notificacion.ReferenciaTipo.ARTICULO, articulo.getId());
        }
        return seguidores.size();
    }

    /** Avisa a quienes marcaron interés en el tipo de evento recién creado. */
    public int notificarEventoNuevo(Event evento) {
        if (notificacionService == null || evento == null || evento.getEventType() == null) return 0;
        List<InteresEvento> interesados = interesEventoRepository.findInteresados(evento.getEventType());
        for (InteresEvento i : interesados) {
            notificacionService.crear(
                    i.getUsuario(), evento.getUsuario(),
                    Notificacion.TipoNotificacion.EVENTO_NUEVO,
                    "Nuevo evento: " + evento.getEventName(),
                    describirEvento(evento),
                    Notificacion.ReferenciaTipo.EVENTO, evento.getId());
        }
        return interesados.size();
    }

    /** Avisa a los seguidores de un evento que cambió su fecha, lugar o estado. */
    public int notificarEventoActualizado(Event evento, String cambios) {
        if (notificacionService == null || evento == null) return 0;
        List<Seguimiento> seguidores = seguimientoRepository.findSeguidores(
                Seguimiento.TipoSeguimiento.EVENTO, evento.getId());
        for (Seguimiento s : seguidores) {
            notificacionService.crear(
                    s.getUsuario(), evento.getUsuario(),
                    Notificacion.TipoNotificacion.EVENTO_ACTUALIZADO,
                    "Novedades en \"" + evento.getEventName() + "\"",
                    cambios + " " + describirEvento(evento),
                    Notificacion.ReferenciaTipo.EVENTO, evento.getId());
        }
        return seguidores.size();
    }

    private String describirEvento(Event e) {
        String fecha = e.getEventDate() != null ? e.getEventDate().format(FORMATO_FECHA) : "fecha a confirmar";
        return fecha + " · " + (e.getLocation() != null ? e.getLocation() : "lugar a confirmar");
    }

    private String nombreCompleto(Usuario u) {
        String nombre = ((u.getNombre() != null ? u.getNombre() : "") + " "
                + (u.getApellido() != null ? u.getApellido() : "")).trim();
        return nombre.isEmpty() ? u.getEmail() : nombre;
    }

    private Usuario requerirUsuario() {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return usuario;
    }
}
