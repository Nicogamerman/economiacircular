package com.pp.economia_circular.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private String rol;

    private String domicilio;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;

    private boolean activo = true;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
