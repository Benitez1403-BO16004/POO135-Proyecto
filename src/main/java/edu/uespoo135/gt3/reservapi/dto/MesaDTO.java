package edu.uespoo135.gt3.reservapi.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesaDTO {
    private Long id;
    private String codigo;
    private Integer capacidad;
    private String ubicacion;
}
