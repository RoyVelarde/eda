package pe.eda.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.eda.auth.enums.RolUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 50
    )
    private String username;

    @Column(
            name = "password",
            nullable = false,
            length = 100
    )
    private String password;

    @Column(
            name = "nombres",
            nullable = false,
            length = 100
    )
    private String nombres;

    @Column(
            name = "apellidos",
            nullable = false,
            length = 150
    )
    private String apellidos;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "rol",
            nullable = false,
            length = 20
    )
    private RolUsuario rol;

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {

        if (activo == null) {
            activo = true;
        }

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}