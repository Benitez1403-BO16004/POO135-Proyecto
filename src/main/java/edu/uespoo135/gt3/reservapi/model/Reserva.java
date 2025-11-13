package edu.uespoo135.gt3.reservapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {
    public enum Estado { CREATED, CONFIRMED, CANCELLED, NO_SHOW, COMPLETED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="cliente_id", foreignKey = @ForeignKey(name="fk_reserva_cliente"))
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="mesa_id", foreignKey = @ForeignKey(name="fk_reserva_mesa"))
    private Mesa mesa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="turno_id", foreignKey = @ForeignKey(name="fk_reserva_turno"))
    private Turno turno;

    @Column(nullable = false)
    private Integer comensales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Estado estado = Estado.CREATED;
}
