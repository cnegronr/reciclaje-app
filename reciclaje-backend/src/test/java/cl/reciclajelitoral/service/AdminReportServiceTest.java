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
        Usuario user = Usuario.builder().id(5L).nombre("Inspector Juan").build();
        InspeccionSemanal inspeccionSemanal = InspeccionSemanal.builder()
                .id(1L)
                .semanaNumero(33)
                .anio(2026)
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
    void shouldGenerateValidPdfReportWithWeekFilter() throws Exception {
        when(detalleRepository.findAll()).thenReturn(List.of(detalle));

        byte[] pdfBytes = adminReportService.generatePdfReport(1L, 5L, 33, 2026);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
