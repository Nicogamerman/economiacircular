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
    private String contrasena;  // nullable para usuarios que solo inician con Google
    private String rol;

    /** ID del usuario en Google (sub del ID token). Permite login y vinculación. */
    @Column(name = "google_id", unique = true)
    private String googleId;

    /** Proveedor de autenticación: "local" (email/contraseña) o "google". */
    @Column(name = "auth_provider", length = 50)
    private String authProvider = "local";

    private String domicilio;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;

    private boolean activo = true;
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

}
