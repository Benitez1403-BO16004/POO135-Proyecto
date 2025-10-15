package edu.uespoo135.gt3.reservapi.mapper;

import edu.uespoo135.gt3.reservapi.model.Mesa;
import edu.uespoo135.gt3.reservapi.dto.MesaDTO;

public class MesaMapper {

    public static MesaDTO toDTO(Mesa mesa) {
        return MesaDTO.builder()
                .id(mesa.getId())
                .codigo(mesa.getCodigo())
                .capacidad(mesa.getCapacidad())
                .ubicacion(mesa.getUbicacion())
                .build();
    }

    public static Mesa toEntity(MesaDTO dto) {
        return Mesa.builder()
                .id(dto.getId())
                .codigo(dto.getCodigo())
                .capacidad(dto.getCapacidad())
                .ubicacion(dto.getUbicacion())
                .build();
    }
}
