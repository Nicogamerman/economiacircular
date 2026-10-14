package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.VerificacionIdentidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificacionIdentidadRepository extends JpaRepository<VerificacionIdentidad, Long> {

    Optional<VerificacionIdentidad> findFirstByUsuario_IdOrderByCreadoEnDesc(Long usuarioId);

    @Query("SELECT v FROM VerificacionIdentidad v JOIN FETCH v.usuario WHERE v.estado = :estado ORDER BY v.creadoEn ASC")
    List<VerificacionIdentidad> findByEstado(@Param("estado") VerificacionIdentidad.EstadoVerificacion estado);

    long countByUsuario_IdAndCreadoEnAfter(Long usuarioId, LocalDateTime desde);
}
