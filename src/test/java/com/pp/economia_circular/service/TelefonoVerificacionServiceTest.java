package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.TelefonoVerificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.TelefonoVerificacionRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.sms.SmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelefonoVerificacionServiceTest {

    @Mock private TelefonoVerificacionRepository verificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SmsSender smsSender;
    @Mock private JWTService authService;

    @InjectMocks
    private TelefonoVerificacionService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        lenient().when(authService.getCurrentUser()).thenReturn(usuario);
    }

    private TelefonoVerificacion verificacionCon(String codigo) {
        TelefonoVerificacion v = new TelefonoVerificacion();
        v.setUsuario(usuario);
        v.setTelefono("+5493511234567");
        v.setCodigoHash(TelefonoVerificacionService.hash(codigo));
        v.setExpiraEn(LocalDateTime.now().plusMinutes(5));
        return v;
    }

    @Test
    void enviarCodigoNormalizaGuardaElHashYMandaElSms() {
        service.enviarCodigo("+54 9 351 123-4567");

        ArgumentCaptor<TelefonoVerificacion> captor = ArgumentCaptor.forClass(TelefonoVerificacion.class);
        verify(verificacionRepository).save(captor.capture());
        ArgumentCaptor<String> sms = ArgumentCaptor.forClass(String.class);
        verify(smsSender).enviar(eq("+5493511234567"), sms.capture());

        Matcher m = Pattern.compile("(\\d{6})").matcher(sms.getValue());
        assertTrue(m.find(), "el SMS incluye un código de 6 dígitos");
        assertEquals(TelefonoVerificacionService.hash(m.group(1)), captor.getValue().getCodigoHash());
        assertNotEquals(m.group(1), captor.getValue().getCodigoHash(), "no se guarda el código en claro");
        assertEquals("+5493511234567", usuario.getTelefono());
        assertFalse(usuario.isTelefonoVerificado());
    }

    @Test
    void numeroSinCodigoDePaisEsRechazado() {
        assertThrows(RuntimeException.class, () -> service.enviarCodigo("3511234567"));
        verifyNoInteractions(smsSender);
    }

    @Test
    void noSePuedePedirOtroCodigoEnseguida() {
        TelefonoVerificacion reciente = verificacionCon("123456");
        reciente.setCreadoEn(LocalDateTime.now().minusSeconds(10));
        when(verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Optional.of(reciente));

        assertThrows(RuntimeException.class, () -> service.enviarCodigo("+5493511234567"));
        verifyNoInteractions(smsSender);
    }

    @Test
    void codigoCorrectoVerificaElCelular() {
        TelefonoVerificacion v = verificacionCon("123456");
        when(verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Optional.of(v));

        service.verificarCodigo(" 123456 ");

        assertTrue(v.isUsado());
        assertTrue(usuario.isTelefonoVerificado());
        assertEquals("+5493511234567", usuario.getTelefono());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void codigoIncorrectoSumaUnIntento() {
        TelefonoVerificacion v = verificacionCon("123456");
        when(verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Optional.of(v));

        assertThrows(RuntimeException.class, () -> service.verificarCodigo("000000"));
        assertEquals(1, v.getIntentos());
        assertFalse(usuario.isTelefonoVerificado());
    }

    @Test
    void codigoVencidoEsRechazado() {
        TelefonoVerificacion v = verificacionCon("123456");
        v.setExpiraEn(LocalDateTime.now().minusMinutes(1));
        when(verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Optional.of(v));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.verificarCodigo("123456"));
        assertTrue(ex.getMessage().contains("venció"));
    }

    @Test
    void demasiadosIntentosBloqueanElCodigo() {
        TelefonoVerificacion v = verificacionCon("123456");
        v.setIntentos(TelefonoVerificacionService.MAX_INTENTOS);
        when(verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Optional.of(v));

        assertThrows(RuntimeException.class, () -> service.verificarCodigo("123456"));
        assertFalse(usuario.isTelefonoVerificado());
    }

    @Test
    void verificarSinHaberPedidoCodigoFalla() {
        when(verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.verificarCodigo("123456"));
    }
}
