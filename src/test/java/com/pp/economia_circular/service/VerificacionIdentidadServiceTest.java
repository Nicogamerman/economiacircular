package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.VerificacionIdentidadDto;
import com.pp.economia_circular.DTO.ia.AnalisisIdentidadDto;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.VerificacionIdentidad;
import com.pp.economia_circular.entity.VerificacionIdentidad.EstadoVerificacion;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.repositories.VerificacionIdentidadRepository;
import com.pp.economia_circular.service.ia.IaClient;
import com.pp.economia_circular.service.ia.IaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionIdentidadServiceTest {

    @Mock private VerificacionIdentidadRepository verificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IaClient iaClient;
    @Mock private JWTService authService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private VerificacionIdentidadService service;

    private Usuario usuario;
    private final MockMultipartFile documento = new MockMultipartFile("documento", "dni.jpg", "image/jpeg", new byte[]{1});
    private final MockMultipartFile selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", new byte[]{2});

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setRol("USER");
        lenient().when(authService.getCurrentUser()).thenReturn(usuario);
        lenient().when(iaClient.disponible()).thenReturn(true);
        lenient().when(verificacionRepository.save(any(VerificacionIdentidad.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static AnalisisIdentidadDto analisis(String veredicto, boolean legible, boolean nombre,
                                                 boolean selfie, boolean fraude, int confianza) {
        AnalisisIdentidadDto a = new AnalisisIdentidadDto();
        a.veredicto = veredicto;
        a.documentoLegible = legible;
        a.nombreCoincide = nombre;
        a.selfieConDocumento = selfie;
        a.indiciosDeFraude = fraude;
        a.confianza = confianza;
        a.motivo = "motivo";
        return a;
    }

    @Test
    void decidirApruebaSoloSiTodoCoincideConConfianzaSuficiente() {
        assertEquals(EstadoVerificacion.APROBADA,
                VerificacionIdentidadService.decidir(analisis("APROBADO", true, true, true, false, 90)));
        // Confianza baja: aunque la IA diga aprobado, va a revisión
        assertEquals(EstadoVerificacion.REVISION_MANUAL,
                VerificacionIdentidadService.decidir(analisis("APROBADO", true, true, true, false, 40)));
        // La IA dice aprobado pero un chequeo falla: revisión
        assertEquals(EstadoVerificacion.REVISION_MANUAL,
                VerificacionIdentidadService.decidir(analisis("APROBADO", true, false, true, false, 95)));
    }

    @Test
    void decidirRechazaConIndiciosDeFraudeODocumentoIlegible() {
        assertEquals(EstadoVerificacion.RECHAZADA,
                VerificacionIdentidadService.decidir(analisis("RECHAZADO", true, true, true, true, 90)));
        assertEquals(EstadoVerificacion.RECHAZADA,
                VerificacionIdentidadService.decidir(analisis("RECHAZADO", false, false, false, false, 80)));
        // Rechazado sin motivo claro: lo revisa una persona
        assertEquals(EstadoVerificacion.REVISION_MANUAL,
                VerificacionIdentidadService.decidir(analisis("RECHAZADO", true, false, true, false, 80)));
    }

    @Test
    void verificacionAprobadaMarcaAlUsuarioComoVerificado() {
        when(iaClient.analizarIdentidad(any(), eq("image/jpeg"), any(), eq("image/jpeg"), eq("Ana Pérez")))
                .thenReturn(analisis("APROBADO", true, true, true, false, 92));

        VerificacionIdentidadDto dto = service.verificar(documento, selfie);

        assertEquals(EstadoVerificacion.APROBADA, dto.getEstado());
        assertNotNull(dto.getResueltoEn());
        assertTrue(usuario.isIdentidadVerificada());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void verificacionDudosaQuedaEnRevisionSinVerificarAlUsuario() {
        when(iaClient.analizarIdentidad(any(), any(), any(), any(), any()))
                .thenReturn(analisis("REVISION_MANUAL", true, true, false, false, 50));

        VerificacionIdentidadDto dto = service.verificar(documento, selfie);

        assertEquals(EstadoVerificacion.REVISION_MANUAL, dto.getEstado());
        assertNull(dto.getResueltoEn());
        assertFalse(usuario.isIdentidadVerificada());
    }

    @Test
    void usuarioYaVerificadoNoVuelveAVerificar() {
        usuario.setIdentidadVerificada(true);
        assertThrows(RuntimeException.class, () -> service.verificar(documento, selfie));
        verifyNoInteractions(iaClient);
    }

    @Test
    void limiteDeIntentosDiarios() {
        when(verificacionRepository.countByUsuario_IdAndCreadoEnAfter(eq(1L), any()))
                .thenReturn((long) VerificacionIdentidadService.MAX_INTENTOS_POR_DIA);

        assertThrows(RuntimeException.class, () -> service.verificar(documento, selfie));
        verify(iaClient, never()).analizarIdentidad(any(), any(), any(), any(), any());
    }

    @Test
    void sinIaConfiguradaInformaNoDisponible() {
        when(iaClient.disponible()).thenReturn(false);

        IaException ex = assertThrows(IaException.class, () -> service.verificar(documento, selfie));
        assertTrue(ex.isNoDisponible());
    }

    @Test
    void adminApruebaUnaRevisionManualYNotifica() {
        Usuario admin = new Usuario();
        admin.setId(9L);
        admin.setRol("ADMIN");
        VerificacionIdentidad pendiente = new VerificacionIdentidad();
        pendiente.setId(3L);
        pendiente.setUsuario(usuario);
        pendiente.setEstado(EstadoVerificacion.REVISION_MANUAL);
        when(authService.getCurrentUser()).thenReturn(admin);
        when(verificacionRepository.findById(3L)).thenReturn(Optional.of(pendiente));

        VerificacionIdentidadDto dto = service.resolver(3L, true, "Documentación correcta");

        assertEquals(EstadoVerificacion.APROBADA, dto.getEstado());
        assertTrue(usuario.isIdentidadVerificada());
        verify(notificacionService).crear(eq(usuario), eq(admin), eq(Notificacion.TipoNotificacion.VERIFICACION_IDENTIDAD),
                eq("Identidad verificada"), anyString(), eq(Notificacion.ReferenciaTipo.USUARIO), eq(1L));
    }

    @Test
    void unUsuarioComunNoPuedeResolverRevisiones() {
        assertThrows(RuntimeException.class, () -> service.resolver(3L, true, null));
    }
}
