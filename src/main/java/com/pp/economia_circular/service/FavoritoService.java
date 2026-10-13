package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.FavoritoResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.EtiquetaArticulo;
import com.pp.economia_circular.entity.Favorito;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.FavoritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    public FavoritoResponseDto agregarFavorito(Long articuloId) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");

        Articulo articulo = articleRepository.findById(articuloId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        if (articulo.getEstado() == Articulo.EstadoArticulo.CANCELADO) {
            throw new RuntimeException("El artículo no está disponible");
        }

        if (favoritoRepository.existsByUsuario_IdAndArticulo_Id(usuario.getId(), articuloId)) {
            throw new RuntimeException("El artículo ya está en tus favoritos");
        }

        Favorito favorito = new Favorito(usuario, articulo);
        Favorito saved = favoritoRepository.save(favorito);
        return toDto(saved);
    }

    public void quitarFavorito(Long articuloId) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");

        if (!favoritoRepository.existsByUsuario_IdAndArticulo_Id(usuario.getId(), articuloId)) {
            throw new RuntimeException("El artículo no está en tus favoritos");
        }

        favoritoRepository.deleteByUsuario_IdAndArticulo_Id(usuario.getId(), articuloId);
    }

    @Transactional(readOnly = true)
    public Page<FavoritoResponseDto> listarFavoritos(Pageable pageable) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");

        return favoritoRepository.findByUsuario_Id(usuario.getId(), pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public boolean esFavorito(Long articuloId) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) return false;
        return favoritoRepository.existsByUsuario_IdAndArticulo_Id(usuario.getId(), articuloId);
    }

    @Transactional(readOnly = true)
    public long contarFavoritos(Long articuloId) {
        return favoritoRepository.countByArticulo_Id(articuloId);
    }

    private FavoritoResponseDto toDto(Favorito favorito) {
        ArticleResponseDto artDto = articuloToDto(favorito.getArticulo());
        return new FavoritoResponseDto(favorito.getId(), favorito.getCreadoEn(), artDto);
    }

    private ArticleResponseDto articuloToDto(Articulo a) {
        ArticleResponseDto dto = new ArticleResponseDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitulo());
        dto.setDescription(a.getDescripcion());
        dto.setCategory(a.getCategoria());
        dto.setSubcategoria(a.getSubcategoria());
        dto.setMarca(a.getMarca());
        dto.setModelo(a.getModelo());
        dto.setCondition(a.getCondicion());
        dto.setStatus(a.getEstado());
        if (a.getEtiquetas() != null) {
            dto.setEtiquetas(a.getEtiquetas().stream()
                    .map(EtiquetaArticulo::getEtiqueta)
                    .collect(Collectors.toList()));
        }
        if (a.getUsuario() != null) {
            dto.setUserId(a.getUsuario().getId());
            dto.setUsername(a.getUsuario().getEmail());
        }
        dto.setCreatedAt(a.getCreadoEn());
        dto.setUpdatedAt(a.getActualizadoEn());
        return dto;
    }
}
