package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.CreateUserRequest;
import cl.reciclajelitoral.dto.UserAdminDTO;
import cl.reciclajelitoral.entity.AsignacionInspector;
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
    private cl.reciclajelitoral.repository.HistorialAsignacionComunaRepository historialAsignacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Mock
    private SessionInvalidationService sessionInvalidationService;

    @InjectMocks
    private AdminUserService adminUserService;

    private Usuario adminUser;

    @BeforeEach
    void setUp() {
        adminUser = Usuario.builder()
                .id(2L)
                .nombre("Admin Test")
                .email("admin2@test.cl")
                .passwordHash("hashedPass")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
    }

    @Test
    void shouldGetAllUsers() {
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUser));
        when(asignacionRepository.findByInspectorId(2L)).thenReturn(List.of());

        List<UserAdminDTO> result = adminUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Admin Test", result.get(0).getNombre());
    }

    @Test
    void shouldGetActiveUsers() {
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(adminUser));
        when(asignacionRepository.findByInspectorId(2L)).thenReturn(List.of());

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
                .email("admin2@test.cl")
                .password("NewPass123!")
                .rol(Rol.ADMIN)
                .activo(true)
                .comunaIds(List.of(10L))
                .build();

        Comuna comuna = Comuna.builder().id(10L).nombre("El Quisco").build();

        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newEncodedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(comunaRepository.findAllById(List.of(10L))).thenReturn(List.of(comuna));
        when(asignacionRepository.findByInspectorId(2L)).thenReturn(List.of());

        UserAdminDTO dto = adminUserService.updateUser(2L, req);

        assertNotNull(dto);
        assertEquals("Admin Modificado", dto.getNombre());
    }

    @Test
    void shouldThrowWhenUpdateUserDuplicateEmail() {
        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Admin Modificado")
                .email("otro@test.cl")
                .build();

        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));
        when(usuarioRepository.existsByEmail("otro@test.cl")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUser(2L, req));
    }

    @Test
    void shouldDeleteUserSoftly() {
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));

        adminUserService.deleteUser(2L);

        assertFalse(adminUser.getActivo());
        verify(usuarioRepository).save(adminUser);
    }

    @Test
    void shouldHardDeleteUserSuccessfully() {
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any(), any())).thenReturn(0L);

        adminUserService.hardDeleteUser(2L);

        verify(asignacionRepository).deleteByInspectorId(2L);
        verify(usuarioRepository).delete(adminUser);
    }

    @Test
    void shouldThrowWhenHardDeleteUserHasAssociatedInspections() {
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any(), any())).thenReturn(3L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.hardDeleteUser(2L));
        assertTrue(ex.getMessage().contains("cuenta con registros históricos de inspección"));
        verify(usuarioRepository, never()).delete(any());
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

    @Test
    void shouldCreateUserWithReassignedComuna() {
        Usuario inspectorExistente = Usuario.builder().id(5L).nombre("Inspector Anterior").build();
        Comuna comuna = Comuna.builder().id(4L).nombre("San Antonio").build();
        AsignacionInspector asignacionExistente = AsignacionInspector.builder()
                .id(100L)
                .inspector(inspectorExistente)
                .comuna(comuna)
                .build();

        CreateUserRequest req = CreateUserRequest.builder()
                .nombre("Nuevo Inspector")
                .email("nuevo@test.cl")
                .password("Pass123!")
                .rol(Rol.INSPECTOR)
                .comunaIds(List.of(4L))
                .build();

        when(usuarioRepository.existsByEmail("nuevo@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(6L);
            return u;
        });
        when(comunaRepository.findAllById(List.of(4L))).thenReturn(List.of(comuna));
        when(asignacionRepository.findByInspectorId(6L)).thenReturn(List.of());
        when(asignacionRepository.findByComunaId(4L)).thenReturn(List.of(asignacionExistente));

        UserAdminDTO dto = adminUserService.createUser(req);

        assertNotNull(dto);
        assertEquals(6L, asignacionExistente.getInspector().getId());
        verify(asignacionRepository).save(asignacionExistente);
    }

    @Test
    void shouldUpdateUserWithReassignedComuna() {
        Usuario inspectorExistente = Usuario.builder().id(5L).nombre("Inspector Anterior").build();
        Comuna comuna = Comuna.builder().id(4L).nombre("San Antonio").build();
        AsignacionInspector asignacionExistente = AsignacionInspector.builder()
                .id(100L)
                .inspector(inspectorExistente)
                .comuna(comuna)
                .build();

        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Admin Modificado")
                .email("admin2@test.cl")
                .comunaIds(List.of(4L))
                .build();

        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(asignacionRepository.findByInspectorId(2L)).thenReturn(List.of());
        when(comunaRepository.findAllById(List.of(4L))).thenReturn(List.of(comuna));
        when(asignacionRepository.findByComunaId(4L)).thenReturn(List.of(asignacionExistente));

        UserAdminDTO dto = adminUserService.updateUser(2L, req);

        assertNotNull(dto);
        assertEquals(2L, asignacionExistente.getInspector().getId());
        verify(asignacionRepository).save(asignacionExistente);
    }

    @Test
    void shouldThrowWhenUserTriesToDeactivateThemselvesInDeleteUser() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.deleteUser(2L));
            assertEquals("No puedes desactivar tu propio usuario", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenUserTriesToDeactivateThemselvesInUpdateUser() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));

            cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                    .nombre("Admin Modificado")
                    .email("admin2@test.cl")
                    .activo(false)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUser(2L, req));
            assertEquals("No puedes desactivar tu propio usuario", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenUserTriesToChangeTheirOwnRoleInUpdateUser() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));

            cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                    .nombre("Admin Modificado")
                    .email("admin2@test.cl")
                    .rol(Rol.REPORTERIA)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUser(2L, req));
            assertEquals("No puedes cambiar el rol de tu propio usuario", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenUserTriesToHardDeleteThemselves() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.hardDeleteUser(2L));
            assertEquals("No puedes eliminar tu propio usuario", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenReporteriaTriesToCreateAdminUser() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "rep@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_REPORTERIA"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            CreateUserRequest req = CreateUserRequest.builder()
                    .nombre("Admin Fake")
                    .email("fake@test.cl")
                    .password("Pass123!")
                    .rol(Rol.ADMIN)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));
            assertEquals("Los usuarios de reportería solamente pueden crear usuarios con rol INSPECTOR o CHOFER", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenReporteriaTriesToCreateReporteriaUser() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "rep@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_REPORTERIA"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            CreateUserRequest req = CreateUserRequest.builder()
                    .nombre("Rep Fake")
                    .email("fake_rep@test.cl")
                    .password("Pass123!")
                    .rol(Rol.REPORTERIA)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));
            assertEquals("Los usuarios de reportería solamente pueden crear usuarios con rol INSPECTOR o CHOFER", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenReporteriaTriesToUpdateAnotherAdminOrReporteriaUser() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "rep@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_REPORTERIA"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario targetRep = Usuario.builder()
                .id(20L)
                .nombre("Otro Reportero")
                .email("otro_rep@test.cl")
                .rol(Rol.REPORTERIA)
                .activo(true)
                .build();

        try {
            when(usuarioRepository.findById(20L)).thenReturn(java.util.Optional.of(targetRep));

            cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                    .nombre("Modificado")
                    .email("otro_rep@test.cl")
                    .rol(Rol.REPORTERIA)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUser(20L, req));
            assertEquals("Los usuarios de reportería solamente pueden modificar usuarios con rol INSPECTOR, CHOFER o su propio usuario", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenReporteriaTriesToDeleteAdminOrReporteria() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "rep@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_REPORTERIA"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario targetAdmin = Usuario.builder()
                .id(30L)
                .nombre("Admin Objetivo")
                .email("admin_obj@test.cl")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();

        try {
            when(usuarioRepository.findById(30L)).thenReturn(java.util.Optional.of(targetAdmin));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.deleteUser(30L));
            assertEquals("No tiene permisos para desactivar este usuario", ex.getMessage());

            IllegalArgumentException exHard = assertThrows(IllegalArgumentException.class, () -> adminUserService.hardDeleteUser(30L));
            assertEquals("No tiene permisos para eliminar este usuario", exHard.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldRegisterEmailChangeWhenEmailIsUpdated() {
        Usuario inspector = Usuario.builder()
                .id(10L)
                .nombre("Inspector Test")
                .email("inspector_viejo@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .build();

        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Inspector Test Mod")
                .email("inspector_nuevo@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .build();

        when(usuarioRepository.findById(10L)).thenReturn(java.util.Optional.of(inspector));
        when(usuarioRepository.existsByEmail("inspector_nuevo@test.cl")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UserAdminDTO dto = adminUserService.updateUser(10L, req);

        assertNotNull(dto);
        assertEquals("inspector_nuevo@test.cl", dto.getEmail());
        verify(sessionInvalidationService).registerEmailChange(10L, "inspector_viejo@test.cl", "inspector_nuevo@test.cl");
    }

    @Test
    void shouldRegisterPasswordChangeWhenPasswordIsUpdated() {
        Usuario inspector = Usuario.builder()
                .id(10L)
                .nombre("Inspector Test")
                .email("inspector@test.cl")
                .passwordHash("oldHashed")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .build();

        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Inspector Test")
                .email("inspector@test.cl")
                .password("NewSecretPassword123!")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .build();

        when(usuarioRepository.findById(10L)).thenReturn(java.util.Optional.of(inspector));
        when(passwordEncoder.encode("NewSecretPassword123!")).thenReturn("newHashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UserAdminDTO dto = adminUserService.updateUser(10L, req);

        assertNotNull(dto);
        verify(sessionInvalidationService).registerPasswordChange(10L, "inspector@test.cl");
    }

    @Test
    void shouldSetAdministradorGeneralFlagInToDTO() {
        Usuario generalAdmin = Usuario.builder()
                .id(1L)
                .nombre("Administrador General")
                .email("admin@reciclajelitoral.cl")
                .rol(Rol.ADMIN)
                .activo(true)
                .esAdministradorGeneral(true)
                .build();

        when(usuarioRepository.findAll()).thenReturn(List.of(generalAdmin, adminUser));
        when(asignacionRepository.findByInspectorId(1L)).thenReturn(List.of());
        when(asignacionRepository.findByInspectorId(2L)).thenReturn(List.of());

        List<UserAdminDTO> result = adminUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getAdministradorGeneral());
        assertFalse(result.get(1).getAdministradorGeneral());
    }

    @Test
    void shouldThrowWhenNonSelfTriesToUpdateAdministradorGeneral() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario generalAdmin = Usuario.builder()
                .id(1L)
                .nombre("Administrador General")
                .email("admin@reciclajelitoral.cl")
                .rol(Rol.ADMIN)
                .activo(true)
                .esAdministradorGeneral(true)
                .build();

        try {
            when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(generalAdmin));

            cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                    .nombre("Modificando General")
                    .email("admin@reciclajelitoral.cl")
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUser(1L, req));
            assertEquals("No tienes permisos para modificar al Administrador General", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldAllowAdministradorGeneralToUpdateSelf() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin@reciclajelitoral.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario generalAdmin = Usuario.builder()
                .id(1L)
                .nombre("Administrador General")
                .email("admin@reciclajelitoral.cl")
                .rol(Rol.ADMIN)
                .activo(true)
                .esAdministradorGeneral(true)
                .build();

        try {
            when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(generalAdmin));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

            cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                    .nombre("Administrador General Actualizado")
                    .email("admin@reciclajelitoral.cl")
                    .build();

            UserAdminDTO dto = adminUserService.updateUser(1L, req);
            assertNotNull(dto);
            assertEquals("Administrador General Actualizado", dto.getNombre());
            assertTrue(dto.getAdministradorGeneral());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenAnyoneTriesToDeactivateAdministradorGeneral() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario generalAdmin = Usuario.builder()
                .id(1L)
                .nombre("Administrador General")
                .email("admin@reciclajelitoral.cl")
                .rol(Rol.ADMIN)
                .activo(true)
                .esAdministradorGeneral(true)
                .build();

        try {
            when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(generalAdmin));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.deleteUser(1L));
            assertEquals("No tienes permisos para desactivar al Administrador General", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldThrowWhenAnyoneTriesToHardDeleteAdministradorGeneral() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin2@test.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario generalAdmin = Usuario.builder()
                .id(1L)
                .nombre("Administrador General")
                .email("admin@reciclajelitoral.cl")
                .rol(Rol.ADMIN)
                .activo(true)
                .esAdministradorGeneral(true)
                .build();

        try {
            when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(generalAdmin));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminUserService.hardDeleteUser(1L));
            assertEquals("No tienes permisos para eliminar al Administrador General", ex.getMessage());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldAllowAdministradorGeneralToUpdateAnotherAdmin() {
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin@reciclajelitoral.cl", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(adminUser));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

            cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                    .nombre("Admin 2 Modificado por General")
                    .email("admin2@test.cl")
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();

            UserAdminDTO dto = adminUserService.updateUser(2L, req);
            assertNotNull(dto);
            assertEquals("Admin 2 Modificado por General", dto.getNombre());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldDeleteUserAndRemoveComunaAssignments() {
        Usuario inspector = Usuario.builder()
                .id(3L)
                .nombre("Inspector Test")
                .email("inspector@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .esAdministradorGeneral(false)
                .build();

        when(usuarioRepository.findById(3L)).thenReturn(java.util.Optional.of(inspector));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        adminUserService.deleteUser(3L);

        assertFalse(inspector.getActivo());
        verify(asignacionRepository).deleteByInspectorId(3L);
        verify(usuarioRepository).save(inspector);
    }

    @Test
    void shouldUpdateUserWithActivoFalseAndRemoveComunaAssignments() {
        Usuario inspector = Usuario.builder()
                .id(3L)
                .nombre("Inspector Test")
                .email("inspector@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .esAdministradorGeneral(false)
                .build();

        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Inspector Inactivo")
                .email("inspector@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(false)
                .comunaIds(List.of(1L, 2L))
                .build();

        when(usuarioRepository.findById(3L)).thenReturn(java.util.Optional.of(inspector));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(asignacionRepository.findByInspectorId(3L)).thenReturn(List.of());

        UserAdminDTO dto = adminUserService.updateUser(3L, req);

        assertNotNull(dto);
        assertFalse(dto.getActivo());
        verify(asignacionRepository).deleteByInspectorId(3L);
        // Verify comuna assignments were NOT synced because user is inactive
        verify(comunaRepository, never()).findAllById(any());
    }

    @Test
    void shouldReactivateUserAndAssignOnlyExplicitComunas() {
        Usuario inspectorInactivo = Usuario.builder()
                .id(3L)
                .nombre("Inspector Inactivo")
                .email("inspector@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(false)
                .esAdministradorGeneral(false)
                .build();

        Comuna comuna4 = Comuna.builder().id(4L).nombre("San Antonio").build();

        cl.reciclajelitoral.dto.UpdateUserRequest req = cl.reciclajelitoral.dto.UpdateUserRequest.builder()
                .nombre("Inspector Reactivado")
                .email("inspector@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .comunaIds(List.of(4L))
                .build();

        when(usuarioRepository.findById(3L)).thenReturn(java.util.Optional.of(inspectorInactivo));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(asignacionRepository.findByInspectorId(3L)).thenReturn(List.of());
        when(comunaRepository.findAllById(List.of(4L))).thenReturn(List.of(comuna4));
        when(asignacionRepository.findByComunaId(4L)).thenReturn(List.of());

        UserAdminDTO dto = adminUserService.updateUser(3L, req);

        assertNotNull(dto);
        assertTrue(dto.getActivo());
        verify(asignacionRepository, never()).deleteByInspectorId(3L);
        verify(asignacionRepository).save(argThat(a -> a.getInspector().getId().equals(3L) && a.getComuna().getId().equals(4L)));
    }

    @Test
    void shouldRegisterAuditWhenDeactivatingUserWithAssignments() {
        Usuario inspector = Usuario.builder()
                .id(3L)
                .nombre("Inspector Con Comuna")
                .email("inspector3@test.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .esAdministradorGeneral(false)
                .build();
        Comuna comuna = Comuna.builder().id(1L).nombre("El Quisco").build();
        AsignacionInspector asignacion = AsignacionInspector.builder().id(10L).inspector(inspector).comuna(comuna).build();

        when(usuarioRepository.findById(3L)).thenReturn(java.util.Optional.of(inspector));
        when(asignacionRepository.findByInspectorId(3L)).thenReturn(List.of(asignacion));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        adminUserService.deleteUser(3L);

        verify(historialAsignacionRepository).save(argThat(h -> 
            h.getInspectorNombre().equals("Inspector Con Comuna") &&
            h.getComunaNombre().equals("El Quisco") &&
            h.getAccion().equals("DESACTIVACION_USUARIO")
        ));
        verify(asignacionRepository).deleteByInspectorId(3L);
    }
}
