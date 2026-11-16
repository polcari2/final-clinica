package pe.edu.upeu.clinica.citas.cita.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.clinica.auditoria.AuditoriaService;
import pe.edu.upeu.clinica.citas.cita.dto.CitaRequest;
import pe.edu.upeu.clinica.citas.cita.entity.Cita;
import pe.edu.upeu.clinica.citas.cita.mapper.CitaMapper;
import pe.edu.upeu.clinica.citas.cita.repository.CitaRepository;
import pe.edu.upeu.clinica.citas.paciente.repository.PacienteRepository;
import pe.edu.upeu.clinica.shared.exception.ReglaNegocioException;
import pe.edu.upeu.clinica.tratamientos.catalogo.repository.ProcedimientoRepository;

@ExtendWith(MockitoExtension.class)
class CitaServiceImplTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProcedimientoRepository procedimientoRepository;

    @Mock
    private CitaMapper citaMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    void noPermiteProgramarOtraCitaEnElMismoHorario() {
        LocalDateTime fechaHora = LocalDateTime.of(2026, 11, 10, 10, 0);
        CitaRequest request = CitaRequest.builder()
                .idPaciente(5L)
                .fechaHora(fechaHora)
                .build();
        when(citaRepository.existsByFechaHoraAndEstadoNot(fechaHora, Cita.ESTADO_CANCELADA))
                .thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> citaService.programar(request));

        verify(pacienteRepository, never()).existsById(5L);
        verify(citaRepository, never()).save(org.mockito.ArgumentMatchers.any(Cita.class));
    }

    @Test
    void alEditarExcluyeLaCitaActualPeroImpideOcuparOtroHorarioExistente() {
        LocalDateTime fechaHora = LocalDateTime.of(2026, 11, 10, 10, 0);
        Cita cita = Cita.builder()
                .id(8L)
                .idPaciente(5L)
                .fechaHora(LocalDateTime.of(2026, 11, 10, 9, 0))
                .estado(Cita.ESTADO_PROGRAMADA)
                .build();
        CitaRequest request = CitaRequest.builder()
                .idPaciente(5L)
                .fechaHora(fechaHora)
                .build();
        when(citaRepository.findById(8L)).thenReturn(Optional.of(cita));
        when(pacienteRepository.existsById(5L)).thenReturn(true);
        when(citaRepository.existsByFechaHoraAndEstadoNotAndIdNot(
                fechaHora, Cita.ESTADO_CANCELADA, 8L))
                .thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> citaService.actualizar(8L, request));

        verify(citaRepository, never()).save(cita);
    }
}
