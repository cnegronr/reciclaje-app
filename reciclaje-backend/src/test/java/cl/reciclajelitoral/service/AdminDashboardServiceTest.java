package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.DashboardMetricsDTO;
import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import cl.reciclajelitoral.util.WeekDateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private DetalleInspeccionRepository detalleRepository;
    @Mock private ComunaRepository comunaRepository;
    @Mock private FotoInspeccionRepository fotoRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    private Usuario user;
    private Comuna comuna;
    private Contenedor cont;
    private DetalleInspeccion det;

    @BeforeEach
    void setUp() {
        user = Usuario.builder().id(1L).nombre("User 1").rol(Rol.INSPECTOR).build();
        comuna = Comuna.builder().id(1L).nombre("San Antonio").codigoRegion("V").build();
        cont = Contenedor.builder().id(10L).comuna(comuna).build();
        det = DetalleInspeccion.builder()
                .id(100L)
                .visitado(true)
                .fechaHoraInicial(LocalDateTime.now())
                .creadoPorUsuario(user)
                .actualizadoPorUsuario(user)
                .contenedor(cont)
                .porcentajeEstimado(BigDecimal.valueOf(50))
                .kilosCalculados(BigDecimal.valueOf(250))
                .build();

        lenient().when(usuarioRepository.findAll()).thenReturn(List.of(user));
        lenient().when(contenedorRepository.findAll()).thenReturn(List.of(cont));
        lenient().when(detalleRepository.findAll()).thenReturn(List.of(det));
        lenient().when(comunaRepository.findAll()).thenReturn(List.of(comuna));
        lenient().when(fotoRepository.count()).thenReturn(5L);
    }

    @Test
    @DisplayName("Debe retornar metricas historicas completas")
    void shouldReturnCorrectMetrics() {
        DashboardMetricsDTO dto = adminDashboardService.getMetrics("ALL", "HISTORIC", null, null, null, null);

        assertNotNull(dto);
        assertEquals(1L, dto.getTotalUsuarios());
        assertEquals(1L, dto.getTotalContenedores());
        assertEquals(1L, dto.getTotalInspecciones());
        assertEquals(BigDecimal.valueOf(250), dto.getTotalKilosCalculados());
        assertEquals(5L, dto.getTotalFotosCargadas());
    }

    @Test
    @DisplayName("Debe filtrar metricas por periodo DAY, WEEK, MONTH, YEAR")
    void shouldFilterMetricsByPeriod() {
        DashboardMetricsDTO dayDto = adminDashboardService.getMetrics("ALL", "DAY", null, null, null, null);
        assertNotNull(dayDto);
        assertEquals(1L, dayDto.getTotalInspecciones());

        DashboardMetricsDTO weekDto = adminDashboardService.getMetrics("ALL", "WEEK", null, null, null, null);
        assertNotNull(weekDto);
        assertEquals(1L, weekDto.getTotalInspecciones());

        DashboardMetricsDTO monthDto = adminDashboardService.getMetrics("ALL", "MONTH", null, null, null, null);
        assertNotNull(monthDto);
        assertEquals(1L, monthDto.getTotalInspecciones());

        DashboardMetricsDTO yearDto = adminDashboardService.getMetrics("ALL", "YEAR", null, null, null, null);
        assertNotNull(yearDto);
        assertEquals(1L, yearDto.getTotalInspecciones());

        DashboardMetricsDTO pastWeekDto = adminDashboardService.getMetrics("ALL", "PAST_WEEK", null, null, null, null);
        assertNotNull(pastWeekDto);
    }

    @Test
    @DisplayName("Debe filtrar metricas por userId, comunaId, role y region")
    void shouldFilterMetricsByParams() {
        DashboardMetricsDTO dto = adminDashboardService.getMetrics("INDIVIDUAL", "HISTORIC", 1L, 1L, "INSPECTOR", "V");
        assertNotNull(dto);
        assertEquals(1L, dto.getTotalInspecciones());

        DashboardMetricsDTO emptyUserDto = adminDashboardService.getMetrics("INDIVIDUAL", "HISTORIC", 99L, null, null, null);
        assertNotNull(emptyUserDto);
        assertEquals(0L, emptyUserDto.getTotalInspecciones());
    }

    @Test
    @DisplayName("Debe clasificar correctamente inspeccion de la semana pasada basandose en fechaHoraInicial")
    void shouldClassifyPastWeekInspectionByEffectiveTimestamp() {
        // Simular inspeccion realizada hace 7 dias (semana pasada)
        LocalDateTime pastWeekDate = LocalDateTime.now().minusDays(7);
        InspeccionSemanal semanaActiva = InspeccionSemanal.builder()
                .id(2L)
                .semanaNumero(WeekDateUtils.getCurrentWeekNumber()) // semanaNumero = 34
                .anio(WeekDateUtils.getCurrentYear())
                .build();

        DetalleInspeccion detPastWeek = DetalleInspeccion.builder()
                .id(200L)
                .visitado(true)
                .fechaHoraInicial(pastWeekDate) // fechaHoraInicial = semana 33 (hace 7 dias)
                .inspeccionSemanal(semanaActiva)
                .creadoPorUsuario(user)
                .actualizadoPorUsuario(user)
                .contenedor(cont)
                .porcentajeEstimado(BigDecimal.valueOf(80))
                .kilosCalculados(BigDecimal.valueOf(400))
                .build();

        when(detalleRepository.findAll()).thenReturn(List.of(detPastWeek));

        // En la semana actual (WEEK) y hoy (DAY) debe retornar 0
        DashboardMetricsDTO weekDto = adminDashboardService.getMetrics("ALL", "WEEK", 1L, null, null, null);
        assertEquals(0L, weekDto.getTotalInspecciones());

        DashboardMetricsDTO dayDto = adminDashboardService.getMetrics("ALL", "DAY", 1L, null, null, null);
        assertEquals(0L, dayDto.getTotalInspecciones());

        // En la semana anterior (PAST_WEEK) e historico debe retornar 1
        DashboardMetricsDTO pastWeekDto = adminDashboardService.getMetrics("ALL", "PAST_WEEK", 1L, null, null, null);
        assertEquals(1L, pastWeekDto.getTotalInspecciones());

        DashboardMetricsDTO historicDto = adminDashboardService.getMetrics("ALL", "HISTORIC", 1L, null, null, null);
        assertEquals(1L, historicDto.getTotalInspecciones());
    }
}
