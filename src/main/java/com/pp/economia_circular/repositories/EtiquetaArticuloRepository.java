package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.EtiquetaArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtiquetaArticuloRepository extends JpaRepository<EtiquetaArticulo, Long> {

    List<EtiquetaArticulo> findByArticulo_Id(Long articuloId);

    @Query("SELECT e.etiqueta, COUNT(e) as cantidad FROM EtiquetaArticulo e " +
            "WHERE e.articulo.estado = 'DISPONIBLE' " +
            "GROUP BY e.etiqueta " +
            "ORDER BY cantidad DESC")
    List<Object[]> findTopEtiquetas();

    @Query("SELECT DISTINCT a.id FROM EtiquetaArticulo e JOIN e.articulo a " +
            "WHERE LOWER(e.etiqueta) = LOWER(:etiqueta) AND a.estado = 'DISPONIBLE'")
    List<Long> findArticulosIdByEtiqueta(@Param("etiqueta") String etiqueta);
}
