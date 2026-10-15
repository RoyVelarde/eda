package pe.eda.capacitacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asignaciones_participante")
@Getter
@Setter
@NoArgsConstructor
public class AsignacionParticipante {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capacitacion_id",
            nullable = false
    )
    private Capacitacion capacitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "aula_capacitacion_id",
            nullable = false
    )
    private AulaCapacitacion aulaCapacitacion;

    @Column(name = "personal_id", nullable = false)
    private UUID personalId;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "funcion", length = 30)
    private String funcion;

    @Column(nullable = false)
    private Boolean activa = true;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
        if (activa == null) {
            activa = true;
        }
    }
}