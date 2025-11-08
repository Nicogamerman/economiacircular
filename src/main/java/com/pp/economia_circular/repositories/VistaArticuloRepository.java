package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.VistaArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VistaArticuloRepository extends JpaRepository<VistaArticulo, Long> {

    @Query("SELECT v.articulo.id, COUNT(v) AS totalVistas " +
            "FROM VistaArticulo v " +
            "GROUP BY v.articulo.id " +
            "ORDER BY totalVistas DESC")
    List<Object[]> findTopViewedArticles();
}
