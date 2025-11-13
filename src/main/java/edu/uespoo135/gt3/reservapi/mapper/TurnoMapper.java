package edu.uespoo135.gt3.reservapi.mapper;

import edu.uespoo135.gt3.reservapi.dto.TurnoDTO;
import edu.uespoo135.gt3.reservapi.model.Turno;

public class TurnoMapper {
    public static TurnoDTO toDTO(Turno t) {
        if (t == null) return null;
        return TurnoDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .horaInicio(t.getHoraInicio())
                .horaFin(t.getHoraFin())
                .build();
    }
}
