package com.pp.economia_circular.DTO;

import lombok.Data;

@Data
public class UsuarioRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private String rol;
    private String domicilio;
    private String fotoBase64;
}