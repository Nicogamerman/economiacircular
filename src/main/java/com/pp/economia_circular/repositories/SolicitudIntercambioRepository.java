package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.SolicitudIntercambio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudIntercambioRepository extends JpaRepository<SolicitudIntercambio, Long> {

    long countByEstado(SolicitudIntercambio.EstadoIntercambio estado);

    @Query(value = "SELECT s FROM SolicitudIntercambio s JOIN FETCH s.articuloSolicitado JOIN FETCH s.articuloOfrecido WHERE s.solicitante.id = :usuarioId ORDER BY s.creadoEn DESC",
           countQuery = "SELECT COUNT(s) FROM SolicitudIntercambio s WHERE s.solicitante.id = :usuarioId")
    Page<SolicitudIntercambio> findBySolicitante_Id(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query(value = "SELECT s FROM SolicitudIntercambio s JOIN FETCH s.articuloSolicitado JOIN FETCH s.articuloOfrecido WHERE s.articuloSolicitado.usuario.id = :usuarioId ORDER BY s.creadoEn DESC",
           countQuery = "SELECT COUNT(s) FROM SolicitudIntercambio s WHERE s.articuloSolicitado.usuario.id = :usuarioId")
    Page<SolicitudIntercambio> findByPropietarioArticuloSolicitado(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query(value = "SELECT s FROM SolicitudIntercambio s JOIN FETCH s.articuloSolicitado JOIN FETCH s.articuloOfrecido WHERE s.solicitante.id = :usuarioId OR s.articuloSolicitado.usuario.id = :usuarioId ORDER BY s.creadoEn DESC",
           countQuery = "SELECT COUNT(s) FROM SolicitudIntercambio s WHERE s.solicitante.id = :usuarioId OR s.articuloSolicitado.usuario.id = :usuarioId")
    Page<SolicitudIntercambio> findHistorialByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM SolicitudIntercambio s WHERE s.solicitante.id = :solicitanteId AND s.articuloSolicitado.id = :articuloSolicitadoId AND s.articuloOfrecido.id = :articuloOfrecidoId AND s.estado = 'PENDIENTE'")
    boolean existeSolicitudPendiente(@Param("solicitanteId") Long solicitanteId,
                                     @Param("articuloSolicitadoId") Long articuloSolicitadoId,
                                     @Param("articuloOfrecidoId") Long articuloOfrecidoId);
}
