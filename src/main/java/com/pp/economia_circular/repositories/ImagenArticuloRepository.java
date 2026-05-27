package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.ImagenArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenArticuloRepository extends JpaRepository<ImagenArticulo, Long> {

    List<ImagenArticulo> findByArticulo_IdOrderByCreadoEnAsc(Long articuloId);

    long countByArticulo_Id(Long articuloId);
}
