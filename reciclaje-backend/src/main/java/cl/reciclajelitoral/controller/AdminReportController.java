package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.service.AdminBackupService;
import cl.reciclajelitoral.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final AdminBackupService adminBackupService;

    private boolean isAdmin() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities() != null
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/years")
    public ResponseEntity<java.util.List<Integer>> getAvailableReportYears() {
        return ResponseEntity.ok(adminReportService.getAvailableReportYears());
    }

    @GetMapping({"/excel", "/excel-zip"})
    public ResponseEntity<byte[]> downloadExcelReport(
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Integer semanaNumero,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false, defaultValue = "false") Boolean incluirId
    ) throws IOException {
        boolean effectiveIncluirId = isAdmin() && Boolean.TRUE.equals(incluirId);
        byte[] excelBytes = adminReportService.generateExcelReport(comunaId, usuarioId, semanaNumero, anio, effectiveIncluirId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Consolidado_Reciclaje.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Integer semanaNumero,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false, defaultValue = "false") Boolean incluirId
    ) throws Exception {
        boolean effectiveIncluirId = isAdmin() && Boolean.TRUE.equals(incluirId);
        byte[] pdfBytes = adminReportService.generatePdfReport(comunaId, usuarioId, semanaNumero, anio, effectiveIncluirId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Consolidado_Reciclaje.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/db-backup/export")
    public ResponseEntity<byte[]> downloadDatabaseSqlBackup() {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] sqlBytes = adminBackupService.generateSqlDump();
        String filename = "reciclaje_db_backup_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".sql";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sqlBytes);
    }

    @PostMapping(value = "/db-backup/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> restoreDatabaseSqlBackup(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) throws Exception {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "Acceso denegado: Se requiere rol ADMIN"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "El archivo de respaldo está vacío"));
        }

        adminBackupService.restoreSqlDump(file.getBytes());
        return ResponseEntity.ok(java.util.Map.of("message", "Base de datos restaurada exitosamente desde el respaldo SQL"));
    }
}
