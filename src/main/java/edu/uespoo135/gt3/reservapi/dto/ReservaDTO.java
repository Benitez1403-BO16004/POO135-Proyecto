package edu.uespoo135.gt3.reservapi.dto;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {

    private Long id;
    private LocalDate fecha;
    private Long clienteId;
    private Long mesaId;
    private Long turnoId;
    private Integer comensales;
    private String estado;
}
