package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByValorado_IdOrderByCreadoEnDesc(Long valoradoId);

    List<Valoracion> findByValorador_IdOrderByCreadoEnDesc(Long valoradorId);

    List<Valoracion> findByArticulo_IdOrderByCreadoEnDesc(Long articuloId);

    @Query("SELECT v FROM Valoracion v WHERE v.valorador.id = :valoradorId " +
            "AND v.valorado.id = :valoradoId " +
            "AND ((:articuloId IS NULL AND v.articulo IS NULL) OR v.articulo.id = :articuloId)")
    Optional<Valoracion> findExistente(@Param("valoradorId") Long valoradorId,
                                       @Param("valoradoId") Long valoradoId,
                                       @Param("articuloId") Long articuloId);

    @Query("SELECT AVG(v.puntaje) FROM Valoracion v WHERE v.valorado.id = :usuarioId")
    Double calcularPromedioDeUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.valorado.id = :usuarioId")
    Long contarValoracionesDeUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.valorado.id = :usuarioId AND v.puntaje = :puntaje")
    Long contarPorPuntaje(@Param("usuarioId") Long usuarioId, @Param("puntaje") Integer puntaje);
}
