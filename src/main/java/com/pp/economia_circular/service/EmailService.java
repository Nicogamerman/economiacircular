package com.pp.economia_circular.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    public void sendVerificationEmail(String email, Long userId) {
        // SERVICIO DESACTIVADO TEMPORALMENTE
        System.out.println("Email de verificación enviado a: " + email + " para usuario: " + userId);
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verificación de Email - Intercambio App");
        message.setText("Hola,\n\n" +
                "Gracias por registrarte en nuestra plataforma de intercambio. " +
                "Para completar tu registro, por favor haz clic en el siguiente enlace:\n\n" +
                "http://localhost:8080/api/auth/verify-email?userId=" + userId + "\n\n" +
                "Si no solicitaste este registro, puedes ignorar este email.\n\n" +
                "Saludos,\n" +
                "Equipo de Intercambio App");
        
        mailSender.send(message);
        */
    }
    
    public void sendPasswordResetEmail(String email, Long userId) {
        // Deprecado: usar sendPasswordResetWithToken
        System.out.println("Email de recuperación enviado a: " + email + " para usuario: " + userId);
    }

    /**
     * Envía email con enlace de recuperación de contraseña (token en URL).
     * Si spring.mail está configurado, envía el correo real; si no, solo loguea (desarrollo).
     */
    public void sendPasswordResetWithToken(String email, String resetLink, int validMinutes) {
        String subject = "Recuperación de contraseña - Economía Circular";
        String text = "Hola,\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña.\n\n"
                + "Haz clic en el siguiente enlace para elegir una nueva contraseña:\n\n"
                + resetLink + "\n\n"
                + "Este enlace expira en " + validMinutes + " minutos.\n\n"
                + "Si no solicitaste este cambio, ignora este correo. Tu contraseña no se modificará.\n\n"
                + "Saludos,\nEquipo Economía Circular";
        sendNotificationEmail(email, subject, text);
    }
    
    public void sendNotificationEmail(String email, String subject, String content) {
        if (mailSender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } else {
            System.out.println("[EmailService] Mail no configurado. Notificación: " + email + " - " + subject);
        }
    }
}
