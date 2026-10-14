package pe.eda.personal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.eda.personal.enums.NivelPersonal;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "personal",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_personal_dni", columnNames = "dni")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Personal {

    @Id
    private UUID id;

    @Column(nullable = false, length = 8)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelPersonal nivel;

    private Integer ranking;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(nullable = false, length = 150)
    private String nombres;

    @Column(length = 15)
    private String celular;

    @Column(length = 150)
    private String correo;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(length = 50)
    private String situacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void antesDeGuardar() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

}
