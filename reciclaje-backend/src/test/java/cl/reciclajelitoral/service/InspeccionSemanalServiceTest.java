package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.InspeccionSemanalDTO;
import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspeccionSemanalServiceTest {

    @Mock
    private InspeccionSemanalRepository inspeccionRepository;

    @Mock
    private DetalleInspeccionRepository detalleRepository;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionInspectorRepository asignacionRepository;

    @Mock
    private S3StorageService s3StorageService;

    @org.mockito.Spy
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private InspeccionSemanalService inspeccionService;

    private InspeccionSemanal inspeccionSemanalMock;
    private Contenedor contenedorEmpresaMock;
    private Comuna comunaMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        comunaMock = Comuna.builder().id(1L).nombre("El Quisco").codigoRegion("V").build();
        usuarioMock = Usuario.builder().id(1L).nombre("Inspector Test").email("inspector@reciclajelitoral.cl").rol(Rol.INSPECTOR).build();

        inspeccionSemanalMock = InspeccionSemanal.builder()
                .id(1L)
                .comuna(comunaMock)
                .inspector(usuarioMock)
                .semanaNumero(32)
                .anio(2026)
                .estado(EstadoInspeccion.EN_PROGRESO)
                .detalles(new ArrayList<>())
                .build();

        contenedorEmpresaMock = Contenedor.builder()
                .id(10L)
                .comuna(comunaMock)
                .nombrePunto("Punto Centro Acopio")
                .categoria(CategoriaContenedor.EMPRESA)
                .kilosMaximos(BigDecimal.valueOf(500))
                .build();
    }

    @Test
    @DisplayName("Debe calcular la fecha limite cuando la fecha actual es antes del Domingo a las 20:00")
    void calcularFechaLimiteSemanalAntesDeDomingo20() {
        LocalDateTime martes = LocalDateTime.of(2026, 8, 11, 10, 0, 0);

        LocalDateTime limite = inspeccionService.calcularFechaLimiteSemanal(martes);

        assertNotNull(limite);
        assertEquals(DayOfWeek.SUNDAY, limite.getDayOfWeek());
        assertEquals(16, limite.getDayOfMonth());
        assertEquals(20, limite.getHour());
    }

    @Test
    @DisplayName("Debe calcular la fecha limite del PROXIMO Domingo cuando la fecha actual es despues del Domingo a las 20:00")
    void calcularFechaLimiteSemanalDespuesDeDomingo20() {
        LocalDateTime domingoTarde = LocalDateTime.of(2026, 8, 16, 21, 0, 0);

        LocalDateTime limite = inspeccionService.calcularFechaLimiteSemanal(domingoTarde);

        assertNotNull(limite);
        assertEquals(DayOfWeek.SUNDAY, limite.getDayOfWeek());
        assertEquals(23, limite.getDayOfMonth());
        assertEquals(20, limite.getHour());
    }

    @Test
    @DisplayName("Debe delegar el calculo de fecha limite actual sin parametros")
    void calcularFechaLimiteSemanalSinParametros() {
        LocalDateTime limite = inspeccionService.calcularFechaLimiteSemanal();

        assertNotNull(limite);
        assertEquals(DayOfWeek.SUNDAY, limite.getDayOfWeek());
        assertEquals(20, limite.getHour());
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe retornar existente si ya existe con detalles y fotos convertidos")
    void obtenerOCrearInspeccionSemanalExistente() {
        FotoInspeccion fotoMock = FotoInspeccion.builder()
                .id(100L)
                .momento(MomentoFoto.INICIAL_ANTES)
                .urlFoto("http://s3.com/foto.jpg")
                .creadoEn(LocalDateTime.now())
                .build();

        DetalleInspeccion detalleMock = DetalleInspeccion.builder()
                .id(50L)
                .contenedor(contenedorEmpresaMock)
                .porcentajeEstimado(BigDecimal.valueOf(80))
                .kilosCalculados(BigDecimal.valueOf(400))
                .visitado(true)
                .fotos(List.of(fotoMock))
                .build();

        inspeccionSemanalMock.getDetalles().add(detalleMock);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(inspeccionSemanalMock));

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1, dto.getDetalles().size());
        assertEquals(1, dto.getDetalles().get(0).getFotos().size());
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe manejar detalles y fotos nulas en la conversion DTO")
    void obtenerOCrearInspeccionSemanalConDetallesYFotosNull() {
        InspeccionSemanal inspeccionSinDetalles = InspeccionSemanal.builder()
                .id(2L)
                .comuna(comunaMock)
                .inspector(usuarioMock)
                .semanaNumero(32)
                .anio(2026)
                .estado(EstadoInspeccion.EN_PROGRESO)
                .detalles(null)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(inspeccionSinDetalles));

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L);

        assertNotNull(dto);
        assertTrue(dto.getDetalles().isEmpty());
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe manejar fotos nulas dentro del detalle")
    void obtenerOCrearInspeccionSemanalConFotosNullEnDetalle() {
        DetalleInspeccion detalleFotosNull = DetalleInspeccion.builder()
                .id(51L)
                .contenedor(contenedorEmpresaMock)
                .visitado(false)
                .fotos(null)
                .build();

        inspeccionSemanalMock.getDetalles().add(detalleFotosNull);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(inspeccionSemanalMock));

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L);

        assertNotNull(dto);
        assertEquals(1, dto.getDetalles().size());
        assertTrue(dto.getDetalles().get(0).getFotos().isEmpty());
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe crear nueva inspeccion e inicializar contenedores si no existe")
    void obtenerOCrearInspeccionSemanalNueva() {
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(contenedorRepository.findByComunaId(1L)).thenReturn(List.of(contenedorEmpresaMock));

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L);

        assertNotNull(dto);
        verify(detalleRepository).save(any(DetalleInspeccion.class));
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe lanzar excepcion si la comuna no existe")
    void obtenerOCrearInspeccionSemanalComunaNoEncontrada() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(comunaRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        assertThrows(IllegalArgumentException.class, () -> inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L));
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe lanzar excepcion si el inspector no existe")
    void obtenerOCrearInspeccionSemanalInspectorNoEncontrado() {
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L));
    }

    @Test
    @DisplayName("Inspeccion Inicial: debe registrar porcentaje, subir fotos a S3, calcular kilos y marcar fechaHoraInicial")
    void registrarInspeccionInicial() {
        DetalleInspeccion detalleNuevo = DetalleInspeccion.builder()
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .visitado(false)
                .fotos(new ArrayList<>())
                .build();

        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleNuevo));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));
        when(s3StorageService.subirFotoAS3(anyString(), anyString())).thenAnswer(i -> i.getArgument(0));

        InspeccionSemanalDTO resultado = inspeccionService.registrarOActualizarInspeccion(
                1L,
                10L,
                BigDecimal.valueOf(80),
                "Acceso despejado",
                List.of("http://s3.com/foto_antes_1.jpg"),
                List.of("http://s3.com/foto_despues_1.jpg"),
                false
        );

        assertNotNull(resultado);
        verify(s3StorageService, times(2)).subirFotoAS3(anyString(), anyString());
        verify(detalleRepository).save(argThat(detalle -> {
            assertTrue(detalle.getVisitado());
            assertEquals(0, BigDecimal.valueOf(400).compareTo(detalle.getKilosCalculados()));
            assertNotNull(detalle.getFechaHoraInicial());
            assertNull(detalle.getFechaHoraActualizacion());
            assertEquals(2, detalle.getFotos().size());
            return true;
        }));
    }

    @Test
    @DisplayName("Modo Actualizacion cuando fechaHoraInicial es null: debe tratarlo como inicial")
    void registrarActualizacionConFechaInicialNull() {
        DetalleInspeccion detalleSinFechaInicial = DetalleInspeccion.builder()
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .visitado(false)
                .fechaHoraInicial(null)
                .fotos(new ArrayList<>())
                .build();

        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleSinFechaInicial));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));

        inspeccionService.registrarOActualizarInspeccion(
                1L,
                10L,
                BigDecimal.valueOf(70),
                "Primera visita en modo edicion",
                List.of("http://s3.com/foto_antes.jpg"),
                List.of("http://s3.com/foto_despues.jpg"),
                true
        );

        verify(detalleRepository).save(argThat(detalle -> {
            assertNotNull(detalle.getFechaHoraInicial());
            assertNull(detalle.getFechaHoraActualizacion());
            assertEquals(2, detalle.getFotos().size());
            return true;
        }));
    }

    @Test
    @DisplayName("Inspeccion Inicial con listas de fotos nulas: no debe fallar")
    void registrarInspeccionInicialFotosNull() {
        DetalleInspeccion detalleNuevo = DetalleInspeccion.builder()
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .visitado(false)
                .fotos(new ArrayList<>())
                .build();

        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleNuevo));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));

        InspeccionSemanalDTO resultado = inspeccionService.registrarOActualizarInspeccion(
                1L,
                10L,
                BigDecimal.valueOf(50),
                "Sin fotos",
                null,
                null,
                false
        );

        assertNotNull(resultado);
        verify(detalleRepository).save(argThat(detalle -> detalle.getFotos().isEmpty()));
    }

    @Test
    @DisplayName("Modo Actualizacion: debe preservar fotos iniciales y registrar fechaHoraActualizacion")
    void registrarActualizacionInspeccion() {
        LocalDateTime fechaInicialPrevio = LocalDateTime.now().minusDays(1);
        
        FotoInspeccion fotoInicialAntes = FotoInspeccion.builder()
                .momento(MomentoFoto.INICIAL_ANTES)
                .urlFoto("http://s3.com/foto_inicial_antes.jpg")
                .build();

        DetalleInspeccion detalleExistente = DetalleInspeccion.builder()
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .visitado(true)
                .porcentajeEstimado(BigDecimal.valueOf(50))
                .kilosCalculados(BigDecimal.valueOf(250))
                .fechaHoraInicial(fechaInicialPrevio)
                .fotos(new ArrayList<>(List.of(fotoInicialAntes)))
                .build();

        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleExistente));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));
        when(s3StorageService.subirFotoAS3(anyString(), anyString())).thenAnswer(i -> i.getArgument(0));

        inspeccionService.registrarOActualizarInspeccion(
                1L,
                10L,
                BigDecimal.valueOf(90),
                "Actualización",
                List.of("http://s3.com/foto_update_antes.jpg"),
                List.of("http://s3.com/foto_update_despues.jpg"),
                true
        );

        verify(detalleRepository).save(argThat(detalle -> {
            assertEquals(fechaInicialPrevio, detalle.getFechaHoraInicial());
            assertNotNull(detalle.getFechaHoraActualizacion());
            assertEquals(3, detalle.getFotos().size());
            return true;
        }));
    }

    @Test
    @DisplayName("Modo Actualizacion con listas de fotos nulas: debe preservar fotos previas")
    void registrarActualizacionInspeccionFotosNull() {
        LocalDateTime fechaInicialPrevio = LocalDateTime.now().minusDays(1);

        DetalleInspeccion detalleExistente = DetalleInspeccion.builder()
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .visitado(true)
                .fechaHoraInicial(fechaInicialPrevio)
                .fotos(new ArrayList<>())
                .build();

        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleExistente));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));

        inspeccionService.registrarOActualizarInspeccion(
                1L,
                10L,
                BigDecimal.valueOf(60),
                "Sin fotos nuevas",
                null,
                null,
                true
        );

        verify(detalleRepository).save(argThat(detalle -> {
            assertNotNull(detalle.getFechaHoraActualizacion());
            assertTrue(detalle.getFotos().isEmpty());
            return true;
        }));
    }

    @Test
    @DisplayName("Debe crear un detalle nuevo si la inspeccion existe pero no el detalle previo")
    void registrarInspeccionDetalleNoExiste() {
        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.empty());
        when(inspeccionRepository.findById(1L)).thenReturn(Optional.of(inspeccionSemanalMock));
        when(contenedorRepository.findById(10L)).thenReturn(Optional.of(contenedorEmpresaMock));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenReturn(inspeccionSemanalMock);
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));

        InspeccionSemanalDTO dto = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(50), "Nuevo", null, null, false
        );

        assertNotNull(dto);
        verify(detalleRepository).save(any(DetalleInspeccion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la inspeccion semanal no existe")
    void registrarInspeccionNoEncontrada() {
        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.empty());
        when(inspeccionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(50), null, null, null, false
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el contenedor no existe")
    void registrarContenedorNoEncontrado() {
        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.empty());
        when(inspeccionRepository.findById(1L)).thenReturn(Optional.of(inspeccionSemanalMock));
        when(contenedorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(50), null, null, null, false
        ));
    }

    @Test
    @DisplayName("finalizarRutaSemanal: debe cambiar el estado a FINALIZADO")
    void finalizarRutaSemanalExitoso() {
        when(inspeccionRepository.findById(1L)).thenReturn(Optional.of(inspeccionSemanalMock));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenAnswer(i -> i.getArgument(0));

        InspeccionSemanalDTO dto = inspeccionService.finalizarRutaSemanal(1L);

        assertNotNull(dto);
        assertEquals("FINALIZADO", dto.getEstado());
    }

    @Test
    @DisplayName("finalizarRutaSemanal: debe lanzar excepcion si no se encuentra la inspeccion")
    void finalizarRutaSemanalNoEncontrada() {
        when(inspeccionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inspeccionService.finalizarRutaSemanal(1L));
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe asociar automaticamente el inspector primario a las inspecciones del CHOFER")
    void obtenerOCrearInspeccionSemanalChofer() {
        Usuario choferMock = Usuario.builder().id(2L).nombre("Pedro Chofer").email("chofer@reciclajelitoral.cl").rol(Rol.CHOFER).build();
        Usuario inspectorPrimarioMock = Usuario.builder().id(1L).nombre("John Inspector").email("inspector@reciclajelitoral.cl").rol(Rol.INSPECTOR).build();
        AsignacionInspector asignacionChofer = AsignacionInspector.builder().id(9L).inspector(choferMock).comuna(comunaMock).build();
        AsignacionInspector asignacionInspector = AsignacionInspector.builder().id(10L).inspector(inspectorPrimarioMock).comuna(comunaMock).build();

        when(inspeccionRepository.findByComunaIdAndTipoRutaAndSemanaNumeroAndAnio(anyLong(), eq(TipoRuta.CHOFER), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(choferMock));
        when(asignacionRepository.findByComunaId(1L)).thenReturn(List.of(asignacionChofer, asignacionInspector));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenAnswer(i -> {
            InspeccionSemanal arg = i.getArgument(0);
            arg.setId(20L);
            return arg;
        });

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 2L);

        assertNotNull(dto);
        assertEquals(2L, dto.getInspectorId());
        assertEquals(1L, dto.getInspectorAsociadoId());
        assertEquals("John Inspector", dto.getInspectorAsociadoNombre());
        assertEquals("CHOFER", dto.getRolUsuario());
    }

    @Test
    @DisplayName("registrarOActualizarInspeccion: debe atribuir el creador y actualizador en colaboracion multi-chofer")
    void registrarInspeccionColaboracionMultiChofer() {
        Usuario pedroChofer = Usuario.builder().id(2L).nombre("Pedro Chofer").email("chofer@reciclajelitoral.cl").rol(Rol.CHOFER).build();
        Usuario juanChofer = Usuario.builder().id(3L).nombre("Juan Chofer").email("chofer2@reciclajelitoral.cl").rol(Rol.CHOFER).build();

        DetalleInspeccion detalleExistente = DetalleInspeccion.builder()
                .id(50L)
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .creadoPorUsuario(pedroChofer)
                .actualizadoPorUsuario(pedroChofer)
                .porcentajeEstimado(BigDecimal.valueOf(40))
                .kilosCalculados(BigDecimal.valueOf(200))
                .porcentajeEstimadoInicial(BigDecimal.valueOf(40))
                .kilosCalculadosInicial(BigDecimal.valueOf(200))
                .observacionesInicial("Inicial por Pedro")
                .fechaHoraInicial(LocalDateTime.now().minusHours(2))
                .visitado(true)
                .fotos(new ArrayList<>())
                .build();

        InspeccionSemanal inspeccionMock = InspeccionSemanal.builder()
                .id(1L)
                .comuna(comunaMock)
                .inspector(pedroChofer)
                .semanaNumero(32)
                .anio(2026)
                .estado(EstadoInspeccion.EN_PROGRESO)
                .detalles(List.of(detalleExistente))
                .build();
        detalleExistente.setInspeccionSemanal(inspeccionMock);

        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleExistente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(juanChofer));
        when(s3StorageService.subirFotoAS3(anyString(), anyString())).thenReturn("https://s3.fake/foto_act.jpg");
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenAnswer(i -> i.getArgument(0));

        InspeccionSemanalDTO dto = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(80), "Inspeccionado por Juan",
                List.of("data:image/png;base64,abc"), List.of("data:image/png;base64,xyz"), true, 3L
        );

        assertNotNull(dto);
        assertEquals(1, dto.getDetalles().get(0).getActualizacionesHistorial().size());
        assertEquals("Juan Chofer", dto.getDetalles().get(0).getActualizacionesHistorial().get(0).getUsuarioNombre());
        assertEquals(BigDecimal.valueOf(80), dto.getDetalles().get(0).getActualizacionesHistorial().get(0).getPorcentajeEstimado());
        assertEquals("Inspeccionado por Juan", dto.getDetalles().get(0).getActualizacionesHistorial().get(0).getObservaciones());
        assertEquals(BigDecimal.valueOf(40), dto.getDetalles().get(0).getPorcentajeEstimadoInicial());

        verify(detalleRepository).save(argThat(d -> {
            assertEquals("Pedro Chofer", d.getCreadoPorUsuario().getNombre());
            assertEquals("Juan Chofer", d.getActualizadoPorUsuario().getNombre());
            assertEquals(BigDecimal.valueOf(40), d.getPorcentajeEstimadoInicial());
            assertEquals(1, d.getActualizaciones().size());
            assertEquals(BigDecimal.valueOf(80), d.getActualizaciones().get(0).getPorcentajeEstimado());
            assertEquals("Inspeccionado por Juan", d.getActualizaciones().get(0).getObservaciones());
            assertEquals("Juan Chofer", d.getActualizaciones().get(0).getUsuario().getNombre());
            assertEquals(2, d.getActualizaciones().get(0).getFotos().size());
            return true;
        }));
    }

    @Test
    @DisplayName("registrarOActualizarInspeccion: asignacion automatica de comentarios por defecto y foto-less")
    void registrarInspeccionMatrizComentarios() {
        Usuario inspector = Usuario.builder().id(1L).nombre("John Inspector").email("inspector@reciclajelitoral.cl").rol(Rol.INSPECTOR).build();

        DetalleInspeccion detalleNulo = DetalleInspeccion.builder()
                .id(100L)
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .porcentajeEstimado(BigDecimal.valueOf(30))
                .kilosCalculados(BigDecimal.valueOf(150))
                .build();
        inspeccionSemanalMock.setDetalles(List.of(detalleNulo));

        // 1. Registro inicial sin comentario -> "Registro inicial"
        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleNulo));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(inspector));
        when(detalleRepository.save(any(DetalleInspeccion.class))).thenAnswer(i -> i.getArgument(0));
        when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenAnswer(i -> i.getArgument(0));

        InspeccionSemanalDTO dtoInicial = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(30), "   ",
                null, null, false, 1L
        );
        assertEquals("Registro inicial", dtoInicial.getDetalles().get(0).getObservaciones());

        // 2. Actualización con fotos sin comentario -> "Actualización de fotos"
        DetalleInspeccion detalleConInicial = DetalleInspeccion.builder()
                .id(50L)
                .inspeccionSemanal(inspeccionSemanalMock)
                .contenedor(contenedorEmpresaMock)
                .creadoPorUsuario(inspector)
                .actualizadoPorUsuario(inspector)
                .porcentajeEstimado(BigDecimal.valueOf(30))
                .kilosCalculados(BigDecimal.valueOf(150))
                .porcentajeEstimadoInicial(BigDecimal.valueOf(30))
                .fechaHoraInicial(LocalDateTime.now().minusHours(3))
                .visitado(true)
                .build();
        inspeccionSemanalMock.setDetalles(List.of(detalleConInicial));
        when(detalleRepository.findByInspeccionSemanalIdAndContenedorId(1L, 10L))
                .thenReturn(Optional.of(detalleConInicial));
        when(s3StorageService.subirFotoAS3(anyString(), anyString())).thenReturn("https://s3.fake/foto_act.jpg");

        InspeccionSemanalDTO dtoActFotos = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(50), null,
                null, List.of("data:image/png;base64,123"), true, 1L
        );
        assertEquals("Actualización de fotos", dtoActFotos.getDetalles().get(0).getActualizacionesHistorial().get(0).getObservaciones());

        // 3. Actualización sin fotos sin comentario -> "Actualización de porcentaje"
        InspeccionSemanalDTO dtoActPorcentaje = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(70), "",
                List.of(), List.of(), true, 1L
        );
        assertEquals("Actualización de porcentaje", dtoActPorcentaje.getDetalles().get(0).getActualizacionesHistorial().get(1).getObservaciones());

        // 4. Actualización sin fotos con comentario nuevo -> "Comentario actualizado: <texto>"
        InspeccionSemanalDTO dtoActComentario = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(70), "Contenedor limpio",
                null, null, true, 1L
        );
        assertEquals("Comentario actualizado: Contenedor limpio", dtoActComentario.getDetalles().get(0).getActualizacionesHistorial().get(2).getObservaciones());

        // 5. Actualización con comentario ya formateado -> Preserva el prefijo sin duplicar
        InspeccionSemanalDTO dtoActRepetido = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(70), "Comentario actualizado: Ya verificado",
                null, null, true, 1L
        );
        assertEquals("Comentario actualizado: Ya verificado", dtoActRepetido.getDetalles().get(0).getActualizacionesHistorial().get(3).getObservaciones());

        InspeccionSemanalDTO dtoActRepetidoPorcentaje = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(70), "Actualización de porcentaje: Desgaste en tapa",
                null, null, true, 1L
        );
        assertEquals("Actualización de porcentaje: Desgaste en tapa", dtoActRepetidoPorcentaje.getDetalles().get(0).getActualizacionesHistorial().get(4).getObservaciones());

        // 6. Subir nuevas fotos cuando las observaciones anteriores eran "Actualización de porcentaje" o "Registro inicial" -> Sobreescribe a "Actualización de fotos"
        InspeccionSemanalDTO dtoFotosSobrePorcentaje = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(80), "Actualización de porcentaje",
                List.of("data:image/png;base64,456"), null, true, 1L
        );
        assertEquals("Actualización de fotos", dtoFotosSobrePorcentaje.getDetalles().get(0).getActualizacionesHistorial().get(5).getObservaciones());

        InspeccionSemanalDTO dtoFotosSobreInicial = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(80), "Registro inicial",
                List.of("data:image/png;base64,456"), null, true, 1L
        );
        assertEquals("Actualización de fotos", dtoFotosSobreInicial.getDetalles().get(0).getActualizacionesHistorial().get(6).getObservaciones());

        InspeccionSemanalDTO dtoFotosConComentarioCustom = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(80), "Fotos de reja despejada",
                List.of("data:image/png;base64,456"), null, true, 1L
        );
        assertEquals("Fotos de reja despejada", dtoFotosConComentarioCustom.getDetalles().get(0).getActualizacionesHistorial().get(7).getObservaciones());

        InspeccionSemanalDTO dtoPorcentajeSobreFotos = inspeccionService.registrarOActualizarInspeccion(
                1L, 10L, BigDecimal.valueOf(90), "Actualización de fotos",
                null, null, true, 1L
        );
        assertEquals("Actualización de porcentaje", dtoPorcentajeSobreFotos.getDetalles().get(0).getActualizacionesHistorial().get(8).getObservaciones());
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe mapear correctamente detalles y fotos con autores nulos a DTO")
    void convertirADTOConAutoresNull() {
        FotoInspeccion fotoSinUsuario = FotoInspeccion.builder()
                .id(105L)
                .momento(MomentoFoto.INICIAL_ANTES)
                .urlFoto("http://s3.com/foto_anon.jpg")
                .creadoEn(LocalDateTime.now())
                .usuario(null)
                .build();

        DetalleInspeccion detalleSinAutores = DetalleInspeccion.builder()
                .id(55L)
                .contenedor(contenedorEmpresaMock)
                .creadoPorUsuario(null)
                .actualizadoPorUsuario(null)
                .porcentajeEstimado(BigDecimal.valueOf(50))
                .kilosCalculados(BigDecimal.valueOf(250))
                .visitado(true)
                .fotos(List.of(fotoSinUsuario))
                .build();

        InspeccionSemanal inspeccionConDetalleSinAutores = InspeccionSemanal.builder()
                .id(99L)
                .comuna(comunaMock)
                .inspector(usuarioMock)
                .semanaNumero(32)
                .anio(2026)
                .estado(EstadoInspeccion.EN_PROGRESO)
                .detalles(List.of(detalleSinAutores))
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(inspeccionConDetalleSinAutores));

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L);

        assertNotNull(dto);
        assertNull(dto.getDetalles().get(0).getCreadoPorUsuarioId());
        assertNull(dto.getDetalles().get(0).getCreadoPorUsuarioNombre());
        assertNull(dto.getDetalles().get(0).getActualizadoPorUsuarioId());
        assertNull(dto.getDetalles().get(0).getActualizadoPorUsuarioNombre());
        assertNull(dto.getDetalles().get(0).getFotos().get(0).getUsuarioId());
        assertNull(dto.getDetalles().get(0).getFotos().get(0).getUsuarioNombre());
    }

    @Test
    @DisplayName("Debe denegar traspaso si la semana actual ya contiene inspecciones visitadas")
    void debeDenegarTraspasoSiSemanaActualTieneVisitas() {
        int semAct = cl.reciclajelitoral.util.WeekDateUtils.getCurrentWeekNumber();
        int anioAct = cl.reciclajelitoral.util.WeekDateUtils.getCurrentYear();
        inspeccionSemanalMock.setSemanaNumero(semAct);
        inspeccionSemanalMock.setAnio(anioAct);

        DetalleInspeccion detVisitado = DetalleInspeccion.builder()
                .id(101L)
                .visitado(true)
                .contenedor(contenedorEmpresaMock)
                .build();
        inspeccionSemanalMock.getDetalles().add(detVisitado);

        usuarioMock.setRol(Rol.ADMIN);
        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        lenient().when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));
        lenient().when(inspeccionRepository.findById(1L)).thenReturn(Optional.of(inspeccionSemanalMock));
        lenient().when(inspeccionRepository.findByComunaIdAndSemanaNumeroAndAnio(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(inspeccionSemanalMock));
        lenient().when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(inspeccionSemanalMock));

        cl.reciclajelitoral.dto.TraspasoPreviewDTO preview = inspeccionService.obtenerPreviewTraspasoVisitadas(1L, 1L);

        assertNotNull(preview);
        assertFalse(preview.isPermitidoTraspaso());
        assertTrue(preview.getMensajeValidacion().contains("ya contiene"));
    }

    @Test
    @DisplayName("Debe permitir traspaso si la semana actual está limpia y copiar las visitas de la semana previa")
    void debePermitirYAplicarTraspasoCuandoSemanaActualEstaLimpia() {
        usuarioMock.setRol(Rol.ADMIN);
        int semAct = cl.reciclajelitoral.util.WeekDateUtils.getCurrentWeekNumber();
        int anioAct = cl.reciclajelitoral.util.WeekDateUtils.getCurrentYear();

        // Semana Destino (limpia)
        InspeccionSemanal semanaDestino = InspeccionSemanal.builder()
                .id(2L)
                .comuna(comunaMock)
                .inspector(usuarioMock)
                .semanaNumero(semAct)
                .anio(anioAct)
                .estado(EstadoInspeccion.EN_PROGRESO)
                .detalles(new ArrayList<>(List.of(
                        DetalleInspeccion.builder().id(201L).visitado(false).contenedor(contenedorEmpresaMock).fotos(new ArrayList<>()).build()
                )))
                .build();

        DetalleInspeccion detOrigen = DetalleInspeccion.builder()
                .id(101L)
                .visitado(true)
                .contenedor(contenedorEmpresaMock)
                .porcentajeEstimado(BigDecimal.valueOf(80))
                .kilosCalculados(BigDecimal.valueOf(400))
                .fotos(new ArrayList<>())
                .build();

        InspeccionSemanal rutaOrigenMock = InspeccionSemanal.builder()
                .id(1L)
                .comuna(comunaMock)
                .inspector(usuarioMock)
                .semanaNumero(semAct - 1)
                .anio(anioAct)
                .detalles(new ArrayList<>(List.of(detOrigen)))
                .build();
        detOrigen.setInspeccionSemanal(rutaOrigenMock);

        lenient().when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(1L, 1L, semAct, anioAct))
                .thenReturn(Optional.of(semanaDestino));
        lenient().when(inspeccionRepository.findByComunaIdAndSemanaNumeroAndAnio(1L, semAct, anioAct))
                .thenReturn(List.of(semanaDestino));
        lenient().when(inspeccionRepository.findByComunaIdAndSemanaNumeroAndAnio(1L, semAct - 1, anioAct))
                .thenReturn(List.of(rutaOrigenMock));
        lenient().when(inspeccionRepository.findInspeccionesPreviasByComuna(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(rutaOrigenMock));
        lenient().when(detalleRepository.findVisitadasByComunaId(anyLong()))
                .thenReturn(List.of(detOrigen));
        lenient().when(inspeccionRepository.findById(2L)).thenReturn(Optional.of(semanaDestino));
        lenient().when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));
        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        lenient().when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenAnswer(inv -> inv.getArgument(0));

        cl.reciclajelitoral.dto.TraspasoPreviewDTO preview = inspeccionService.obtenerPreviewTraspasoVisitadas(1L, 1L);
        assertTrue(preview.isPermitidoTraspaso());
        assertEquals(1, preview.getTotalVisitadasOrigen());

        InspeccionSemanalDTO result = inspeccionService.aplicarTraspasoVisitadas(1L, 1L);

        assertNotNull(result);
        assertTrue(result.getDetalles().get(0).getVisitado());
        assertEquals(BigDecimal.valueOf(80), result.getDetalles().get(0).getPorcentajeEstimado());
        assertTrue(result.getTieneRespaldoLimpieza());
    }

    @Test
    @DisplayName("Debe limpiar la semana actual generando un respaldo y luego revertir exitosamente")
    void debeLimpiarConRespaldoYRevertir() {
        DetalleInspeccion detPrevio = DetalleInspeccion.builder()
                .id(301L)
                .visitado(true)
                .contenedor(contenedorEmpresaMock)
                .porcentajeEstimado(BigDecimal.valueOf(60))
                .kilosCalculados(BigDecimal.valueOf(300))
                .observaciones("Previo a limpiar")
                .fotos(new ArrayList<>())
                .build();

        InspeccionSemanal rutaActual = InspeccionSemanal.builder()
                .id(3L)
                .comuna(comunaMock)
                .inspector(usuarioMock)
                .semanaNumero(cl.reciclajelitoral.util.WeekDateUtils.getCurrentWeekNumber())
                .anio(cl.reciclajelitoral.util.WeekDateUtils.getCurrentYear())
                .estado(EstadoInspeccion.EN_PROGRESO)
                .detalles(new ArrayList<>(List.of(detPrevio)))
                .build();

        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        lenient().when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));
        lenient().when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(rutaActual));
        lenient().when(inspeccionRepository.findById(3L)).thenReturn(Optional.of(rutaActual));
        lenient().when(inspeccionRepository.save(any(InspeccionSemanal.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Limpiar semana actual con respaldo
        InspeccionSemanalDTO limpiaDTO = inspeccionService.limpiarSemanaActualConRespaldo(1L, 1L);
        assertNotNull(limpiaDTO);
        assertFalse(limpiaDTO.getDetalles().get(0).getVisitado());
        assertTrue(limpiaDTO.getTieneRespaldoLimpieza());

        // 2. Revertir la limpieza
        InspeccionSemanalDTO revertidaDTO = inspeccionService.revertirLimpiezaSemanaActual(1L, 1L);
        assertNotNull(revertidaDTO);
        assertTrue(revertidaDTO.getDetalles().get(0).getVisitado());
        assertEquals(BigDecimal.valueOf(60), revertidaDTO.getDetalles().get(0).getPorcentajeEstimado());
        assertEquals("Previo a limpiar", revertidaDTO.getDetalles().get(0).getObservaciones());
        assertFalse(revertidaDTO.getTieneRespaldoLimpieza());
    }
}
