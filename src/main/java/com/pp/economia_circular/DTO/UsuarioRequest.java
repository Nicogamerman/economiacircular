package com.pp.economia_circular.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private String rol;
    private String domicilio;
    private String fotoBase64;
}