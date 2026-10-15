package pe.eda.capacitacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "codigos_reserva")
@Getter
@Setter
@NoArgsConstructor
public class CodigoReserva {

    @Id
    private UUID id;

    @Column(
            name = "capacitacion_id",
            nullable = false,
            unique = true
    )
    private UUID capacitacionId;

    @Column(
            name = "codigo_hash",
            nullable = false,
            length = 255
    )
    private String codigoHash;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
            name = "fecha_actualizacion",
            nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = ahora;
        }
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
