package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsadoFalseAndExpiraEnAfter(String token, LocalDateTime now);

    void deleteByUsuarioId(Long usuarioId);

    void deleteByExpiraEnBefore(LocalDateTime date);
}
