package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ResumenValoracionDto;
import com.pp.economia_circular.DTO.ValoracionCreateDto;
import com.pp.economia_circular.DTO.ValoracionResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Valoracion;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.repositories.ValoracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    public ValoracionResponseDto crear(ValoracionCreateDto dto) {
        Usuario valorador = authService.getCurrentUser();
        if (valorador == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        if (dto.getValoradoId().equals(valorador.getId())) {
            throw new RuntimeException("No puedes valorarte a ti mismo");
        }

        Usuario valorado = usuarioRepository.findById(dto.getValoradoId())
                .orElseThrow(() -> new RuntimeException("Usuario a valorar no encontrado"));

        Articulo articulo = null;
        if (dto.getArticuloId() != null) {
            articulo = articleRepository.findById(dto.getArticuloId())
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        }

        valoracionRepository.findExistente(valorador.getId(), valorado.getId(), dto.getArticuloId())
                .ifPresent(v -> { throw new RuntimeException("Ya valoraste a este usuario en este contexto"); });

        Valoracion valoracion = new Valoracion();
        valoracion.setValorador(valorador);
        valoracion.setValorado(valorado);
        valoracion.setArticulo(articulo);
        valoracion.setPuntaje(dto.getPuntaje());
        valoracion.setComentario(dto.getComentario());

        return new ValoracionResponseDto(valoracionRepository.save(valoracion));
    }

    public ValoracionResponseDto actualizar(Long id, ValoracionCreateDto dto) {
        Usuario actual = authService.getCurrentUser();
        if (actual == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));

        if (!valoracion.getValorador().getId().equals(actual.getId())) {
            throw new RuntimeException("No puedes modificar una valoración que no es tuya");
        }

        valoracion.setPuntaje(dto.getPuntaje());
        valoracion.setComentario(dto.getComentario());
        return new ValoracionResponseDto(valoracionRepository.save(valoracion));
    }

    public void eliminar(Long id) {
        Usuario actual = authService.getCurrentUser();
        if (actual == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));

        boolean esAdmin = "ADMIN".equalsIgnoreCase(actual.getRol());
        if (!esAdmin && !valoracion.getValorador().getId().equals(actual.getId())) {
            throw new RuntimeException("No puedes eliminar una valoración que no es tuya");
        }

        valoracionRepository.delete(valoracion);
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponseDto> listarPorUsuarioValorado(Long usuarioId) {
        return valoracionRepository.findByValorado_IdOrderByCreadoEnDesc(usuarioId)
                .stream().map(ValoracionResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponseDto> listarPorArticulo(Long articuloId) {
        return valoracionRepository.findByArticulo_IdOrderByCreadoEnDesc(articuloId)
                .stream().map(ValoracionResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponseDto> listarMisValoracionesDadas() {
        Usuario actual = authService.getCurrentUser();
        if (actual == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return valoracionRepository.findByValorador_IdOrderByCreadoEnDesc(actual.getId())
                .stream().map(ValoracionResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumenValoracionDto obtenerResumen(Long usuarioId) {
        if (!usuarioRepository.findById(usuarioId).isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        Double promedio = valoracionRepository.calcularPromedioDeUsuario(usuarioId);
        Long total = valoracionRepository.contarValoracionesDeUsuario(usuarioId);
        return new ResumenValoracionDto(
                usuarioId,
                promedio,
                total,
                valoracionRepository.contarPorPuntaje(usuarioId, 5),
                valoracionRepository.contarPorPuntaje(usuarioId, 4),
                valoracionRepository.contarPorPuntaje(usuarioId, 3),
                valoracionRepository.contarPorPuntaje(usuarioId, 2),
                valoracionRepository.contarPorPuntaje(usuarioId, 1)
        );
    }
}
