package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.MensajeAudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MensajeAudioRepository extends JpaRepository<MensajeAudio, Long> {

    Optional<MensajeAudio> findByMensaje_Id(Long mensajeId);
}
