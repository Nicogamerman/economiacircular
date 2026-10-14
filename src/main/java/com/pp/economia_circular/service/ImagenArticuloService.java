package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ImagenArticuloResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.ImagenArticulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.ImagenArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImagenArticuloService {

    @Value("${app.article-images.max-per-article:5}")
    private int maxPorArticulo;

    @Value("${app.article-images.max-size-bytes:5242880}")
    private long maxBytesPorImagen;

    @Autowired
    private ImagenArticuloRepository imagenRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    public ImagenArticuloResponseDto subir(Long articuloId, MultipartFile archivo, String descripcion, String baseUrl) {
        Usuario actual = authService.getCurrentUser();
        if (actual == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Articulo articulo = articleRepository.findById(articuloId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        if (!esDuenoOAdmin(articulo, actual)) {
            throw new RuntimeException("No tenés permiso para agregar imágenes a este artículo");
        }

        validarArchivo(archivo);

        long actuales = imagenRepository.countByArticulo_Id(articuloId);
        if (actuales >= maxPorArticulo) {
            throw new RuntimeException("Se alcanzó el límite de " + maxPorArticulo + " imágenes por artículo");
        }

        ImagenArticulo imagen = new ImagenArticulo();
        imagen.setArticulo(articulo);
        imagen.setNombreArchivo(archivo.getOriginalFilename());
        imagen.setContentType(archivo.getContentType());
        imagen.setTamanoBytes(archivo.getSize());
        imagen.setDescripcion(descripcion);
        try {
            imagen.setContenido(archivo.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo: " + e.getMessage());
        }

        ImagenArticulo guardada = imagenRepository.save(imagen);
        return new ImagenArticuloResponseDto(guardada, baseUrl);
    }

    @Transactional(readOnly = true)
    public List<ImagenArticuloResponseDto> listar(Long articuloId, String baseUrl) {
        if (!articleRepository.findById(articuloId).isPresent()) {
            throw new RuntimeException("Artículo no encontrado");
        }
        return imagenRepository.findByArticulo_IdOrderByCreadoEnAsc(articuloId)
                .stream()
                .map(img -> new ImagenArticuloResponseDto(img, baseUrl))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ImagenArticulo obtenerConContenido(Long articuloId, Long imagenId) {
        ImagenArticulo imagen = imagenRepository.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
        if (imagen.getArticulo() == null || !imagen.getArticulo().getId().equals(articuloId)) {
            throw new RuntimeException("La imagen no pertenece al artículo indicado");
        }
        if (imagen.getContenido() == null) {
            throw new RuntimeException("La imagen no tiene contenido");
        }
        return imagen;
    }

    public void eliminar(Long articuloId, Long imagenId) {
        Usuario actual = authService.getCurrentUser();
        if (actual == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        ImagenArticulo imagen = imagenRepository.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        if (imagen.getArticulo() == null || !imagen.getArticulo().getId().equals(articuloId)) {
            throw new RuntimeException("La imagen no pertenece al artículo indicado");
        }

        if (!esDuenoOAdmin(imagen.getArticulo(), actual)) {
            throw new RuntimeException("No tenés permiso para eliminar esta imagen");
        }

        imagenRepository.delete(imagen);
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }
        if (archivo.getSize() > maxBytesPorImagen) {
            throw new RuntimeException("La imagen supera el tamaño máximo permitido (" + maxBytesPorImagen + " bytes)");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new RuntimeException("El archivo no es una imagen válida (content-type: " + contentType + ")");
        }
    }

    private boolean esDuenoOAdmin(Articulo articulo, Usuario actual) {
        boolean esAdmin = "ADMIN".equalsIgnoreCase(actual.getRol());
        boolean esDueno = articulo.getUsuario() != null
                && articulo.getUsuario().getId().equals(actual.getId());
        return esAdmin || esDueno;
    }
}
