package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.LoginRequest;
import cl.reciclajelitoral.dto.LoginResponse;
import cl.reciclajelitoral.dto.SessionStatusResponse;
import cl.reciclajelitoral.entity.AsignacionInspector;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.AsignacionInspectorRepository;
import cl.reciclajelitoral.repository.UsuarioRepository;
import cl.reciclajelitoral.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AsignacionInspectorRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final SessionInvalidationService sessionInvalidationService;

    public boolean isAdministradorGeneral(Usuario u) {
        if (u == null) return false;
        return Boolean.TRUE.equals(u.getEsAdministradorGeneral());
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("Usuario inactivo en el sistema");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        String token = tokenProvider.generarToken(usuario.getEmail());

        // Limpiar registro de cambio de email y password una vez logueado con nuevas credenciales
        sessionInvalidationService.clearEmailChange(usuario.getId(), usuario.getEmail());
        sessionInvalidationService.clearPasswordChange(usuario.getId(), usuario.getEmail());

        List<String> comunasAsignadas = asignacionRepository.findByInspectorId(usuario.getId())
                .stream()
                .map(a -> a.getComuna().getNombre())
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .comunasAsignadas(comunasAsignadas)
                .administradorGeneral(isAdministradorGeneral(usuario))
                .build();
    }

    public SessionStatusResponse checkSessionStatus(String authHeader, Long userId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return SessionStatusResponse.builder()
                    .active(false)
                    .message("No se proporcionó token de sesión")
                    .build();
        }

        String token = authHeader.substring(7);
        if (!tokenProvider.validarToken(token)) {
            return SessionStatusResponse.builder()
                    .active(false)
                    .message("Token de sesión expirado o inválido")
                    .build();
        }

        String tokenEmail = tokenProvider.obtenerEmailDelToken(token);
        java.util.Date tokenIssuedAtDate = tokenProvider.obtenerFechaEmision(token);
        long tokenIssuedAt = tokenIssuedAtDate != null ? tokenIssuedAtDate.getTime() : 0L;

        // 1. Verificar si la contraseña fue actualizada por admin después de emitir este token
        if (sessionInvalidationService.isPasswordChangedAfter(userId, tokenEmail, tokenIssuedAt)) {
            return SessionStatusResponse.builder()
                    .active(false)
                    .passwordUpdated(true)
                    .message("Tu contraseña ha sido actualizada por un administrador. Debes iniciar sesión con tu nueva contraseña.")
                    .build();
        }

        // 2. Verificar si fue registrado directamente como email actualizado por admin
        if (sessionInvalidationService.isEmailChangedForUser(userId, tokenEmail)) {
            return SessionStatusResponse.builder()
                    .active(false)
                    .emailUpdated(true)
                    .message("Tu correo electrónico ha sido actualizado por un administrador. Debes iniciar sesión con tu nuevo email.")
                    .build();
        }

        // 2. Verificar en base de datos si el userId existe y su email difiere del token
        if (userId != null) {
            Optional<Usuario> userOpt = usuarioRepository.findById(userId);
            if (userOpt.isPresent()) {
                Usuario user = userOpt.get();
                if (!user.getActivo()) {
                    return SessionStatusResponse.builder()
                            .active(false)
                            .deactivated(true)
                            .message("Tu cuenta ha sido desactivada por un administrador.")
                            .build();
                }
                if (!user.getEmail().equalsIgnoreCase(tokenEmail)) {
                    return SessionStatusResponse.builder()
                            .active(false)
                            .emailUpdated(true)
                            .message("Tu correo electrónico ha sido actualizado por un administrador. Debes iniciar sesión con tu nuevo email.")
                            .build();
                }
            } else {
                return SessionStatusResponse.builder()
                        .active(false)
                        .message("Usuario no encontrado en el sistema.")
                        .build();
            }
        } else {
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(tokenEmail);
            if (userOpt.isEmpty()) {
                return SessionStatusResponse.builder()
                        .active(false)
                        .emailUpdated(true)
                        .message("Tu correo electrónico ha sido actualizado por un administrador. Debes iniciar sesión con tu nuevo email.")
                        .build();
            } else if (!userOpt.get().getActivo()) {
                return SessionStatusResponse.builder()
                        .active(false)
                        .deactivated(true)
                        .message("Tu cuenta ha sido desactivada por un administrador.")
                        .build();
            }
        }

        return SessionStatusResponse.builder()
                .active(true)
                .build();
    }
}
