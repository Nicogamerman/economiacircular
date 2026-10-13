package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Politica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoliticaRepository extends JpaRepository<Politica, Long> {

    List<Politica> findByActivoTrueOrderByOrdenAscCreadoEnAsc();

    List<Politica> findByTipoAndActivoTrueOrderByOrdenAsc(Politica.TipoPolitica tipo);

    List<Politica> findAllByOrderByOrdenAscCreadoEnAsc();
}
