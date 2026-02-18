package com.pp.economia_circular.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Token de un solo uso para recuperación de contraseña.
 * Expira típicamente en 1 hora.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(nullable = false)
    private boolean usado = false;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
