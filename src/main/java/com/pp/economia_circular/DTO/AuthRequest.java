package com.pp.economia_circular.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AuthRequest {
    private String email;
    private String contrasena;
}
