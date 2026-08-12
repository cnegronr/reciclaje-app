package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.LoginRequest;
import cl.reciclajelitoral.dto.LoginResponse;
import cl.reciclajelitoral.entity.AsignacionInspector;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.AsignacionInspectorRepository;
import cl.reciclajelitoral.repository.UsuarioRepository;
import cl.reciclajelitoral.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionInspectorRepository asignacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Carlos Valenzuela")
                .email("inspector@reciclajelitoral.cl")
                .passwordHash("$2a$10$encodedPassword")
                .rol(Rol.INSPECTOR)
                .build();
    }

    @Test
    @DisplayName("Login Exitoso: Debe autenticar y devolver LoginResponse con JWT token")
    void loginExitoso() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inspector@reciclajelitoral.cl");
        request.setPassword("Password123!");

        Comuna comunaMock = Comuna.builder().nombre("El Quisco").build();
        AsignacionInspector asignacionMock = AsignacionInspector.builder().comuna(comunaMock).build();

        when(usuarioRepository.findByEmail("inspector@reciclajelitoral.cl")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Password123!", "$2a$10$encodedPassword")).thenReturn(true);
        when(tokenProvider.generarToken("inspector@reciclajelitoral.cl")).thenReturn("token.jwt.mock");
        when(asignacionRepository.findByInspectorId(1L)).thenReturn(List.of(asignacionMock));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token.jwt.mock", response.getToken());
        assertEquals("inspector@reciclajelitoral.cl", response.getEmail());
        assertEquals("INSPECTOR", response.getRol());
        assertEquals(1, response.getComunasAsignadas().size());
        assertEquals("El Quisco", response.getComunasAsignadas().get(0));
    }

    @Test
    @DisplayName("Login Fallido: Debe lanzar excepcion si el usuario no existe")
    void loginUsuarioNoEncontrado() {
        LoginRequest request = new LoginRequest();
        request.setEmail("noexiste@reciclajelitoral.cl");
        request.setPassword("Password123!");

        when(usuarioRepository.findByEmail("noexiste@reciclajelitoral.cl")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login Fallido: Debe lanzar excepcion si la contraseña es incorrecta")
    void loginPasswordIncorrecta() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inspector@reciclajelitoral.cl");
        request.setPassword("PasswordErrado");

        when(usuarioRepository.findByEmail("inspector@reciclajelitoral.cl")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("PasswordErrado", "$2a$10$encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}
