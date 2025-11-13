package edu.uespoo135.gt3.reservapi.service;

import edu.uespoo135.gt3.reservapi.dto.ReservaDTO;
import edu.uespoo135.gt3.reservapi.mapper.ReservaMapper;
import edu.uespoo135.gt3.reservapi.model.*;
import edu.uespoo135.gt3.reservapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepo;
    private final ClienteRepository clienteRepo;
    private final MesaRepository mesaRepo;
    private final TurnoRepository turnoRepo;

    public ReservaDTO crear(ReservaDTO dto) {

        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente " + dto.getClienteId() + " no encontrado"));
        Mesa mesa = mesaRepo.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa " + dto.getMesaId() + " no encontrada"));
        Turno turno = turnoRepo.findById(dto.getTurnoId())
                .orElseThrow(() -> new RuntimeException("Turno " + dto.getTurnoId() + " no encontrado"));

        Reserva.Estado estado = dto.getEstado() == null
                ? Reserva.Estado.CREATED
                : Reserva.Estado.valueOf(dto.getEstado());

        Reserva r = Reserva.builder()
                .fecha(dto.getFecha())
                .cliente(cliente)
                .mesa(mesa)
                .turno(turno)
                .comensales(dto.getComensales())
                .estado(estado)
                .build();
        return ReservaMapper.toDTO(reservaRepo.save(r));
    }

    public List<ReservaDTO> listar() { return reservaRepo.findAll().stream().map(ReservaMapper::toDTO).toList(); }

    public ReservaDTO obtener(Long id) {
        return reservaRepo.findById(id).map(ReservaMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Reserva " + id + " no encontrada"));
    }

    public ReservaDTO actualizar(Long id, ReservaDTO dto) {

        Reserva r = reservaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva " + id + " no encontrada"));

        // Actualización parcial (solo si vienen valores)
        if (dto.getFecha() != null) r.setFecha(dto.getFecha());
        if (dto.getClienteId() != null) {
            r.setCliente(clienteRepo.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente " + dto.getClienteId() + " no encontrado")));
        }
        if (dto.getMesaId() != null) {
            r.setMesa(mesaRepo.findById(dto.getMesaId())
                    .orElseThrow(() -> new RuntimeException("Mesa " + dto.getMesaId() + " no encontrada")));
        }
        if (dto.getTurnoId() != null) {
            r.setTurno(turnoRepo.findById(dto.getTurnoId())
                    .orElseThrow(() -> new RuntimeException("Turno " + dto.getTurnoId() + " no encontrado")));
        }
        if (dto.getComensales() != null) r.setComensales(dto.getComensales());

        // Igual que en crear(): valueOf requiere nombre exacto del enum.
        if (dto.getEstado() != null) r.setEstado(Reserva.Estado.valueOf(dto.getEstado()));

        // Persistir cambios y devolver DTO
        return ReservaMapper.toDTO(reservaRepo.save(r));
    }


    public ReservaDTO confirmar(Long id) {
        Reserva r = reservaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva " + id + " no encontrada"));

        switch (r.getEstado()) {
            case CONFIRMED -> {
                // Idempotente: ya confirmada, devolver tal cual
                return ReservaMapper.toDTO(r);
            }
            case CREATED -> {
                r.setEstado(Reserva.Estado.CONFIRMED);
                return ReservaMapper.toDTO(reservaRepo.save(r));
            }
            case CANCELLED, COMPLETED, NO_SHOW -> {
                throw new IllegalStateException("No se puede confirmar una reserva en estado " + r.getEstado());
            }
            default -> throw new IllegalStateException("Estado de reserva no soportado: " + r.getEstado());
        }
    }

    public ReservaDTO cancelar(Long id) {
        Reserva r = reservaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva " + id + " no encontrada"));

        switch (r.getEstado()) {
            case CANCELLED -> {
                // Idempotente: ya cancelada
                return ReservaMapper.toDTO(r);
            }
            case CREATED, CONFIRMED -> {
                r.setEstado(Reserva.Estado.CANCELLED);
                return ReservaMapper.toDTO(reservaRepo.save(r));
            }
            case COMPLETED, NO_SHOW -> {
                throw new IllegalStateException("No se puede cancelar una reserva en estado " + r.getEstado());
            }
            default -> throw new IllegalStateException("Estado de reserva no soportado: " + r.getEstado());
        }
    }


    public void eliminar(Long id) {
        if (!reservaRepo.existsById(id)) throw new RuntimeException("Reserva " + id + " no encontrada");
        reservaRepo.deleteById(id);
    }
}
