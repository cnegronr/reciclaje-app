package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.CreateUserRequest;
import cl.reciclajelitoral.dto.UpdateUserRequest;
import cl.reciclajelitoral.dto.UserAdminDTO;
import cl.reciclajelitoral.entity.AsignacionInspector;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.AsignacionInspectorRepository;
import cl.reciclajelitoral.repository.ComunaRepository;
import cl.reciclajelitoral.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final AsignacionInspectorRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getActiveUsers() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAdminDTO createUser(CreateUserRequest req) {
        if (!isCurrentRequestingUserAdmin()) {
            if (req.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && req.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("Los usuarios de reportería solamente pueden crear usuarios con rol INSPECTOR o CHOFER");
            }
        }

        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(req.getNombre())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .rol(req.getRol())
                .activo(true)
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        syncComunaAssignments(saved, req.getComunaIds());

        return toDTO(saved);
    }

    @Transactional
    public UserAdminDTO updateUser(Long id, UpdateUserRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        String currentEmail = getCurrentUserEmail();
        boolean isSelf = currentEmail != null && usuario.getEmail().equalsIgnoreCase(currentEmail);

        if (!isCurrentRequestingUserAdmin()) {
            if (!isSelf && usuario.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && usuario.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("Los usuarios de reportería solamente pueden modificar usuarios con rol INSPECTOR, CHOFER o su propio usuario");
            }
            if (!isSelf && req.getRol() != null && req.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && req.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("Los usuarios de reportería solamente pueden asignar roles INSPECTOR o CHOFER");
            }
        }

        if (!usuario.getEmail().equalsIgnoreCase(req.getEmail()) && usuarioRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado por otro usuario");
        }

        if (isSelf) {
            if (req.getActivo() != null && !req.getActivo()) {
                throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
            }
            if (req.getRol() != null && req.getRol() != usuario.getRol()) {
                throw new IllegalArgumentException("No puedes cambiar el rol de tu propio usuario");
            }
        }

        usuario.setNombre(req.getNombre());
        usuario.setEmail(req.getEmail());
        if (req.getRol() != null) {
            usuario.setRol(req.getRol());
        }
        if (req.getActivo() != null) {
            usuario.setActivo(req.getActivo());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        Usuario updated = usuarioRepository.save(usuario);

        if (req.getComunaIds() != null) {
            syncComunaAssignments(updated, req.getComunaIds());
        }

        return toDTO(updated);
    }

    private void syncComunaAssignments(Usuario usuario, List<Long> comunaIds) {
        if (comunaIds == null) {
            return;
        }

        // 1. Quitar asignaciones previas de este usuario que ya no figuren en la nueva lista
        List<AsignacionInspector> actuales = asignacionRepository.findByInspectorId(usuario.getId());
        if (actuales != null && !actuales.isEmpty()) {
            List<AsignacionInspector> toRemove = actuales.stream()
                    .filter(a -> a.getComuna() != null && !comunaIds.contains(a.getComuna().getId()))
                    .collect(Collectors.toList());
            if (!toRemove.isEmpty()) {
                asignacionRepository.deleteAll(toRemove);
            }
        }

        if (comunaIds.isEmpty()) {
            return;
        }

        // 2. Asignar o reasignar cada comuna solicitada
        List<Comuna> comunas = comunaRepository.findAllById(comunaIds);
        for (Comuna c : comunas) {
            Optional<AsignacionInspector> existingOpt = asignacionRepository.findByComunaId(c.getId()).stream().findFirst();
            if (existingOpt.isPresent()) {
                AsignacionInspector existing = existingOpt.get();
                if (existing.getInspector() == null || !existing.getInspector().getId().equals(usuario.getId())) {
                    existing.setInspector(usuario);
                    asignacionRepository.save(existing);
                }
            } else {
                AsignacionInspector asignacion = AsignacionInspector.builder()
                        .inspector(usuario)
                        .comuna(c)
                        .build();
                asignacionRepository.save(asignacion);
            }
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        String currentEmail = getCurrentUserEmail();
        if (currentEmail != null && usuario.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
        }

        if (!isCurrentRequestingUserAdmin()) {
            if (usuario.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && usuario.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("No tiene permisos para desactivar este usuario");
            }
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void hardDeleteUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        String currentEmail = getCurrentUserEmail();
        if (currentEmail != null && usuario.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }

        if (!isCurrentRequestingUserAdmin()) {
            if (usuario.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && usuario.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("No tiene permisos para eliminar este usuario");
            }
        }

        // 1. Eliminar asignaciones de inspector asociadas
        asignacionRepository.deleteByInspectorId(id);

        // 2. Desvincular claves foráneas en tablas relacionadas desvinculando el FK sin eliminar registros de inspección históricos
        jdbcTemplate.update("UPDATE detalle_inspecciones SET creado_por_usuario_id = NULL WHERE creado_por_usuario_id = ?", id);
        jdbcTemplate.update("UPDATE detalle_inspecciones SET actualizado_por_usuario_id = NULL WHERE actualizado_por_usuario_id = ?", id);
        jdbcTemplate.update("UPDATE inspecciones_semanales SET inspector_id = NULL WHERE inspector_id = ?", id);
        jdbcTemplate.update("UPDATE inspecciones_semanales SET inspector_asociado_id = NULL WHERE inspector_asociado_id = ?", id);
        jdbcTemplate.update("UPDATE actualizaciones_detalle SET usuario_id = NULL WHERE usuario_id = ?", id);
        jdbcTemplate.update("UPDATE fotos_inspeccion SET usuario_id = NULL WHERE usuario_id = ?", id);

        // 3. Eliminar usuario de la base de datos
        usuarioRepository.delete(usuario);
    }

    private UserAdminDTO toDTO(Usuario u) {
        List<AsignacionInspector> asignaciones = asignacionRepository.findByInspectorId(u.getId());
        List<Long> comunaIds = new ArrayList<>();
        List<String> comunaNombres = new ArrayList<>();
        if (asignaciones != null) {
            for (AsignacionInspector a : asignaciones) {
                if (a.getComuna() != null) {
                    comunaIds.add(a.getComuna().getId());
                    comunaNombres.add(a.getComuna().getNombre());
                }
            }
        }

        return UserAdminDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .activo(u.getActivo())
                .comunaIds(comunaIds)
                .comunaNombres(comunaNombres)
                .build();
    }

    private String getCurrentUserEmail() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return auth.getName();
    }

    private boolean isCurrentRequestingUserAdmin() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return true;
        }
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
