package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByGoogleId(String googleId);
    long countByActivoTrue();
    long countByCreadoEnAfter(LocalDateTime fecha);

}
