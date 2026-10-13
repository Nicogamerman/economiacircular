package com.pp.economia_circular.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "etiquetas_articulos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_etiqueta_articulo",
                        columnNames = {"articulo_id", "etiqueta"}
                )
        },
        indexes = {
                @Index(name = "idx_etiquetas_articulo", columnList = "articulo_id"),
                @Index(name = "idx_etiquetas_etiqueta", columnList = "etiqueta")
        })
public class EtiquetaArticulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(length = 60, nullable = false)
    private String etiqueta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "articulo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "etiquetas", "imagenes", "solicitudesIntercambio", "vistas"})
    private Articulo articulo;

    public EtiquetaArticulo() {}

    public EtiquetaArticulo(String etiqueta, Articulo articulo) {
        this.etiqueta = etiqueta;
        this.articulo = articulo;
    }
}
