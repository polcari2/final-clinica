package pe.edu.upeu.clinica.citas.paciente.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.clinica.auditoria.AuditoriaService;
import pe.edu.upeu.clinica.citas.paciente.dto.PacienteRequest;
import pe.edu.upeu.clinica.citas.paciente.dto.PacienteResponse;
import pe.edu.upeu.clinica.citas.paciente.entity.Paciente;
import pe.edu.upeu.clinica.citas.paciente.mapper.PacienteMapper;
import pe.edu.upeu.clinica.citas.paciente.repository.PacienteRepository;
import pe.edu.upeu.clinica.citas.paciente.service.PacienteService;
import pe.edu.upeu.clinica.shared.dto.PageResponse;
import pe.edu.upeu.clinica.shared.exception.ReglaNegocioException;
import pe.edu.upeu.clinica.shared.exception.ResourceNotFoundException;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Transactional
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;
    private final AuditoriaService auditoriaService;

    @Override
    public PacienteResponse crear(PacienteRequest request) {
        if (pacienteRepository.existsByDni(request.getDni())) {
            throw new ReglaNegocioException("Ya existe un paciente con DNI " + request.getDni());
        }
        Paciente paciente = pacienteMapper.toEntity(request);
        Paciente guardado = pacienteRepository.save(paciente);
        auditoriaService.registrar("PACIENTE", guardado.getId(), "CREAR", "Alta de paciente");
        return pacienteMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {
        return pacienteMapper.toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorDni(String dni) {
        Paciente paciente = pacienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con DNI " + dni));
        return pacienteMapper.toResponse(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        return pacienteRepository.findAll().stream()
                .map(pacienteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PacienteResponse> listarPaginado(int page, int size) {
        return PageResponse.from(pacienteRepository.findAll(PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.ASC, "apellidos").and(Sort.by(Sort.Direction.ASC, "nombres"))))
                .map(pacienteMapper::toResponse));
    }

    @Override
    public PacienteResponse actualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscar(id);
        pacienteRepository.findByDni(request.getDni())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ReglaNegocioException("Ya existe un paciente con DNI " + request.getDni());
                });
        pacienteMapper.updateEntity(paciente, request);
                Paciente actualizado = pacienteRepository.save(paciente);
                auditoriaService.registrar("PACIENTE", actualizado.getId(), "ACTUALIZAR", "Datos de paciente actualizados");
                return pacienteMapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Paciente paciente = buscar(id);
        auditoriaService.registrar("PACIENTE", id, "ELIMINAR", "Paciente eliminado");
        pacienteRepository.delete(paciente);
    }

    private Paciente buscar(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }
}
