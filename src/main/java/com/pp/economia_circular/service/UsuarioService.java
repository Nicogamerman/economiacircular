package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.PerfilUpdateDto;
import com.pp.economia_circular.DTO.UsuarioPerfilDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.repositories.ValoracionRepository;
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
public class UsuarioService {

    private static final List<String> CONTENT_TYPES_PERMITIDOS =
            Arrays.asList("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_FOTO_BYTES = 3 * 1024 * 1024; // 3 MB

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private ValoracionRepository valoracionRepository;

    @Autowired(required = false)
    private ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public UsuarioPerfilDto obtenerPerfilPublico(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return construirPerfil(u, false);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilDto obtenerMiPerfil() {
        Usuario u = requerirUsuario();
        return construirPerfil(u, true);
    }

    public UsuarioPerfilDto actualizarMiPerfil(PerfilUpdateDto dto) {
        Usuario u = requerirUsuario();
        if (dto.getNombre() != null) u.setNombre(dto.getNombre().trim());
        if (dto.getApellido() != null) u.setApellido(dto.getApellido().trim());
        if (dto.getDomicilio() != null) u.setDomicilio(dto.getDomicilio().trim());
        u.setActualizadoEn(LocalDateTime.now());
        Usuario saved = usuarioRepository.save(u);
        return construirPerfil(saved, true);
    }

    public void subirFoto(MultipartFile archivo) {
        Usuario u = requerirUsuario();

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new RuntimeException("Tipo de archivo no permitido. Use JPEG, PNG, WEBP o GIF");
        }
        if (archivo.getSize() > MAX_FOTO_BYTES) {
            throw new RuntimeException("La foto no puede superar 3 MB");
        }

        try {
            u.setFoto(archivo.getBytes());
            u.setActualizadoEn(LocalDateTime.now());
            usuarioRepository.save(u);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen");
        }
    }

    public void eliminarFoto() {
        Usuario u = requerirUsuario();
        u.setFoto(null);
        u.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(u);
    }

    @Transactional(readOnly = true)
    public byte[] obtenerFoto(Long usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (u.getFoto() == null || u.getFoto().length == 0) {
            throw new RuntimeException("El usuario no tiene foto");
        }
        return u.getFoto();
    }

    private UsuarioPerfilDto construirPerfil(Usuario u, boolean incluirDatosSensibles) {
        UsuarioPerfilDto dto = new UsuarioPerfilDto();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setRol(u.getRol());
        dto.setActivo(u.isActivo());
        dto.setTieneFoto(u.getFoto() != null && u.getFoto().length > 0);
        dto.setCreadoEn(u.getCreadoEn());

        if (incluirDatosSensibles) {
            dto.setEmail(u.getEmail());
            dto.setDomicilio(u.getDomicilio());
            dto.setEmailVerificado(u.isEmailVerificado());
        }

        if (valoracionRepository != null) {
            dto.setValoracionPromedio(valoracionRepository.calcularPromedioDeUsuario(u.getId()));
            dto.setCantidadValoraciones(valoracionRepository.contarValoracionesDeUsuario(u.getId()));
        }

        if (articleRepository != null) {
            dto.setArticulosDisponibles(
                    articleRepository.countByUsuario_IdAndEstado(u.getId(), Articulo.EstadoArticulo.DISPONIBLE));
        }

        return dto;
    }

    private Usuario requerirUsuario() {
        Usuario u = authService.getCurrentUser();
        if (u == null) throw new RuntimeException("Usuario no autenticado");
        return u;
    }
}
