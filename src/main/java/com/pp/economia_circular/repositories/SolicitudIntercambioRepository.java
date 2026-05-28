package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.SolicitudIntercambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


@Repository
public interface SolicitudIntercambioRepository extends JpaRepository<SolicitudIntercambio, Long> {
    long countByEstado(SolicitudIntercambio.EstadoIntercambio estado);
    long countByCreadoEnAfter(LocalDateTime fecha);


    @Query("SELECT YEAR(s.creadoEn), MONTH(s.creadoEn), COUNT(s) " +
            "FROM SolicitudIntercambio s " +
            "WHERE s.estado = :estado " +
            "GROUP BY YEAR(s.creadoEn), MONTH(s.creadoEn) " +
            "ORDER BY YEAR(s.creadoEn), MONTH(s.creadoEn)")
    List<Object[]> countIntercambiosPorMesPorEstado(
            @Param("estado") SolicitudIntercambio.EstadoIntercambio estado);



}
