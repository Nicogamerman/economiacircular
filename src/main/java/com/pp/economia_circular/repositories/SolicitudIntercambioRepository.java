package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.SolicitudIntercambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


@Repository
public interface SolicitudIntercambioRepository extends JpaRepository<SolicitudIntercambio, Long> {
    long countByEstado(SolicitudIntercambio.EstadoIntercambio estado);
    long countByCreadoEnAfter(LocalDateTime fecha);


    @Query("SELECT FUNCTION('DATE_FORMAT', s.creadoEn, '%Y-%m'), COUNT(s) " +
            "FROM SolicitudIntercambio s " +
            "WHERE s.estado = 'COMPLETADO' " +
            "GROUP BY FUNCTION('DATE_FORMAT', s.creadoEn, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', s.creadoEn, '%Y-%m')")
    List<Object[]> countIntercambiosCompletadosPorMes();



}
