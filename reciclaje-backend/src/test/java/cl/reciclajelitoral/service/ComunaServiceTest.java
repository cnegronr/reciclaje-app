package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.ComunaDTO;
import cl.reciclajelitoral.entity.CategoriaContenedor;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.repository.ComunaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComunaServiceTest {

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private cl.reciclajelitoral.repository.UsuarioRepository usuarioRepository;

    @Mock
    private cl.reciclajelitoral.repository.AsignacionInspectorRepository asignacionRepository;

    @InjectMocks
    private ComunaService comunaService;

    @Test
    @DisplayName("Debe listar todas las comunas correctamente mapeadas a DTO")
    void listarTodasLasComunas() {
        Contenedor contenedorMock = Contenedor.builder()
                .id(100L)
                .sector("EL TOTORAL")
                .nombrePunto("Punto El Totoral")
                .categoria(CategoriaContenedor.MUNICIPAL)
                .kilosMaximos(BigDecimal.valueOf(1000))
                .build();

        Comuna comunaMock = Comuna.builder()
                .id(1L)
                .nombre("El Quisco")
                .codigoRegion("V")
                .contenedores(List.of(contenedorMock))
                .build();

        when(comunaRepository.findAll()).thenReturn(List.of(comunaMock));

        List<ComunaDTO> resultado = comunaService.listarTodasLasComunas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("El Quisco", resultado.get(0).getNombre());
        assertEquals(1, resultado.get(0).getContenedores().size());
        assertEquals("EL TOTORAL", resultado.get(0).getContenedores().get(0).getSector());
        assertEquals("Punto El Totoral", resultado.get(0).getContenedores().get(0).getNombrePunto());
    }

    @Test
    @DisplayName("Debe obtener comuna por ID correctamente")
    void obtenerPorIdExitoso() {
        Comuna comunaMock = Comuna.builder()
                .id(1L)
                .nombre("Algarrobo")
                .codigoRegion("V")
                .contenedores(List.of())
                .build();

        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comunaMock));

        ComunaDTO dto = comunaService.obtenerPorId(1L);

        assertNotNull(dto);
        assertEquals("Algarrobo", dto.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la comuna no existe")
    void obtenerPorIdNoEncontrado() {
        when(comunaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> comunaService.obtenerPorId(999L));
    }
}
