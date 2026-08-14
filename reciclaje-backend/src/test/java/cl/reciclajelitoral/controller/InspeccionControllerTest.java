package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.InspeccionSemanalDTO;
import cl.reciclajelitoral.dto.RegistrarInspeccionRequest;
import cl.reciclajelitoral.security.CustomUserDetailsService;
import cl.reciclajelitoral.security.JwtTokenProvider;
import cl.reciclajelitoral.service.InspeccionSemanalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InspeccionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InspeccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InspeccionSemanalService inspeccionService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private InspeccionSemanalDTO inspeccionDTO;

    @BeforeEach
    void setUp() {
        inspeccionDTO = InspeccionSemanalDTO.builder()
                .id(1L)
                .comunaId(10L)
                .inspectorId(1L)
                .semanaNumero(32)
                .anio(2026)
                .fechaLimite(LocalDateTime.now().plusDays(5))
                .estado("EN_PROGRESO")
                .detalles(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /api/inspecciones/comuna/{comunaId} debe retornar 200 OK")
    void obtenerOCrearInspeccionSemanal() throws Exception {
        when(inspeccionService.obtenerOCrearInspeccionSemanal(10L, 1L)).thenReturn(inspeccionDTO);

        mockMvc.perform(get("/api/inspecciones/comuna/10")
                        .param("inspectorId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comunaId").value(10))
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    @Test
    @DisplayName("POST /api/inspecciones/{id}/registrar debe registrar la inspeccion y fotos")
    void registrarInspeccion() throws Exception {
        RegistrarInspeccionRequest request = new RegistrarInspeccionRequest();
        request.setContenedorId(5L);
        request.setPorcentajeEstimado(BigDecimal.valueOf(80));
        request.setObservaciones("Despejado");
        request.setFotosAntesUrls(List.of("data:image/jpeg;base64,/9j/4AAQSkZJRg=="));
        request.setFotosDespuesUrls(List.of("data:image/jpeg;base64,/9j/4AAQSkZJRg=="));
        request.setEsActualizacion(false);

        when(inspeccionService.registrarOActualizarInspeccion(
                eq(1L), eq(5L), eq(BigDecimal.valueOf(80)), eq("Despejado"),
                any(), any(), eq(false), any()
        )).thenReturn(inspeccionDTO);

        mockMvc.perform(post("/api/inspecciones/1/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/inspecciones/{id}/finalizar debe finalizar la ruta semanal")
    void finalizarRutaSemanal() throws Exception {
        inspeccionDTO.setEstado("FINALIZADO");
        when(inspeccionService.finalizarRutaSemanal(1L)).thenReturn(inspeccionDTO);

        mockMvc.perform(post("/api/inspecciones/1/finalizar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }
}
