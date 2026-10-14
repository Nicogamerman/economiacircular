package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    Optional<Seguimiento> findByUsuario_IdAndTipoAndObjetivoId(Long usuarioId, Seguimiento.TipoSeguimiento tipo, Long objetivoId);

    boolean existsByUsuario_IdAndTipoAndObjetivoId(Long usuarioId, Seguimiento.TipoSeguimiento tipo, Long objetivoId);

    List<Seguimiento> findByUsuario_IdOrderByCreadoEnDesc(Long usuarioId);

    long countByTipoAndObjetivoId(Seguimiento.TipoSeguimiento tipo, Long objetivoId);

    @Query("SELECT s.objetivoId FROM Seguimiento s WHERE s.usuario.id = :usuarioId AND s.tipo = :tipo")
    List<Long> findObjetivoIds(@Param("usuarioId") Long usuarioId, @Param("tipo") Seguimiento.TipoSeguimiento tipo);

    @Query("SELECT s FROM Seguimiento s JOIN FETCH s.usuario WHERE s.tipo = :tipo AND s.objetivoId = :objetivoId")
    List<Seguimiento> findSeguidores(@Param("tipo") Seguimiento.TipoSeguimiento tipo, @Param("objetivoId") Long objetivoId);
}
