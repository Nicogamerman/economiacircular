package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.SoporteChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SoporteChatRepository extends JpaRepository<SoporteChat, Long> {

    @Query(value = "SELECT c FROM SoporteChat c WHERE c.usuario.id = :usuarioId ORDER BY c.actualizadoEn DESC",
           countQuery = "SELECT COUNT(c) FROM SoporteChat c WHERE c.usuario.id = :usuarioId")
    Page<SoporteChat> findByUsuario_Id(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query(value = "SELECT c FROM SoporteChat c ORDER BY c.actualizadoEn DESC",
           countQuery = "SELECT COUNT(c) FROM SoporteChat c")
    Page<SoporteChat> findAllOrderByActualizadoEnDesc(Pageable pageable);

    @Query(value = "SELECT c FROM SoporteChat c WHERE c.estado = :estado ORDER BY c.actualizadoEn DESC",
           countQuery = "SELECT COUNT(c) FROM SoporteChat c WHERE c.estado = :estado")
    Page<SoporteChat> findByEstado(@Param("estado") SoporteChat.EstadoChat estado, Pageable pageable);
}
