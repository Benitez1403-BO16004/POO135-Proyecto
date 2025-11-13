package edu.uespoo135.gt3.reservapi.dto;

import lombok.*;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoDTO {

    private Long id;
    private String nombre;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
