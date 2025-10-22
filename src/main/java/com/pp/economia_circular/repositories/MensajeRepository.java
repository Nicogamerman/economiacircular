package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    
    List<Mensaje> findByRemitente_Id(Long remitenteId);
    
    List<Mensaje> findByDestinatario_Id(Long destinatarioId);
    
    List<Mensaje> findByRemitente_IdAndDestinatario_Id(Long remitenteId, Long destinatarioId);
    
    List<Mensaje> findByArticulo_Id(Long articuloId);
    
    List<Mensaje> findByEstado(Mensaje.EstadoMensaje estado);
    
    @Query("SELECT m FROM Mensaje m WHERE " +
           "(m.remitente.id = :usuarioId OR m.destinatario.id = :usuarioId) AND " +
           "m.estado != 'ELIMINADO' " +
           "ORDER BY m.creadoEn DESC")
    List<Mensaje> findConversationsByUser(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT m FROM Mensaje m WHERE " +
           "((m.remitente.id = :usuarioId1 AND m.destinatario.id = :usuarioId2) OR " +
           "(m.remitente.id = :usuarioId2 AND m.destinatario.id = :usuarioId1)) AND " +
           "m.estado != 'ELIMINADO' " +
           "ORDER BY m.creadoEn ASC")
    List<Mensaje> findConversationBetweenUsers(@Param("usuarioId1") Long usuarioId1, @Param("usuarioId2") Long usuarioId2);
    
    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.destinatario.id = :usuarioId AND m.estado = 'NO_LEIDO'")
    Long countUnreadMessagesByUser(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT m FROM Mensaje m WHERE m.destinatario.id = :usuarioId AND m.estado = 'NO_LEIDO'")
    List<Mensaje> findUnreadMessagesByUser(@Param("usuarioId") Long usuarioId);
}
