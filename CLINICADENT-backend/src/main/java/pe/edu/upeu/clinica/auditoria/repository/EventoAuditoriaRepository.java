package pe.edu.upeu.clinica.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.clinica.auditoria.entity.EventoAuditoria;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {
}
