package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.ComunaDTO;
import cl.reciclajelitoral.security.JwtAuthenticationFilter;
import cl.reciclajelitoral.security.JwtTokenProvider;
import cl.reciclajelitoral.service.ComunaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComunaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComunaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComunaService comunaService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/comunas - Debe responder 200 OK y devolver lista de comunas")
    void listarComunas() throws Exception {
        ComunaDTO comunaDTO = ComunaDTO.builder()
                .id(1L)
                .nombre("El Quisco")
                .codigoRegion("V")
                .contenedores(List.of())
                .build();

        when(comunaService.listarTodasLasComunas()).thenReturn(List.of(comunaDTO));

        mockMvc.perform(get("/api/comunas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("El Quisco"));
    }

    @Test
    @DisplayName("GET /api/comunas/{id} - Debe responder 200 OK y devolver detalle de comuna por ID")
    void obtenerComunaPorId() throws Exception {
        ComunaDTO comunaDTO = ComunaDTO.builder()
                .id(1L)
                .nombre("Algarrobo")
                .codigoRegion("V")
                .contenedores(List.of())
                .build();

        when(comunaService.obtenerPorId(1L)).thenReturn(comunaDTO);

        mockMvc.perform(get("/api/comunas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Algarrobo"));
    }
}
