package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBackupServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ComunaRepository comunaRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private AsignacionInspectorRepository asignacionInspectorRepository;
    @Mock private InspeccionSemanalRepository inspeccionSemanalRepository;
    @Mock private DetalleInspeccionRepository detalleInspeccionRepository;
    @Mock private ActualizacionDetalleRepository actualizacionDetalleRepository;
    @Mock private FotoInspeccionRepository fotoInspeccionRepository;
    @Mock private OutboxEventRepository outboxEventRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AdminBackupService adminBackupService;

    @Test
    @DisplayName("Debe generar dump SQL valido conteniendo todas las entidades y secuencias")
    void generateSqlDumpExitoso() {
        Usuario u = Usuario.builder().id(1L).nombre("Admin").email("admin@test.cl").passwordHash("hash").rol(Rol.ADMIN).activo(true).creadoEn(java.time.LocalDateTime.now()).build();
        Comuna c = Comuna.builder().id(10L).nombre("El Quisco").codigoRegion("V").build();
        Contenedor cont = Contenedor.builder().id(100L).nombrePunto("Punto 1").comuna(c).categoria(CategoriaContenedor.MUNICIPAL).kilosMaximos(new java.math.BigDecimal("1000")).activo(true).build();
        AsignacionInspector asig = AsignacionInspector.builder().id(1L).inspector(u).comuna(c).build();
        InspeccionSemanal ins = InspeccionSemanal.builder().id(1L).comuna(c).inspector(u).semanaNumero(33).anio(2026).estado(EstadoInspeccion.EN_PROGRESO).creadoEn(java.time.LocalDateTime.now()).build();
        DetalleInspeccion det = DetalleInspeccion.builder().id(1L).inspeccionSemanal(ins).contenedor(cont).visitado(true).porcentajeEstimado(new java.math.BigDecimal("50")).kilosCalculados(new java.math.BigDecimal("500")).build();
        ActualizacionDetalle act = ActualizacionDetalle.builder().id(1L).detalleInspeccion(det).usuario(u).porcentajeEstimado(new java.math.BigDecimal("80")).kilosCalculados(new java.math.BigDecimal("800")).build();
        FotoInspeccion foto = FotoInspeccion.builder().id(1L).detalleInspeccion(det).usuario(u).momento(MomentoFoto.INICIAL_ANTES).urlFoto("https://s3/foto.jpg").build();
        OutboxEvent outbox = OutboxEvent.builder().id(1L).aggregateType("CONTENEDOR").aggregateId("100").eventType("CREATED").payload("{}").status("PENDING").build();

        when(usuarioRepository.findAll()).thenReturn(List.of(u));
        when(comunaRepository.findAll()).thenReturn(List.of(c));
        when(contenedorRepository.findAll()).thenReturn(List.of(cont));
        when(asignacionInspectorRepository.findAll()).thenReturn(List.of(asig));
        when(inspeccionSemanalRepository.findAll()).thenReturn(List.of(ins));
        when(detalleInspeccionRepository.findAll()).thenReturn(List.of(det));
        when(actualizacionDetalleRepository.findAll()).thenReturn(List.of(act));
        when(fotoInspeccionRepository.findAll()).thenReturn(List.of(foto));
        when(outboxEventRepository.findAll()).thenReturn(List.of(outbox));

        byte[] sqlBytes = adminBackupService.generateSqlDump();
        assertNotNull(sqlBytes);
        assertTrue(sqlBytes.length > 0);

        String sqlText = new String(sqlBytes, StandardCharsets.UTF_8);
        assertTrue(sqlText.contains("INSERT INTO usuarios"));
        assertTrue(sqlText.contains("INSERT INTO comunas"));
        assertTrue(sqlText.contains("INSERT INTO contenedores"));
        assertTrue(sqlText.contains("INSERT INTO asignaciones_inspector"));
        assertTrue(sqlText.contains("INSERT INTO inspecciones_semanales"));
        assertTrue(sqlText.contains("INSERT INTO detalle_inspecciones"));
        assertTrue(sqlText.contains("INSERT INTO actualizaciones_detalle"));
        assertTrue(sqlText.contains("INSERT INTO fotos_inspeccion"));
        assertTrue(sqlText.contains("INSERT INTO outbox_events"));
        assertTrue(sqlText.contains("setval('usuarios_id_seq'"));
    }

    @Test
    @DisplayName("Debe restaurar dump SQL ejecutando las sentencias en JdbcTemplate y capturar excepciones individualmente")
    void restoreSqlDumpExitoso() {
        String script = "INSERT INTO comunas (id, nombre) VALUES (1, 'Algarrobo');\nINVALID SQL STATEMENT;";
        doNothing().when(jdbcTemplate).execute("INSERT INTO comunas (id, nombre) VALUES (1, 'Algarrobo')");
        doThrow(new RuntimeException("Error sintaxis")).when(jdbcTemplate).execute("INVALID SQL STATEMENT");

        assertDoesNotThrow(() -> adminBackupService.restoreSqlDump(script.getBytes(StandardCharsets.UTF_8)));
        verify(jdbcTemplate, times(1)).execute("INSERT INTO comunas (id, nombre) VALUES (1, 'Algarrobo')");
        verify(jdbcTemplate, times(1)).execute("INVALID SQL STATEMENT");
    }

    @Test
    @DisplayName("Debe lanzar excepcion si se intenta restaurar un archivo SQL vacio o nulo")
    void restoreSqlDumpVacio() {
        assertThrows(IllegalArgumentException.class, () -> adminBackupService.restoreSqlDump(null));
        assertThrows(IllegalArgumentException.class, () -> adminBackupService.restoreSqlDump(new byte[0]));
    }
}
