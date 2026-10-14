package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.TelefonoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelefonoVerificacionRepository extends JpaRepository<TelefonoVerificacion, Long> {

    Optional<TelefonoVerificacion> findFirstByUsuario_IdOrderByCreadoEnDesc(Long usuarioId);
}
