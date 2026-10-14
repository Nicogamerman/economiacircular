package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.TelefonoVerificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.TelefonoVerificacionRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.sms.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/** Confirmación del celular del usuario con un código de 6 dígitos enviado por SMS. */
@Service
@Transactional
public class TelefonoVerificacionService {

    static final int MINUTOS_VALIDEZ = 10;
    static final int MAX_INTENTOS = 5;
    static final int SEGUNDOS_ENTRE_ENVIOS = 60;

    /** Formato internacional E.164: + y entre 8 y 15 dígitos. */
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private TelefonoVerificacionRepository verificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SmsSender smsSender;

    @Autowired
    private JWTService authService;

    /** Guarda el número (sin verificar) y envía un código. */
    public void enviarCodigo(String telefonoIngresado) {
        Usuario usuario = requerirUsuario();
        String telefono = normalizar(telefonoIngresado);
        if (!E164.matcher(telefono).matches()) {
            throw new RuntimeException("Ingresá el celular con código de país, por ejemplo +5493511234567");
        }

        verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(usuario.getId()).ifPresent(ultima -> {
            if (!ultima.isUsado() && ultima.getCreadoEn().plusSeconds(SEGUNDOS_ENTRE_ENVIOS).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Esperá un minuto antes de pedir otro código");
            }
        });

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        TelefonoVerificacion v = new TelefonoVerificacion();
        v.setUsuario(usuario);
        v.setTelefono(telefono);
        v.setCodigoHash(hash(codigo));
        v.setExpiraEn(LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ));
        verificacionRepository.save(v);

        usuario.setTelefono(telefono);
        usuario.setTelefonoVerificado(false);
        usuarioRepository.save(usuario);

        smsSender.enviar(telefono, "Economía Circular: tu código de verificación es " + codigo
                + ". Vence en " + MINUTOS_VALIDEZ + " minutos.");
    }

    public void verificarCodigo(String codigo) {
        Usuario usuario = requerirUsuario();
        TelefonoVerificacion v = verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Primero pedí un código de verificación"));

        if (v.isUsado()) throw new RuntimeException("El código ya fue usado. Pedí uno nuevo");
        if (v.getExpiraEn().isBefore(LocalDateTime.now())) throw new RuntimeException("El código venció. Pedí uno nuevo");
        if (v.getIntentos() >= MAX_INTENTOS) throw new RuntimeException("Superaste los intentos permitidos. Pedí un código nuevo");

        if (codigo == null || !hash(codigo.trim()).equals(v.getCodigoHash())) {
            v.setIntentos(v.getIntentos() + 1);
            verificacionRepository.save(v);
            throw new RuntimeException("Código incorrecto");
        }

        v.setUsado(true);
        verificacionRepository.save(v);
        usuario.setTelefono(v.getTelefono());
        usuario.setTelefonoVerificado(true);
        usuarioRepository.save(usuario);
    }

    static String normalizar(String telefono) {
        if (telefono == null) return "";
        return telefono.replaceAll("[\\s\\-()]", "");
    }

    static String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private Usuario requerirUsuario() {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return usuario;
    }
}
