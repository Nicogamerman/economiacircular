package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Favorito;

import java.time.LocalDateTime;

public class FavoritoResponseDto {

    private Long favoritoId;
    private LocalDateTime guardadoEn;
    private ArticleResponseDto articulo;

    public FavoritoResponseDto() {}

    public FavoritoResponseDto(Long favoritoId, LocalDateTime guardadoEn, ArticleResponseDto articulo) {
        this.favoritoId = favoritoId;
        this.guardadoEn = guardadoEn;
        this.articulo = articulo;
    }

    public Long getFavoritoId() { return favoritoId; }
    public void setFavoritoId(Long favoritoId) { this.favoritoId = favoritoId; }

    public LocalDateTime getGuardadoEn() { return guardadoEn; }
    public void setGuardadoEn(LocalDateTime guardadoEn) { this.guardadoEn = guardadoEn; }

    public ArticleResponseDto getArticulo() { return articulo; }
    public void setArticulo(ArticleResponseDto articulo) { this.articulo = articulo; }
}
