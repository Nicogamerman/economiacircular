package com.pp.economia_circular.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_password_reset_token_hash", columnList = "token_hash"),
                @Index(name = "idx_password_reset_usuario", columnList = "usuario_id")
        })
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "usado_en")
    private LocalDateTime usadoEn;

    public PasswordResetToken() {
        this.creadoEn = LocalDateTime.now();
    }

    public boolean estaVigente() {
        return usadoEn == null && LocalDateTime.now().isBefore(expiraEn);
    }
}
