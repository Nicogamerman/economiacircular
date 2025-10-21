package com.pp.economia_circular.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    // SERVICIO DE EMAIL DESACTIVADO TEMPORALMENTE - REQUIERE CONFIGURACIÓN DE MAIL
    // @Autowired
    // private JavaMailSender mailSender;
    
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
        // SERVICIO DESACTIVADO TEMPORALMENTE
        System.out.println("Email de recuperación enviado a: " + email + " para usuario: " + userId);
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Recuperación de Contraseña - Intercambio App");
        message.setText("Hola,\n\n" +
                "Recibimos una solicitud para restablecer tu contraseña. " +
                "Para crear una nueva contraseña, haz clic en el siguiente enlace:\n\n" +
                "http://localhost:8080/api/auth/reset-password?userId=" + userId + "\n\n" +
                "Este enlace expirará en 24 horas.\n\n" +
                "Si no solicitaste este cambio, puedes ignorar este email.\n\n" +
                "Saludos,\n" +
                "Equipo de Intercambio App");
        
        mailSender.send(message);
        */
    }
    
    public void sendNotificationEmail(String email, String subject, String content) {
        // SERVICIO DESACTIVADO TEMPORALMENTE
        System.out.println("Email de notificación enviado a: " + email + " - Asunto: " + subject);
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);
        
        mailSender.send(message);
        */
    }
}
