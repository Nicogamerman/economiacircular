package com.pp.economia_circular.service.sms;

/**
 * Envío de SMS. La implementación por defecto solo registra el mensaje en el log;
 * para producción se registra otro bean (Twilio, AWS SNS, etc.) que la reemplaza.
 */
public interface SmsSender {

    void enviar(String telefono, String mensaje);
}
