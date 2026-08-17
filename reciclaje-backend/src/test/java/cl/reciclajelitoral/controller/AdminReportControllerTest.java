package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.security.JwtAuthenticationFilter;
import cl.reciclajelitoral.security.JwtTokenProvider;
import cl.reciclajelitoral.service.AdminBackupService;
import cl.reciclajelitoral.service.AdminReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminReportService adminReportService;

    @MockBean
    private AdminBackupService adminBackupService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private cl.reciclajelitoral.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debe obtener lista de anos disponibles para reportes")
    void getAvailableReportYears() throws Exception {
        when(adminReportService.getAvailableReportYears()).thenReturn(List.of(2026, 2025));

        mockMvc.perform(get("/api/admin/reports/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2026))
                .andExpect(jsonPath("$[1]").value(2025));
    }

    @Test
    @DisplayName("Debe descargar reporte Excel directo (.xlsx) con filtro de semana")
    void downloadExcelReport() throws Exception {
        when(adminReportService.generateExcelReport(any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/admin/reports/excel?semanaNumero=33&anio=2026"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Reporte_Consolidado_Reciclaje.xlsx\""));
    }

    @Test
    @DisplayName("Debe descargar reporte PDF con filtro de semana")
    void downloadPdfReport() throws Exception {
        when(adminReportService.generatePdfReport(any(), any(), any(), any())).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/api/admin/reports/pdf?semanaNumero=33&anio=2026"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Reporte_Consolidado_Reciclaje.pdf\""));
    }

    @Test
    @DisplayName("Debe exportar respaldo SQL de la base de datos")
    void downloadDatabaseSqlBackup() throws Exception {
        when(adminBackupService.generateSqlDump()).thenReturn("INSERT INTO test;".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/admin/reports/db-backup/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/plain")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("reciclaje_db_backup_")));
    }

    @Test
    @DisplayName("Debe importar respaldo SQL de la base de datos")
    void restoreDatabaseSqlBackup() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "backup.sql", "text/plain", "INSERT INTO test;".getBytes(StandardCharsets.UTF_8));
        doNothing().when(adminBackupService).restoreSqlDump(any());

        mockMvc.perform(multipart("/api/admin/reports/db-backup/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Base de datos restaurada exitosamente desde el respaldo SQL"));
    }

    @Test
    @DisplayName("Debe retornar bad request al intentar importar un archivo vacio")
    void restoreDatabaseSqlBackupVacio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vacio.sql", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/api/admin/reports/db-backup/import").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El archivo de respaldo está vacío"));
    }
}
