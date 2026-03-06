package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.ValoracionOferente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OferenteValoracionRepository extends JpaRepository<ValoracionOferente, Long> {

    List<ValoracionOferente> findByOferente_IdOrderByCreadoEnDesc(Long oferenteId);

    boolean existsByAutor_IdAndArticulo_Id(Long autorId, Long articuloId);

    long countByOferente_Id(Long oferenteId);

    @Query("SELECT AVG(v.puntuacion) FROM ValoracionOferente v WHERE v.oferente.id = :oferenteId")
    Double findAverageRatingByOferenteId(@Param("oferenteId") Long oferenteId);
}
