package pe.edu.upeu.clinica.auditoria.dto;

import java.time.Instant;
import pe.edu.upeu.clinica.auditoria.entity.EventoAuditoria;

public record EventoAuditoriaResponse(
        Long id,
        Instant fechaHora,
        String actor,
        String accion,
        String recurso,
        String recursoId,
        String detalle) {

    public static EventoAuditoriaResponse from(EventoAuditoria evento) {
        return new EventoAuditoriaResponse(
                evento.getId(),
                evento.getFechaHora(),
                evento.getActor(),
                evento.getAccion(),
                evento.getRecurso(),
                evento.getRecursoId(),
                evento.getDetalle());
    }
}
