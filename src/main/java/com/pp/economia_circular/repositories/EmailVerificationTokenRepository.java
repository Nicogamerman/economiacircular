package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.usadoEn = :ahora " +
            "WHERE t.usuario.id = :usuarioId AND t.usadoEn IS NULL")
    void invalidarTokensVigentesDeUsuario(@Param("usuarioId") Long usuarioId,
                                          @Param("ahora") LocalDateTime ahora);
}
