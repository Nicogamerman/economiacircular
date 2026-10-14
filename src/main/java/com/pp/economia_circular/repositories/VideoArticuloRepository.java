package com.pp.economia_circular.repositories;

import com.pp.economia_circular.DTO.VideoArticuloDto;
import com.pp.economia_circular.entity.VideoArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoArticuloRepository extends JpaRepository<VideoArticulo, Long> {

    Optional<VideoArticulo> findByArticulo_Id(Long articuloId);

    /** Metadatos sin traer el LONGBLOB. */
    @Query("SELECT new com.pp.economia_circular.DTO.VideoArticuloDto(" +
            "v.id, v.articulo.id, v.nombreArchivo, v.contentType, v.tamanoBytes, v.conAudio, v.creadoEn) " +
            "FROM VideoArticulo v WHERE v.articulo.id = :articuloId")
    Optional<VideoArticuloDto> findMetadatos(@Param("articuloId") Long articuloId);

    boolean existsByArticulo_Id(Long articuloId);
}
