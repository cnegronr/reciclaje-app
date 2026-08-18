package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.ComunaDTO;
import cl.reciclajelitoral.service.ComunaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comunas")
@RequiredArgsConstructor
public class ComunaController {

    private final ComunaService comunaService;

    @GetMapping
    public ResponseEntity<List<ComunaDTO>> listarComunas(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId != null) {
            return ResponseEntity.ok(comunaService.obtenerComunasParaUsuario(usuarioId));
        }
        return ResponseEntity.ok(comunaService.listarTodasLasComunas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComunaDTO> obtenerComuna(@PathVariable Long id) {
        return ResponseEntity.ok(comunaService.obtenerPorId(id));
    }
}
