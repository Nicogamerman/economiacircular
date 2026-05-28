package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.PoliticaCreateDto;
import com.pp.economia_circular.DTO.PoliticaResponseDto;
import com.pp.economia_circular.entity.Politica;
import com.pp.economia_circular.repositories.PoliticaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PoliticaService {

    @Autowired
    private PoliticaRepository politicaRepository;

    @Transactional(readOnly = true)
    public List<PoliticaResponseDto> listarActivas(Politica.TipoPolitica tipo) {
        List<Politica> politicas = tipo != null
                ? politicaRepository.findByTipoAndActivoTrueOrderByOrdenAsc(tipo)
                : politicaRepository.findByActivoTrueOrderByOrdenAscCreadoEnAsc();
        return politicas.stream().map(PoliticaResponseDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PoliticaResponseDto> listarTodas() {
        return politicaRepository.findAllByOrderByOrdenAscCreadoEnAsc().stream()
                .map(PoliticaResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PoliticaResponseDto obtener(Long id) {
        return PoliticaResponseDto.from(buscar(id));
    }

    public PoliticaResponseDto crear(PoliticaCreateDto dto) {
        Politica p = new Politica();
        aplicarDto(p, dto);
        return PoliticaResponseDto.from(politicaRepository.save(p));
    }

    public PoliticaResponseDto actualizar(Long id, PoliticaCreateDto dto) {
        Politica p = buscar(id);
        aplicarDto(p, dto);
        return PoliticaResponseDto.from(politicaRepository.save(p));
    }

    public void eliminar(Long id) {
        Politica p = buscar(id);
        politicaRepository.delete(p);
    }

    public PoliticaResponseDto toggleActivo(Long id) {
        Politica p = buscar(id);
        p.setActivo(!p.isActivo());
        return PoliticaResponseDto.from(politicaRepository.save(p));
    }

    private void aplicarDto(Politica p, PoliticaCreateDto dto) {
        p.setTipo(dto.getTipo());
        p.setTitulo(dto.getTitulo().trim());
        p.setContenido(dto.getContenido().trim());
        p.setActivo(dto.isActivo());
        p.setOrden(dto.getOrden());
    }

    private Politica buscar(Long id) {
        return politicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));
    }
}
