package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.SolicitudIntercambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudIntercambioRepository extends JpaRepository<SolicitudIntercambio, Long> {
    long countByEstado(SolicitudIntercambio.EstadoIntercambio estado);
}
