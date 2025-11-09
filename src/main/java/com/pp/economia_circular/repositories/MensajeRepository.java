package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Mensajes enviados por un usuario
    List<Mensaje> findByRemitente_Id(Long remitenteId);

    // Mensajes recibidos por un usuario
    List<Mensaje> findByDestinatario_Id(Long destinatarioId);

    // Conversación entre dos usuarios (sin importar quién envió primero)
    @Query("SELECT m FROM Mensaje m " +
            "WHERE ((m.remitente.id = :usuario1 AND m.destinatario.id = :usuario2) " +
            "OR (m.remitente.id = :usuario2 AND m.destinatario.id = :usuario1)) " +
            "AND m.estado <> 'ELIMINADO' " +
            "ORDER BY m.creadoEn ASC")
    List<Mensaje> findConversationBetweenUsers(@Param("usuario1") Long usuario1, @Param("usuario2") Long usuario2);

    // Mensajes asociados a un artículo
    List<Mensaje> findByArticulo_Id(Long articuloId);

    // Conversaciones activas del usuario (últimos mensajes, sin eliminados)
    @Query("SELECT m FROM Mensaje m " +
            "WHERE (m.remitente.id = :usuarioId OR m.destinatario.id = :usuarioId) " +
            "AND m.estado <> 'ELIMINADO' " +
            "ORDER BY m.creadoEn DESC")
    List<Mensaje> findConversationsByUser(@Param("usuarioId") Long usuarioId);

    // Mensajes no leídos
    @Query("SELECT m FROM Mensaje m WHERE m.destinatario.id = :usuarioId AND m.estado = 'NO_LEIDO'")
    List<Mensaje> findUnreadMessagesByUser(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.destinatario.id = :usuarioId AND m.estado = 'NO_LEIDO'")
    Long countUnreadMessagesByUser(@Param("usuarioId") Long usuarioId);
}
