package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.entity.InteresEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteresEventoRepository extends JpaRepository<InteresEvento, Long> {

    List<InteresEvento> findByUsuario_Id(Long usuarioId);

    @Query("SELECT i FROM InteresEvento i JOIN FETCH i.usuario WHERE i.tipoEvento = :tipo")
    List<InteresEvento> findInteresados(@Param("tipo") Event.EventType tipo);

    @Modifying
    @Query("DELETE FROM InteresEvento i WHERE i.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}
