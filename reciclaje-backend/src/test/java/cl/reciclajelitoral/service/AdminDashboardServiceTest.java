package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.DashboardMetricsDTO;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.*;
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
class AdminDashboardServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private DetalleInspeccionRepository detalleRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private FotoInspeccionRepository fotoRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        Usuario user = Usuario.builder().id(1L).nombre("User 1").build();
        Comuna comuna = Comuna.builder().id(1L).nombre("San Antonio").codigoRegion("V").build();
        Contenedor cont = Contenedor.builder().id(10L).comuna(comuna).build();
        DetalleInspeccion det = DetalleInspeccion.builder()
                .id(100L)
                .visitado(true)
                .creadoPorUsuario(user)
                .contenedor(cont)
                .porcentajeEstimado(BigDecimal.valueOf(50))
                .kilosCalculados(BigDecimal.valueOf(250))
                .build();

        when(usuarioRepository.findAll()).thenReturn(List.of(user));
        when(contenedorRepository.findAll()).thenReturn(List.of(cont));
        when(detalleRepository.findAll()).thenReturn(List.of(det));
        when(comunaRepository.findAll()).thenReturn(List.of(comuna));
        when(fotoRepository.count()).thenReturn(5L);
    }

    @Test
    void shouldReturnCorrectMetrics() {
        DashboardMetricsDTO dto = adminDashboardService.getMetrics("ALL", "HISTORIC", null, null, null, null);

        assertNotNull(dto);
        assertEquals(1L, dto.getTotalUsuarios());
        assertEquals(1L, dto.getTotalContenedores());
        assertEquals(1L, dto.getTotalInspecciones());
        assertEquals(BigDecimal.valueOf(250), dto.getTotalKilosCalculados());
        assertEquals(5L, dto.getTotalFotosCargadas());
    }
}
