package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuario_IdOrderByCreadoEnDesc(Long usuarioId);

    long countByUsuario_IdAndLeidaFalse(Long usuarioId);
}
