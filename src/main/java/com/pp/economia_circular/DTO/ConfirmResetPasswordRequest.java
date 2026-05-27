package com.pp.economia_circular.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ConfirmResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String newPassword;

    public ConfirmResetPasswordRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
