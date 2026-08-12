package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.RegistrarInspeccionRequest;
import cl.reciclajelitoral.entity.InspeccionSemanal;
import cl.reciclajelitoral.service.InspeccionSemanalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspecciones")
@RequiredArgsConstructor
public class InspeccionController {

    private final InspeccionSemanalService inspeccionService;

    @PostMapping("/{inspeccionId}/registrar")
    public ResponseEntity<InspeccionSemanal> registrarOActualizarInspeccion(
            @PathVariable Long inspeccionId,
            @Valid @RequestBody RegistrarInspeccionRequest request
    ) {
        InspeccionSemanal resultado = inspeccionService.registrarOActualizarInspeccion(
                inspeccionId,
                request.getContenedorId(),
                request.getPorcentajeEstimado(),
                request.getObservaciones(),
                request.getFotosAntesUrls(),
                request.getFotosDespuesUrls(),
                request.isEsActualizacion()
        );
        return ResponseEntity.ok(resultado);
    }
}
