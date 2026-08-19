package cl.reciclajelitoral.security;

import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debe cargar UserDetails correctamente para un INSPECTOR")
    void loadUserByUsernameExitosoInspector() {
        Usuario usuarioMock = Usuario.builder()
                .id(1L)
                .email("inspector@reciclajelitoral.cl")
                .passwordHash("hashed_password")
                .rol(Rol.INSPECTOR)
                .build();

        when(usuarioRepository.findByEmail("inspector@reciclajelitoral.cl")).thenReturn(Optional.of(usuarioMock));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("inspector@reciclajelitoral.cl");

        assertNotNull(userDetails);
        assertEquals("inspector@reciclajelitoral.cl", userDetails.getUsername());
        assertEquals("hashed_password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSPECTOR")));
    }

    @Test
    @DisplayName("Debe cargar UserDetails correctamente para un CHOFER")
    void loadUserByUsernameExitosoChofer() {
        Usuario usuarioMock = Usuario.builder()
                .id(2L)
                .email("chofer@reciclajelitoral.cl")
                .passwordHash("hashed_password")
                .rol(Rol.CHOFER)
                .build();

        when(usuarioRepository.findByEmail("chofer@reciclajelitoral.cl")).thenReturn(Optional.of(usuarioMock));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("chofer@reciclajelitoral.cl");

        assertNotNull(userDetails);
        assertEquals("chofer@reciclajelitoral.cl", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CHOFER")));
    }

    @Test
    @DisplayName("Debe validar los valores del Enum Rol incluyendo CHOFER y REPORTERIA")
    void testRolEnum() {
        assertEquals(4, Rol.values().length);
        assertEquals(Rol.CHOFER, Rol.valueOf("CHOFER"));
        assertEquals(Rol.REPORTERIA, Rol.valueOf("REPORTERIA"));
    }

    @Test
    @DisplayName("Debe lanzar UsernameNotFoundException si el email no existe")
    void loadUserByUsernameNoEncontrado() {
        when(usuarioRepository.findByEmail("noexiste@reciclajelitoral.cl")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> 
                customUserDetailsService.loadUserByUsername("noexiste@reciclajelitoral.cl"));
    }
}
