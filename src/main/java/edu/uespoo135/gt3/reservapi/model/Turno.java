package edu.uespoo135.gt3.reservapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "turnos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_turno_nombre", columnNames = "nombre")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Turno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name="hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name="hora_fin", nullable = false)
    private LocalTime horaFin;
}
