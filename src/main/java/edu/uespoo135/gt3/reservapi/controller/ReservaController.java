package edu.uespoo135.gt3.reservapi.controller;

import edu.uespoo135.gt3.reservapi.dto.ReservaDTO;
import edu.uespoo135.gt3.reservapi.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    /**
     * Crea una nueva reserva.
     * @return 201 Created + Location + DTO creado
     */
    @PostMapping
    public ResponseEntity<ReservaDTO> crear(@RequestBody ReservaDTO dto) {
        ReservaDTO creada = service.crear(dto);
        return ResponseEntity
                .created(URI.create("/api/reservas/" + creada.getId()))
                .body(creada);
    }

    /** Lista todas las reservas. */
    @GetMapping
    public List<ReservaDTO> listar() {
        return service.listar();
    }

    /** Obtiene una reserva por id. */
    @GetMapping("{id}")
    public ReservaDTO obtener(@PathVariable("id") Long id) {
        return service.obtener(id);
    }

    /**
     * Actualiza una reserva existente.
     * Nota: el servicio debe revalidar reglas (capacidad, conflicto, estado).
     */
    @PutMapping("{id}")
    public ReservaDTO actualizar(@PathVariable("id") Long id, @RequestBody ReservaDTO dto) {
        return service.actualizar(id, dto);
    }

    /** Elimina una reserva por id. */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**  endpoints de transición de estado explícitos */
    @PostMapping("{id}/confirmar")
    public ReservaDTO confirmar(@PathVariable("id") Long id) {
        return service.confirmar(id);
    }

    @PostMapping("{id}/cancelar")
    public ReservaDTO cancelar(@PathVariable("id") Long id) {
        return service.cancelar(id);
    }
}
