package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.DetalleInspeccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private DetalleInspeccionRepository detalleRepository;

    @InjectMocks
    private AdminReportService adminReportService;

    private DetalleInspeccion detalle;

    @BeforeEach
    void setUp() {
        Comuna comuna = Comuna.builder().id(1L).nombre("San Antonio").build();
        Contenedor contenedor = Contenedor.builder()
                .id(100L)
                .nombrePunto("Punto Central")
                .comuna(comuna)
                .build();
        Usuario user = Usuario.builder().id(5L).nombre("Inspector Juan").rol(Rol.INSPECTOR).build();
        InspeccionSemanal inspeccionSemanal = InspeccionSemanal.builder()
                .id(1L)
                .semanaNumero(33)
                .anio(2026)
                .tipoRuta(TipoRuta.INSPECTOR)
                .comuna(comuna)
                .build();

        detalle = DetalleInspeccion.builder()
                .id(10L)
                .inspeccionSemanal(inspeccionSemanal)
                .contenedor(contenedor)
                .creadoPorUsuario(user)
                .visitado(true)
                .porcentajeEstimado(BigDecimal.valueOf(75))
                .kilosCalculados(BigDecimal.valueOf(375))
                .build();
    }

    @Test
    void shouldGenerateValidExcelReportWithWeekFilter() throws Exception {
        when(detalleRepository.findAll()).thenReturn(List.of(detalle));

        byte[] excelBytes = adminReportService.generateExcelReport(1L, 5L, 33, 2026);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    void shouldGenerateExcelReportWithAndWithoutIdColumn() throws Exception {
        when(detalleRepository.findAll()).thenReturn(List.of(detalle));

        // Con ID: Columna 0 debe ser "ID Detalle"
        byte[] excelWithId = adminReportService.generateExcelReport(1L, 5L, 33, 2026, true);
        assertNotNull(excelWithId);
        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excelWithId))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(1); // Row 0 is Banner, Row 1 is Header
            assertEquals("ID Detalle", headerRow.getCell(0).getStringCellValue());
        }

        // Sin ID: Columna 0 debe ser "Semana / Año"
        byte[] excelWithoutId = adminReportService.generateExcelReport(1L, 5L, 33, 2026, false);
        assertNotNull(excelWithoutId);
        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excelWithoutId))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(1);
            assertEquals("Semana / Año", headerRow.getCell(0).getStringCellValue());
        }
    }

    @Test
    void shouldGenerateValidPdfReportWithWeekFilter() throws Exception {
        when(detalleRepository.findAll()).thenReturn(List.of(detalle));

        byte[] pdfBytes = adminReportService.generatePdfReport(1L, 5L, 33, 2026);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        byte[] pdfWithId = adminReportService.generatePdfReport(1L, 5L, 33, 2026, true);
        assertNotNull(pdfWithId);
        assertTrue(pdfWithId.length > 0);

        byte[] pdfWithoutId = adminReportService.generatePdfReport(1L, 5L, 33, 2026, false);
        assertNotNull(pdfWithoutId);
        assertTrue(pdfWithoutId.length > 0);
    }

    @Test
    void shouldGenerateReportsForChoferWithKilosRetirados() throws Exception {
        Comuna comuna = Comuna.builder().id(1L).nombre("San Antonio").build();
        Contenedor contenedor = Contenedor.builder().id(101L).nombrePunto("Punto 2").comuna(comuna).build();
        Usuario chofer = Usuario.builder().id(6L).nombre("Chofer Pedro").rol(Rol.CHOFER).build();
        InspeccionSemanal rutaChofer = InspeccionSemanal.builder()
                .id(2L)
                .semanaNumero(33)
                .anio(2026)
                .tipoRuta(TipoRuta.CHOFER)
                .comuna(comuna)
                .build();
        DetalleInspeccion detChofer = DetalleInspeccion.builder()
                .id(11L)
                .inspeccionSemanal(rutaChofer)
                .contenedor(contenedor)
                .creadoPorUsuario(chofer)
                .visitado(true)
                .porcentajeEstimado(BigDecimal.valueOf(80))
                .kilosCalculados(BigDecimal.valueOf(400))
                .kilosRetirados(BigDecimal.valueOf(390))
                .build();

        when(detalleRepository.findAll()).thenReturn(List.of(detalle, detChofer));

        // Filtrando por chofer específico
        byte[] excelChofer = adminReportService.generateExcelReport(null, 6L, null, null);
        assertNotNull(excelChofer);
        assertTrue(excelChofer.length > 0);

        byte[] pdfChofer = adminReportService.generatePdfReport(null, 6L, null, null);
        assertNotNull(pdfChofer);
        assertTrue(pdfChofer.length > 0);

        // Filtrando por todos los usuarios activos (usuarioId == null)
        byte[] excelAll = adminReportService.generateExcelReport(null, null, null, null);
        assertNotNull(excelAll);
        assertTrue(excelAll.length > 0);

        byte[] pdfAll = adminReportService.generatePdfReport(null, null, null, null);
        assertNotNull(pdfAll);
        assertTrue(pdfAll.length > 0);
    }
}
