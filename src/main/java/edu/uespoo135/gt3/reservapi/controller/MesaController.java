package edu.uespoo135.gt3.reservapi.controller;

import edu.uespoo135.gt3.reservapi.dto.MesaDTO;
import edu.uespoo135.gt3.reservapi.service.MesaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @PostMapping
    public ResponseEntity<MesaDTO> crearMesa(@RequestBody MesaDTO mesaDTO) {
        MesaDTO nueva = mesaService.crearMesa(mesaDTO);
        return ResponseEntity.ok(nueva);
    }

    @GetMapping
    public ResponseEntity<List<MesaDTO>> listarMesas() {
        return ResponseEntity.ok(mesaService.listarMesas());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaDTO> actualizarCliente(@PathVariable Long id,
                                                        @RequestBody MesaDTO mesaDTO) {
        return ResponseEntity.ok(mesaService.actualizarMesa(id, mesaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        mesaService.eliminarMesa(id);
        return ResponseEntity.noContent().build();
    }
}
