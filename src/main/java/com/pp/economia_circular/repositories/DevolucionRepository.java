package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    /** Devolución más reciente de un intercambio (la que se muestra en el listado). */
    Optional<Devolucion> findFirstBySolicitud_IdOrderByCreadoEnDesc(Long solicitudId);

    List<Devolucion> findBySolicitud_IdIn(Collection<Long> solicitudIds);

    boolean existsBySolicitud_IdAndEstado(Long solicitudId, Devolucion.EstadoDevolucion estado);

    @Query("SELECT d FROM Devolucion d " +
            "WHERE d.solicitud.solicitante.id = :usuarioId OR d.solicitud.articuloSolicitado.usuario.id = :usuarioId " +
            "ORDER BY d.creadoEn DESC")
    List<Devolucion> findByParticipante(@Param("usuarioId") Long usuarioId);

    List<Devolucion> findAllByOrderByCreadoEnDesc();
}
