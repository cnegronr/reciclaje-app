package cl.reciclajelitoral.config;

import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserInitializer initializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(initializer, "adminEmail", "admin@reciclajelitoral.cl");
        ReflectionTestUtils.setField(initializer, "adminName", "Administrador General");
        ReflectionTestUtils.setField(initializer, "adminPassword", "Password123!");
    }

    @Test
    void run_createsAdminUser_whenNotExists() {
        given(usuarioRepository.existsByEmail("admin@reciclajelitoral.cl")).willReturn(false);
        given(passwordEncoder.encode("Password123!")).willReturn("encodedPasswordHash");

        initializer.run(null);

        ArgumentCaptor<Usuario> userCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(userCaptor.capture());

        Usuario savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("admin@reciclajelitoral.cl");
        assertThat(savedUser.getNombre()).isEqualTo("Administrador General");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPasswordHash");
        assertThat(savedUser.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(savedUser.getActivo()).isTrue();
    }

    @Test
    void run_doesNotCreateAdminUser_whenAlreadyExists() {
        given(usuarioRepository.existsByEmail("admin@reciclajelitoral.cl")).willReturn(true);

        initializer.run(null);

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
