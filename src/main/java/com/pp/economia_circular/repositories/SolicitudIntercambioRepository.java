package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.SolicitudIntercambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;


@Repository
public interface SolicitudIntercambioRepository extends JpaRepository<SolicitudIntercambio, Long> {
    long countByEstado(SolicitudIntercambio.EstadoIntercambio estado);
    long countByCreadoEnAfter(LocalDateTime fecha);


}
