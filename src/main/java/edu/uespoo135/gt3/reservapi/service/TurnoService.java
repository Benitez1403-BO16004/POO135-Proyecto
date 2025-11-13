package edu.uespoo135.gt3.reservapi.service;

import edu.uespoo135.gt3.reservapi.dto.TurnoDTO;
import edu.uespoo135.gt3.reservapi.mapper.TurnoMapper;
import edu.uespoo135.gt3.reservapi.model.Turno;
import edu.uespoo135.gt3.reservapi.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {
    private final TurnoRepository repo;

    public TurnoDTO crear(TurnoDTO dto) {
        Turno t = Turno.builder()
                .nombre(dto.getNombre())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .build();
        return TurnoMapper.toDTO(repo.save(t));
    }

    public List<TurnoDTO> listar() { return repo.findAll().stream().map(TurnoMapper::toDTO).toList(); }

    public TurnoDTO obtener(Long id) {
        return repo.findById(id).map(TurnoMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Turno " + id + " no encontrado"));
    }

    public TurnoDTO actualizar(Long id, TurnoDTO dto) {
        Turno t = repo.findById(id).orElseThrow(() -> new RuntimeException("Turno " + id + " no encontrado"));
        t.setNombre(dto.getNombre());
        t.setHoraInicio(dto.getHoraInicio());
        t.setHoraFin(dto.getHoraFin());
        return TurnoMapper.toDTO(repo.save(t));
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Turno " + id + " no encontrado");
        repo.deleteById(id);
    }
}
