package pe.eda.capacitacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.eda.capacitacion.enums.FuncionTutor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "asignaciones_tutor",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tutor_aula_capacitacion",
                        columnNames = {"aula_capacitacion_id", "personal_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AsignacionTutor {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capacitacion_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_asignacion_tutor_capacitacion")
    )
    private Capacitacion capacitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "aula_capacitacion_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_asignacion_tutor_aula")
    )
    private AulaCapacitacion aulaCapacitacion;

    @Column(name = "personal_id", nullable = false)
    private UUID personalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcion", nullable = false, length = 20)
    private FuncionTutor funcion;

    @Column(name = "activa", nullable = false)
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