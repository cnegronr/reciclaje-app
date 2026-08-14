package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.CreateUserRequest;
import cl.reciclajelitoral.dto.UserAdminDTO;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.AsignacionInspectorRepository;
import cl.reciclajelitoral.repository.ComunaRepository;
import cl.reciclajelitoral.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private AsignacionInspectorRepository asignacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private Usuario adminUser;

    @BeforeEach
    void setUp() {
        adminUser = Usuario.builder()
                .id(1L)
                .nombre("Admin Test")
                .email("admin@test.cl")
                .passwordHash("hashedPass")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
    }

    @Test
    void shouldGetAllUsers() {
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUser));
        when(asignacionRepository.findByInspectorId(1L)).thenReturn(List.of());

        List<UserAdminDTO> result = adminUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Admin Test", result.get(0).getNombre());
    }

    @Test
    void shouldCreateUserSuccessfully() {
        CreateUserRequest req = CreateUserRequest.builder()
                .nombre("Nuevo Inspector")
                .email("nuevo@test.cl")
                .password("Pass123!")
                .rol(Rol.INSPECTOR)
                .comunaIds(List.of(10L))
                .build();

        Comuna comuna = Comuna.builder().id(10L).nombre("Algarrobo").build();

        when(usuarioRepository.existsByEmail("nuevo@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(comunaRepository.findAllById(List.of(10L))).thenReturn(List.of(comuna));
        when(asignacionRepository.findByInspectorId(2L)).thenReturn(List.of());

        UserAdminDTO dto = adminUserService.createUser(req);

        assertNotNull(dto);
        assertEquals("Nuevo Inspector", dto.getNombre());
        verify(usuarioRepository).save(any(Usuario.class));
    }
}
