package pe.eda.aulas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.eda.aulas.enums.TipoAula;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aulas")
@Getter
@Setter
@NoArgsConstructor
public class Aula {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAula tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Boolean activa;

    @Column(name = "fecha_creacion",nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void alCrear() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (activa == null) {
            activa = true;
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}