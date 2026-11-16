package pe.edu.upeu.clinica.citas.paciente.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.clinica.auditoria.AuditoriaService;
import pe.edu.upeu.clinica.citas.paciente.dto.PacienteResponse;
import pe.edu.upeu.clinica.citas.paciente.entity.Paciente;
import pe.edu.upeu.clinica.citas.paciente.mapper.PacienteMapper;
import pe.edu.upeu.clinica.citas.paciente.repository.PacienteRepository;
import pe.edu.upeu.clinica.shared.dto.PageResponse;

@ExtendWith(MockitoExtension.class)
class PacienteServiceImplTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PacienteMapper pacienteMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    @Test
    void listaPacientesConMetadatosYOrdenEstable() {
        Paciente paciente = Paciente.builder()
                .id(1L)
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Alvarez")
                .build();
        PacienteResponse response = PacienteResponse.builder()
                .id(1L)
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Alvarez")
                .build();
        when(pacienteRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(paciente), PageRequest.of(1, 10), 25));
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PageResponse<PacienteResponse> result = pacienteService.listarPaginado(1, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(pacienteRepository).findAll(captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
        assertEquals(3, result.totalPages());
        assertEquals(25, result.totalElements());
        assertEquals("Alvarez", result.content().get(0).getApellidos());
    }
}
