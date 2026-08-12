package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.LoginRequest;
import cl.reciclajelitoral.dto.LoginResponse;
import cl.reciclajelitoral.security.JwtAuthenticationFilter;
import cl.reciclajelitoral.security.JwtTokenProvider;
import cl.reciclajelitoral.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/login - Debe responder 200 OK y devolver token JWT con credenciales validas")
    void loginExitoso() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("inspector@reciclajelitoral.cl");
        request.setPassword("Password123!");

        LoginResponse responseMock = LoginResponse.builder()
                .token("jwt.mock.token.value")
                .id(1L)
                .nombre("Carlos Valenzuela")
                .email("inspector@reciclajelitoral.cl")
                .rol("INSPECTOR")
                .comunasAsignadas(List.of("El Quisco", "Algarrobo"))
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(responseMock);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.mock.token.value"))
                .andExpect(jsonPath("$.email").value("inspector@reciclajelitoral.cl"))
                .andExpect(jsonPath("$.rol").value("INSPECTOR"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe responder 400 Bad Request si el cuerpo de la peticion es invalido")
    void loginInvalidoBody() throws Exception {
        LoginRequest requestInvalido = new LoginRequest();
        requestInvalido.setEmail("email-invalido-sin-formato");
        requestInvalido.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
