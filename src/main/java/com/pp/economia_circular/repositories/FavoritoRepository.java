package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Favorito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    @Query(value = "SELECT f FROM Favorito f JOIN FETCH f.articulo WHERE f.usuario.id = :usuarioId ORDER BY f.creadoEn DESC",
           countQuery = "SELECT COUNT(f) FROM Favorito f WHERE f.usuario.id = :usuarioId")
    Page<Favorito> findByUsuario_Id(@Param("usuarioId") Long usuarioId, Pageable pageable);

    Optional<Favorito> findByUsuario_IdAndArticulo_Id(Long usuarioId, Long articuloId);

    boolean existsByUsuario_IdAndArticulo_Id(Long usuarioId, Long articuloId);

    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.usuario.id = :usuarioId AND f.articulo.id = :articuloId")
    void deleteByUsuario_IdAndArticulo_Id(@Param("usuarioId") Long usuarioId, @Param("articuloId") Long articuloId);

    long countByArticulo_Id(Long articuloId);
}
