package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.VideoArticuloDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.VideoArticulo;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.VideoArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Un video por artículo, con la opción de reproducirlo con o sin audio. */
@Service
@Transactional
public class VideoArticuloService {

    static final List<String> TIPOS_PERMITIDOS = Arrays.asList("video/mp4", "video/webm", "video/quicktime");

    @Value("${app.article-video.max-size-bytes:31457280}")
    private long maxBytes = 30L * 1024 * 1024;

    @Autowired
    private VideoArticuloRepository videoRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    /** Sube o reemplaza el video del artículo. */
    public VideoArticuloDto subir(Long articuloId, MultipartFile archivo, boolean conAudio) {
        Articulo articulo = articuloPropio(articuloId);

        if (archivo == null || archivo.isEmpty()) throw new RuntimeException("El archivo está vacío");
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new RuntimeException("Formato no permitido. Usá MP4, WEBM o MOV");
        }
        if (archivo.getSize() > maxBytes) {
            throw new RuntimeException("El video no puede superar " + (maxBytes / (1024 * 1024)) + " MB");
        }

        VideoArticulo video = videoRepository.findByArticulo_Id(articuloId).orElseGet(VideoArticulo::new);
        video.setArticulo(articulo);
        video.setNombreArchivo(archivo.getOriginalFilename());
        video.setContentType(contentType);
        video.setTamanoBytes(archivo.getSize());
        video.setConAudio(conAudio);
        video.setCreadoEn(LocalDateTime.now());
        try {
            video.setContenido(archivo.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo");
        }
        VideoArticulo guardado = videoRepository.save(video);
        return toDto(guardado, articuloId);
    }

    @Transactional(readOnly = true)
    public Optional<VideoArticuloDto> metadatos(Long articuloId) {
        return videoRepository.findMetadatos(articuloId);
    }

    @Transactional(readOnly = true)
    public VideoArticulo obtenerConContenido(Long articuloId) {
        return videoRepository.findByArticulo_Id(articuloId)
                .orElseThrow(() -> new RuntimeException("El artículo no tiene video"));
    }

    public VideoArticuloDto cambiarAudio(Long articuloId, boolean conAudio) {
        articuloPropio(articuloId);
        VideoArticulo video = obtenerConContenido(articuloId);
        video.setConAudio(conAudio);
        return toDto(videoRepository.save(video), articuloId);
    }

    public void eliminar(Long articuloId) {
        articuloPropio(articuloId);
        videoRepository.findByArticulo_Id(articuloId).ifPresent(videoRepository::delete);
    }

    private Articulo articuloPropio(Long articuloId) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        Articulo articulo = articleRepository.findById(articuloId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        boolean esAdmin = usuario.getRol() != null && usuario.getRol().contains("ADMIN");
        if (!esAdmin && !articulo.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tenés permiso para modificar el video de este artículo");
        }
        return articulo;
    }

    private VideoArticuloDto toDto(VideoArticulo v, Long articuloId) {
        return new VideoArticuloDto(v.getId(), articuloId, v.getNombreArchivo(), v.getContentType(),
                v.getTamanoBytes(), v.isConAudio(), v.getCreadoEn());
    }
}
