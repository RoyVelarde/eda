package pe.eda.capacitacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "aulas_capacitacion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_capacitacion_aula",
                        columnNames = {
                                "capacitacion_id",
                                "aula_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AulaCapacitacion {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capacitacion_id",
            nullable = false
    )
    private Capacitacion capacitacion;

    @Column(name = "aula_id", nullable = false)
    private UUID aulaId;

    @Column(nullable = false)
    private Boolean activa;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (activa == null) {
            activa = true;
        }
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}
