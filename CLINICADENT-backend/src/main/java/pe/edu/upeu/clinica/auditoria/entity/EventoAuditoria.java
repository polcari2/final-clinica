package pe.edu.upeu.clinica.auditoria.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AUDITORIA_EVENTOS")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "FECHA_HORA", nullable = false)
    private Instant fechaHora;

    @Column(name = "ACTOR", nullable = false, length = 120)
    private String actor;

    @Column(name = "ACCION", nullable = false, length = 40)
    private String accion;

    @Column(name = "RECURSO", nullable = false, length = 60)
    private String recurso;

    @Column(name = "RECURSO_ID", nullable = false, length = 80)
    private String recursoId;

    @Column(name = "DETALLE", nullable = false, length = 300)
    private String detalle;
}
