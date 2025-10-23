package edu.uespoo135.gt3.reservapi.service;

import edu.uespoo135.gt3.reservapi.dto.MesaDTO;
import edu.uespoo135.gt3.reservapi.mapper.MesaMapper;
import edu.uespoo135.gt3.reservapi.model.Mesa;
import edu.uespoo135.gt3.reservapi.repository.MesaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public MesaDTO crearMesa(MesaDTO mesaDTO) {
        Mesa mesa = MesaMapper.toEntity(mesaDTO);
        Mesa guardada = mesaRepository.save(mesa);
        return MesaMapper.toDTO(guardada);
    }

    public List<MesaDTO> listarMesas() {
        return mesaRepository.findAll()
                .stream()
                .map(MesaMapper::toDTO)
                .collect(Collectors.toList());
    }
}
