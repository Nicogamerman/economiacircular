package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findAll();

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Long id);

    void delete(Usuario usuario);
}
