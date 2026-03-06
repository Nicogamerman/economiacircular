package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.OferenteValoracionCreateDto;
import com.pp.economia_circular.DTO.OferenteValoracionResponseDto;
import com.pp.economia_circular.DTO.OferenteValoracionesSummaryDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.ValoracionOferente;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.OferenteValoracionRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OferenteValoracionService {

    @Autowired
    private OferenteValoracionRepository valoracionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService jwtService;

    public OferenteValoracionesSummaryDto getSummaryByOferenteId(Long oferenteId) {
        usuarioRepository.findById(oferenteId)
                .orElseThrow(() -> new RuntimeException("Oferente no encontrado"));

        List<OferenteValoracionResponseDto> reviews = valoracionRepository
                .findByOferente_IdOrderByCreadoEnDesc(oferenteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        Double averageRating = valoracionRepository.findAverageRatingByOferenteId(oferenteId);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        return OferenteValoracionesSummaryDto.builder()
                .oferenteId(oferenteId)
                .averageRating(averageRating)
                .totalReviews((long) reviews.size())
                .reviews(reviews)
                .build();
    }

    public OferenteValoracionResponseDto createReview(Long oferenteId, OferenteValoracionCreateDto createDto) {
        Usuario autor = jwtService.getCurrentUser();
        if (autor == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Usuario oferente = usuarioRepository.findById(oferenteId)
                .orElseThrow(() -> new RuntimeException("Oferente no encontrado"));

        Articulo articulo = articleRepository.findById(createDto.getArticuloId())
                .orElseThrow(() -> new RuntimeException("Articulo no encontrado"));

        if (!articulo.getUsuario().getId().equals(oferente.getId())) {
            throw new RuntimeException("El articulo no pertenece al oferente indicado");
        }

        if (autor.getId().equals(oferente.getId())) {
            throw new RuntimeException("No puedes valorar tus propios articulos");
        }

        if (valoracionRepository.existsByAutor_IdAndArticulo_Id(autor.getId(), articulo.getId())) {
            throw new RuntimeException("Ya valoraste al oferente para este articulo");
        }

        ValoracionOferente valoracion = new ValoracionOferente();
        valoracion.setAutor(autor);
        valoracion.setOferente(oferente);
        valoracion.setArticulo(articulo);
        valoracion.setPuntuacion(createDto.getRating());
        valoracion.setComentario(createDto.getComment());

        ValoracionOferente saved = valoracionRepository.save(valoracion);
        return toResponse(saved);
    }

    private OferenteValoracionResponseDto toResponse(ValoracionOferente valoracion) {
        Usuario autor = valoracion.getAutor();
        String reviewerName = (autor.getNombre() != null && !autor.getNombre().trim().isEmpty())
                ? autor.getNombre()
                : autor.getEmail();

        return OferenteValoracionResponseDto.builder()
                .id(valoracion.getId())
                .reviewerId(autor.getId())
                .reviewerName(reviewerName)
                .articleId(valoracion.getArticulo().getId())
                .rating(valoracion.getPuntuacion())
                .comment(valoracion.getComentario())
                .createdAt(valoracion.getCreadoEn())
                .build();
    }
}
