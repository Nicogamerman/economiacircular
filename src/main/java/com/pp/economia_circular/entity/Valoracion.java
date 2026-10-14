package com.pp.economia_circular.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "valoraciones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_valoracion_valorador_valorado_articulo",
                        columnNames = {"valorador_id", "valorado_id", "articulo_id"}
                )
        })
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer puntaje;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String comentario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valorador_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario valorador;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valorado_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario valorado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Articulo articulo;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public Valoracion() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void preActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
