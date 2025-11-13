package edu.uespoo135.gt3.reservapi.mapper;

import edu.uespoo135.gt3.reservapi.dto.ReservaDTO;
import edu.uespoo135.gt3.reservapi.model.Reserva;

public class ReservaMapper {
    public static ReservaDTO toDTO(Reserva r) {
        if (r == null) return null;
        return ReservaDTO.builder()
                .id(r.getId())
                .fecha(r.getFecha())
                .clienteId(r.getCliente().getId())
                .mesaId(r.getMesa().getId())
                .turnoId(r.getTurno().getId())
                .comensales(r.getComensales())
                .estado(r.getEstado() != null ? r.getEstado().name() : null)
                .build();
    }
}
