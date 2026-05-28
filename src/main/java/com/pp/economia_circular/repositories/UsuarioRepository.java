package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByGoogleId(String googleId);
    long countByActivoTrue();
    long countByCreadoEnAfter(LocalDateTime fecha);

    @Query("SELECT YEAR(u.creadoEn), MONTH(u.creadoEn), COUNT(u) " +
            "FROM Usuario u GROUP BY YEAR(u.creadoEn), MONTH(u.creadoEn) " +
            "ORDER BY YEAR(u.creadoEn), MONTH(u.creadoEn)")
    List<Object[]> countUsuariosAgrupadosPorMes();

}

