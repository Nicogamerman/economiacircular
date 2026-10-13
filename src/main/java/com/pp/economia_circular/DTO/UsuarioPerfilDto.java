package com.pp.economia_circular.DTO;

import java.time.LocalDateTime;

public class UsuarioPerfilDto {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String domicilio;
    private String rol;
    private boolean activo;
    private boolean emailVerificado;
    private boolean tieneFoto;
    private Double valoracionPromedio;
    private Long cantidadValoraciones;
    private Long articulosDisponibles;
    private LocalDateTime creadoEn;

    public UsuarioPerfilDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isEmailVerificado() { return emailVerificado; }
    public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }

    public boolean isTieneFoto() { return tieneFoto; }
    public void setTieneFoto(boolean tieneFoto) { this.tieneFoto = tieneFoto; }

    public Double getValoracionPromedio() { return valoracionPromedio; }
    public void setValoracionPromedio(Double valoracionPromedio) { this.valoracionPromedio = valoracionPromedio; }

    public Long getCantidadValoraciones() { return cantidadValoraciones; }
    public void setCantidadValoraciones(Long cantidadValoraciones) { this.cantidadValoraciones = cantidadValoraciones; }

    public Long getArticulosDisponibles() { return articulosDisponibles; }
    public void setArticulosDisponibles(Long articulosDisponibles) { this.articulosDisponibles = articulosDisponibles; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
