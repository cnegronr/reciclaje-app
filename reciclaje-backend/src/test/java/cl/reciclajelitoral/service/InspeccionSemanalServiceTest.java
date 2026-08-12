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
    private S3StorageService s3StorageService;

    @InjectMocks
    private InspeccionSemanalService inspeccionService;

    private InspeccionSemanal inspeccionSemanalMock;
    private Contenedor contenedorEmpresaMock;
    private Comuna comunaMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        comunaMock = Comuna.builder().id(1L).nombre("El Quisco").codigoRegion("V").build();
        usuarioMock = Usuario.builder().id(1L).nombre("Inspector Test").email("inspector@reciclajelitoral.cl").build();

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
        when(inspeccionRepository.findById(1L)).thenReturn(Optional.of(inspeccionSemanalMock));

        InspeccionSemanalDTO dto = inspeccionService.obtenerOCrearInspeccionSemanal(1L, 1L);

        assertNotNull(dto);
        verify(detalleRepository).save(any(DetalleInspeccion.class));
    }

    @Test
    @DisplayName("obtenerOCrearInspeccionSemanal: debe lanzar excepcion si la comuna no existe")
    void obtenerOCrearInspeccionSemanalComunaNoEncontrada() {
        when(inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(comunaRepository.findById(1L)).thenReturn(Optional.empty());

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
}
