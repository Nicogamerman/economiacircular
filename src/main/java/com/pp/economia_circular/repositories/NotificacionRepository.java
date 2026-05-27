package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatario_IdOrderByCreadoEnDesc(Long destinatarioId);

    List<Notificacion> findByDestinatario_IdAndLeidaFalseOrderByCreadoEnDesc(Long destinatarioId);

    long countByDestinatario_IdAndLeidaFalse(Long destinatarioId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.leidoEn = :ahora " +
            "WHERE n.destinatario.id = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId,
                              @Param("ahora") LocalDateTime ahora);
}
