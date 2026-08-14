package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.Usuario;
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

        detalle = DetalleInspeccion.builder()
                .id(10L)
                .contenedor(contenedor)
                .creadoPorUsuario(user)
                .visitado(true)
                .porcentajeEstimado(BigDecimal.valueOf(75))
                .kilosCalculados(BigDecimal.valueOf(375))
                .build();
    }

    @Test
    void shouldGenerateValidExcelZipReport() throws Exception {
        when(detalleRepository.findAll()).thenReturn(List.of(detalle));

        byte[] zipBytes = adminReportService.generateExcelZipReport(null, null);

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);
    }
}
