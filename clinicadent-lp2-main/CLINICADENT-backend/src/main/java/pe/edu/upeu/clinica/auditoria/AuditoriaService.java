package pe.edu.upeu.clinica.auditoria;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.clinica.auditoria.entity.EventoAuditoria;
import pe.edu.upeu.clinica.auditoria.repository.EventoAuditoriaRepository;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final EventoAuditoriaRepository repository;

    @Transactional
    public void registrar(String recurso, Long recursoId, String accion, String detalle) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication != null && authentication.isAuthenticated()
                ? authentication.getName()
                : "sistema";

        repository.save(EventoAuditoria.builder()
                .fechaHora(Instant.now())
                .actor(actor)
                .accion(accion)
                .recurso(recurso)
                .recursoId(String.valueOf(recursoId))
                .detalle(detalle)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<EventoAuditoria> listar(int page, int size) {
        return repository.findAll(PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "fechaHora").and(Sort.by(Sort.Direction.DESC, "id"))));
    }
}
