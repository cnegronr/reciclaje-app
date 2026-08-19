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

    @Mock
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
    void shouldGetActiveUsers() {
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(adminUser));
        when(asignacionRepository.findByInspectorId(1L)).thenReturn(List.of());

        List<UserAdminDTO> result = adminUserService.getActiveUsers();

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

    @Test
    void shouldCreateReporteriaUserSuccessfully() {
        CreateUserRequest req = CreateUserRequest.builder()
                .nombre("Usuario Reporteria")
                .email("reporteria@test.cl")
                .password("Pass123!")
                .rol(Rol.REPORTERIA)
                .build();

        when(usuarioRepository.existsByEmail("reporteria@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(asignacionRepository.findByInspectorId(3L)).thenReturn(List.of());

        UserAdminDTO dto = adminUserService.createUser(req);

        assertNotNull(dto);
        assertEquals("Usuario Reporteria", dto.getNombre());
        assertEquals(Rol.REPORTERIA, dto.getRol());
    }

    @Test
    void shouldThrowWhenCreateUserEmailExists() {
        CreateUserRequest req = CreateUserRequest.builder()
                .nombre("Nuevo Inspector")
                .email("existente@test.cl")
                .password("Pass123!")
                .rol(Rol.INSPECTOR)
                .build();

        when(usuarioRepository.existsByEmail("existente@test.cl")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Admin Modificado")
                .email("admin@test.cl")
                .password("NewPass123!")
                .rol(Rol.ADMIN)
                .activo(true)
                .comunaIds(List.of(10L))
                .build();

        Comuna comuna = Comuna.builder().id(10L).nombre("El Quisco").build();

        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newEncodedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(comunaRepository.findAllById(List.of(10L))).thenReturn(List.of(comuna));
        when(asignacionRepository.findByInspectorId(1L)).thenReturn(List.of());

        UserAdminDTO dto = adminUserService.updateUser(1L, req);

        assertNotNull(dto);
        assertEquals("Admin Modificado", dto.getNombre());
    }

    @Test
    void shouldThrowWhenUpdateUserDuplicateEmail() {
        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Admin Modificado")
                .email("otro@test.cl")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));
        when(usuarioRepository.existsByEmail("otro@test.cl")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUser(1L, req));
    }

    @Test
    void shouldDeleteUserSoftly() {
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));

        adminUserService.deleteUser(1L);

        assertFalse(adminUser.getActivo());
        verify(usuarioRepository).save(adminUser);
    }

    @Test
    void shouldHardDeleteUserSuccessfully() {
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));

        adminUserService.hardDeleteUser(1L);

        verify(asignacionRepository).deleteByInspectorId(1L);
        verify(jdbcTemplate, times(6)).update(anyString(), eq(1L));
        verify(usuarioRepository).delete(adminUser);
    }

    @Test
    void shouldThrowWhenHardDeleteUserNotFound() {
        when(usuarioRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminUserService.hardDeleteUser(999L));
    }

    @Test
    void shouldThrowWhenNonAdminTriesToAssignAdminRole() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "reporteria", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_REPORTERIA"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            CreateUserRequest req = CreateUserRequest.builder()
                    .nombre("Sub Admin")
                    .email("subadmin@test.cl")
                    .password("Pass123!")
                    .rol(Rol.ADMIN)
                    .build();

            assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
