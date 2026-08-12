package cl.reciclajelitoral.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String testSecret = "SecretKeyTestingReciclajeLitoral2026MustBeAtLeast256BitsLongForHMACSHA256!";
    private final long testExpiration = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", testExpiration);
    }

    @Test
    @DisplayName("Debe generar un token JWT valido para un email de usuario")
    void generarToken() {
        String email = "inspector@reciclajelitoral.cl";
        String token = tokenProvider.generarToken(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(tokenProvider.validarToken(token));
    }

    @Test
    @DisplayName("Debe extraer correctamente el email contenido en el token JWT")
    void obtenerEmailDelToken() {
        String email = "inspector@reciclajelitoral.cl";
        String token = tokenProvider.generarToken(email);

        String emailExtraido = tokenProvider.obtenerEmailDelToken(token);

        assertEquals(email, emailExtraido);
    }

    @Test
    @DisplayName("Debe retornar false al validar un token JWT invalido o malformado")
    void validarTokenInvalido() {
        String tokenInvalido = "jwt.invalid.token.signature";

        boolean esValido = tokenProvider.validarToken(tokenInvalido);

        assertFalse(esValido);
    }

    @Test
    @DisplayName("Debe retornar false al validar un token nulo o vacio")
    void validarTokenNullOVacio() {
        assertFalse(tokenProvider.validarToken(null));
        assertFalse(tokenProvider.validarToken(""));
    }
}
