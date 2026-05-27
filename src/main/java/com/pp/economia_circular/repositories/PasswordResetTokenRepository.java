package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usadoEn = :ahora " +
            "WHERE t.usuario.id = :usuarioId AND t.usadoEn IS NULL")
    void invalidarTokensVigentesDeUsuario(@Param("usuarioId") Long usuarioId,
                                          @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiraEn < :limite")
    void eliminarExpirados(@Param("limite") LocalDateTime limite);
}
