package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.ValoracionOferente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OferenteValoracionRepository extends JpaRepository<ValoracionOferente, Long> {

    List<ValoracionOferente> findByOferente_IdAndAprobadoTrueOrderByCreadoEnDesc(Long oferenteId);

    List<ValoracionOferente> findAllByOrderByAprobadoAscCreadoEnDesc();

    boolean existsByAutor_IdAndArticulo_Id(Long autorId, Long articuloId);

    long countByOferente_IdAndAprobadoTrue(Long oferenteId);

    long countByAprobadoTrue();

    long countByAprobadoFalse();

    @Query("SELECT AVG(v.puntuacion) FROM ValoracionOferente v WHERE v.oferente.id = :oferenteId AND v.aprobado = true")
    Double findAverageRatingByOferenteId(@Param("oferenteId") Long oferenteId);

    @Query("SELECT AVG(v.puntuacion) FROM ValoracionOferente v WHERE v.aprobado = true")
    Double findAverageApprovedRating();
}
