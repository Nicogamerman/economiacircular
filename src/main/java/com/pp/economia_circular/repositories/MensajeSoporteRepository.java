package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.MensajeSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeSoporteRepository extends JpaRepository<MensajeSoporte, Long> {

    List<MensajeSoporte> findByChat_IdOrderByCreadoEnAsc(Long chatId);

    long countByChat_IdAndLeidoFalse(Long chatId);

    @Modifying
    @Query("UPDATE MensajeSoporte m SET m.leido = true WHERE m.chat.id = :chatId AND m.leido = false AND m.emisor.id <> :usuarioId")
    int marcarLeidosEnChat(@Param("chatId") Long chatId, @Param("usuarioId") Long usuarioId);
}
