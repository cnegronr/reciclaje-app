package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.RegistrarInspeccionRequest;
import cl.reciclajelitoral.entity.InspeccionSemanal;
import cl.reciclajelitoral.security.JwtAuthenticationFilter;
import cl.reciclajelitoral.security.JwtTokenProvider;
import cl.reciclajelitoral.service.InspeccionSemanalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/inspecciones/{id}/registrar - Debe procesar registro de inspeccion exitosamente")
    void registrarInspeccionExitoso() throws Exception {
        RegistrarInspeccionRequest request = new RegistrarInspeccionRequest();
        request.setContenedorId(10L);
        request.setPorcentajeEstimado(BigDecimal.valueOf(80));
        request.setObservaciones("Todo normal");
        request.setFotosAntesUrls(List.of("http://s3.com/foto_antes.jpg"));
        request.setFotosDespuesUrls(List.of("http://s3.com/foto_despues.jpg"));
        request.setEsActualizacion(false);

        InspeccionSemanal inspeccionMock = InspeccionSemanal.builder().id(1L).semanaNumero(32).build();

        when(inspeccionService.registrarOActualizarInspeccion(
                eq(1L), eq(10L), eq(BigDecimal.valueOf(80)), eq("Todo normal"),
                anyList(), anyList(), eq(false)
        )).thenReturn(inspeccionMock);

        mockMvc.perform(post("/api/inspecciones/1/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.semanaNumero").value(32));
    }
}
