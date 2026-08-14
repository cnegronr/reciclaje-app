package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.ContainerAdminDTO;
import cl.reciclajelitoral.dto.CreateContainerRequest;
import cl.reciclajelitoral.entity.CategoriaContenedor;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.repository.ComunaRepository;
import cl.reciclajelitoral.repository.ContenedorRepository;
import cl.reciclajelitoral.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminContenedorServiceTest {

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private AdminContenedorService adminContenedorService;

    private Contenedor contenedor;
    private Comuna comuna;

    @BeforeEach
    void setUp() {
        comuna = Comuna.builder().id(1L).nombre("San Antonio").build();
        contenedor = Contenedor.builder()
                .id(100L)
                .nombrePunto("Punto Central")
                .categoria(CategoriaContenedor.MUNICIPAL)
                .comuna(comuna)
                .kilosMaximos(BigDecimal.valueOf(1000))
                .activo(true)
                .build();
    }

    @Test
    void shouldGetAllContainers() {
        when(contenedorRepository.findAll()).thenReturn(List.of(contenedor));

        List<ContainerAdminDTO> result = adminContenedorService.getAllContainers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Punto Central", result.get(0).getNombrePunto());
    }

    @Test
    void shouldCreateContainerSuccessfully() {
        CreateContainerRequest req = CreateContainerRequest.builder()
                .comunaId(1L)
                .nombrePunto("Nuevo Punto")
                .categoria(CategoriaContenedor.EMPRESA)
                .kilosMaximos(BigDecimal.valueOf(500))
                .build();

        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comuna));
        when(contenedorRepository.save(any(Contenedor.class))).thenAnswer(i -> {
            Contenedor c = i.getArgument(0);
            c.setId(200L);
            return c;
        });

        ContainerAdminDTO dto = adminContenedorService.createContainer(req);

        assertNotNull(dto);
        assertEquals("Nuevo Punto", dto.getNombrePunto());
        verify(outboxEventRepository).save(any());
    }
}
