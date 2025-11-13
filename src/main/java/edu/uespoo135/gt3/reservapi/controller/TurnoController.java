package edu.uespoo135.gt3.reservapi.controller;

import edu.uespoo135.gt3.reservapi.dto.TurnoDTO;
import edu.uespoo135.gt3.reservapi.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService service;

    /**
     * Crea un turno.
     */
    @PostMapping
    public ResponseEntity<TurnoDTO> crear(@RequestBody TurnoDTO dto) {
        TurnoDTO creado = service.crear(dto);
        return ResponseEntity
                .created(URI.create("/api/turnos/" + creado.getId()))
                .body(creado);
    }

    /** Lista todos los turnos. */
    @GetMapping
    public List<TurnoDTO> listar() {
        return service.listar();
    }

    /** Obtiene un turno por id. */
    @GetMapping("{id}")
    public TurnoDTO obtener(@PathVariable("id") Long id) {
        return service.obtener(id);
    }

    /** Actualiza un turno existente. */
    @PutMapping("{id}")
    public TurnoDTO actualizar(@PathVariable("id") Long id, @RequestBody TurnoDTO dto) {
        return service.actualizar(id, dto);
    }

    /** Elimina un turno por id. */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
