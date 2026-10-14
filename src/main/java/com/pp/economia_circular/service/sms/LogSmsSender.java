package com.pp.economia_circular.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SMS de desarrollo: escribe el mensaje en el log en vez de enviarlo.
 * Para usar un proveedor real, registrar otra implementación de SmsSender marcada con @Primary.
 */
@Component
public class LogSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LogSmsSender.class);

    @Override
    public void enviar(String telefono, String mensaje) {
        log.info("[SMS simulado] a {}: {}", telefono, mensaje);
    }
}
