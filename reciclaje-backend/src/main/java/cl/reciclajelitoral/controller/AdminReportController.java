package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/excel-zip")
    public ResponseEntity<byte[]> downloadExcelZipReport(
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) Long usuarioId
    ) throws IOException {
        byte[] zipBytes = adminReportService.generateExcelZipReport(comunaId, usuarioId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Consolidado_Reciclaje.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) Long usuarioId
    ) throws Exception {
        byte[] pdfBytes = adminReportService.generatePdfReport(comunaId, usuarioId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Consolidado_Reciclaje.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
