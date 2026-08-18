package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.InspeccionSemanalDTO;
import cl.reciclajelitoral.dto.RegistrarInspeccionRequest;
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

    @GetMapping("/comuna/{comunaId}")
    public ResponseEntity<InspeccionSemanalDTO> obtenerOCrearInspeccionSemanal(
            @PathVariable Long comunaId,
            @RequestParam(defaultValue = "1") Long inspectorId
    ) {
        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(comunaId, inspectorId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{inspeccionId}/registrar")
    public ResponseEntity<InspeccionSemanalDTO> registrarOActualizarInspeccion(
            @PathVariable Long inspeccionId,
            @Valid @RequestBody RegistrarInspeccionRequest request
    ) {
        InspeccionSemanalDTO resultado = inspeccionService.registrarOActualizarInspeccion(
                inspeccionId,
                request.getContenedorId(),
                request.getPorcentajeEstimado(),
                request.getObservaciones(),
                request.getFotosAntesUrls(),
                request.getFotosDespuesUrls(),
                request.isEsActualizacion(),
                request.getUsuarioId()
        );
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/{inspeccionId}/finalizar")
    public ResponseEntity<InspeccionSemanalDTO> finalizarRutaSemanal(@PathVariable Long inspeccionId) {
        InspeccionSemanalDTO resultado = inspeccionService.finalizarRutaSemanal(inspeccionId);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/comuna/{comunaId}/preview-traspaso")
    public ResponseEntity<cl.reciclajelitoral.dto.TraspasoPreviewDTO> obtenerPreviewTraspaso(
            @PathVariable Long comunaId,
            @RequestParam(defaultValue = "1") Long inspectorId
    ) {
        return ResponseEntity.ok(inspeccionService.obtenerPreviewTraspasoVisitadas(comunaId, inspectorId));
    }

    @PostMapping("/comuna/{comunaId}/aplicar-traspaso")
    public ResponseEntity<InspeccionSemanalDTO> aplicarTraspaso(
            @PathVariable Long comunaId,
            @RequestParam(defaultValue = "1") Long inspectorId
    ) {
        return ResponseEntity.ok(inspeccionService.aplicarTraspasoVisitadas(comunaId, inspectorId));
    }

    @PostMapping("/comuna/{comunaId}/limpiar-actual")
    public ResponseEntity<InspeccionSemanalDTO> limpiarSemanaActual(
            @PathVariable Long comunaId,
            @RequestParam(defaultValue = "1") Long inspectorId
    ) {
        return ResponseEntity.ok(inspeccionService.limpiarSemanaActualConRespaldo(comunaId, inspectorId));
    }

    @PostMapping("/comuna/{comunaId}/revertir-limpieza")
    public ResponseEntity<InspeccionSemanalDTO> revertirLimpiezaSemanaActual(
            @PathVariable Long comunaId,
            @RequestParam(defaultValue = "1") Long inspectorId
    ) {
        return ResponseEntity.ok(inspeccionService.revertirLimpiezaSemanaActual(comunaId, inspectorId));
    }
}
