package pe.edu.upeu.clinica.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.upeu.clinica.auditoria.entity.EventoAuditoria;
import pe.edu.upeu.clinica.auditoria.repository.EventoAuditoriaRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private EventoAuditoriaRepository repository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraElActorYLaOperacionSinDatosClinicos() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("keycloak-subject", null, List.of()));

        auditoriaService.registrar("PAGO", 42L, "REGISTRAR_ABONO", "Abono registrado");

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(repository).save(captor.capture());
        assertEquals("keycloak-subject", captor.getValue().getActor());
        assertEquals("PAGO", captor.getValue().getRecurso());
        assertEquals("42", captor.getValue().getRecursoId());
        assertEquals("REGISTRAR_ABONO", captor.getValue().getAccion());
        assertEquals("Abono registrado", captor.getValue().getDetalle());
    }
}
