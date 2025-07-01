package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Taller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TallerRepository extends JpaRepository<Taller, Long> {
    Optional<Taller> findById(Long id);

    Taller save(Taller taller);

    void delete(Taller taller);

    List<Taller> findAll();
}
