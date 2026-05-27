package com.pp.economia_circular.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:no-reply@economia-circular.local}")
    private String from;

    public void sendVerificationEmail(String email, Long userId) {
        System.out.println("Email de verificación enviado a: " + email + " para usuario: " + userId);
    }

    public void sendPasswordResetEmail(String email, Long userId) {
        System.out.println("Email de recuperación enviado a: " + email + " para usuario: " + userId);
    }

    public void sendNotificationEmail(String email, String subject, String content) {
        System.out.println("Email de notificación enviado a: " + email + " - Asunto: " + subject);
        enviarSiEstaHabilitado(email, subject, content);
    }

    public void enviarEmailRecuperacionPassword(String email, String nombre, String resetUrl, long ttlMinutos) {
        String saludo = (nombre != null && !nombre.trim().isEmpty()) ? "Hola " + nombre.trim() : "Hola";
        String asunto = "Recuperación de contraseña - Economía Circular";
        String cuerpo = saludo + ",\n\n" +
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta.\n" +
                "Para crear una nueva contraseña, ingresá al siguiente enlace:\n\n" +
                resetUrl + "\n\n" +
                "Este enlace expirará en " + ttlMinutos + " minutos.\n" +
                "Si vos no solicitaste este cambio, podés ignorar este email; tu contraseña actual seguirá vigente.\n\n" +
                "Saludos,\nEquipo de Economía Circular";

        System.out.println("Email de recuperación de contraseña enviado a: " + email);
        enviarSiEstaHabilitado(email, asunto, cuerpo);
    }

    public void enviarEmailConfirmacionCambioPassword(String email, String nombre) {
        String saludo = (nombre != null && !nombre.trim().isEmpty()) ? "Hola " + nombre.trim() : "Hola";
        String asunto = "Tu contraseña fue actualizada - Economía Circular";
        String cuerpo = saludo + ",\n\n" +
                "Te confirmamos que la contraseña de tu cuenta fue actualizada correctamente.\n" +
                "Si no fuiste vos quien realizó este cambio, contactá inmediatamente al soporte.\n\n" +
                "Saludos,\nEquipo de Economía Circular";

        System.out.println("Email de confirmación de cambio de contraseña enviado a: " + email);
        enviarSiEstaHabilitado(email, asunto, cuerpo);
    }

    private void enviarSiEstaHabilitado(String to, String subject, String body) {
        if (!emailEnabled || mailSender == null) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error al enviar email a " + to + ": " + e.getMessage());
        }
    }
}
