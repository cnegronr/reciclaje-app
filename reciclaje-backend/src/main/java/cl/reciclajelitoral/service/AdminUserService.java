package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.CreateUserRequest;
import cl.reciclajelitoral.dto.UpdateUserRequest;
import cl.reciclajelitoral.dto.UserAdminDTO;
import cl.reciclajelitoral.entity.AsignacionInspector;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.entity.HistorialAsignacionComuna;
import cl.reciclajelitoral.repository.AsignacionInspectorRepository;
import cl.reciclajelitoral.repository.ComunaRepository;
import cl.reciclajelitoral.repository.HistorialAsignacionComunaRepository;
import cl.reciclajelitoral.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final HistorialAsignacionComunaRepository historialAsignacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final SessionInvalidationService sessionInvalidationService;

    public boolean isAdministradorGeneral(Usuario u) {
        if (u == null) return false;
        return Boolean.TRUE.equals(u.getEsAdministradorGeneral());
    }

    public boolean hasAssociatedInspections(Long userId) {
        if (userId == null) return false;
        String sql = "SELECT (" +
                "(SELECT COUNT(*) FROM detalle_inspecciones WHERE creado_por_usuario_id = ? OR actualizado_por_usuario_id = ?) + " +
                "(SELECT COUNT(*) FROM inspecciones_semanales WHERE inspector_id = ? OR inspector_asociado_id = ?) + " +
                "(SELECT COUNT(*) FROM actualizaciones_detalle WHERE usuario_id = ?) + " +
                "(SELECT COUNT(*) FROM fotos_inspeccion WHERE usuario_id = ?)" +
                ")";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, userId, userId, userId, userId, userId);
        return count != null && count > 0;
    }

    private void registrarAuditoriaAsignacion(Usuario usuario, Comuna comuna, String accion, String motivo) {
        if (usuario == null || comuna == null) return;
        HistorialAsignacionComuna historial = HistorialAsignacionComuna.builder()
                .inspector(usuario)
                .inspectorNombre(usuario.getNombre())
                .inspectorEmail(usuario.getEmail())
                .comuna(comuna)
                .comunaNombre(comuna.getNombre())
                .accion(accion)
                .motivo(motivo)
                .ejecutadoPorEmail(getCurrentUserEmail())
                .fechaHora(LocalDateTime.now())
                .build();
        historialAsignacionRepository.save(historial);
    }

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

        if (isAdministradorGeneral(usuario) && !isSelf) {
            throw new IllegalArgumentException("No tienes permisos para modificar al Administrador General");
        }

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

        String oldEmail = usuario.getEmail();
        String newEmail = req.getEmail();
        boolean emailChanged = newEmail != null && !oldEmail.equalsIgnoreCase(newEmail.trim());

        usuario.setNombre(req.getNombre());
        usuario.setEmail(req.getEmail());
        if (req.getRol() != null) {
            usuario.setRol(req.getRol());
        }
        if (req.getActivo() != null) {
            usuario.setActivo(req.getActivo());
            if (!req.getActivo()) {
                List<AsignacionInspector> actuales = asignacionRepository.findByInspectorId(usuario.getId());
                if (actuales != null) {
                    for (AsignacionInspector a : actuales) {
                        if (a.getComuna() != null) {
                            registrarAuditoriaAsignacion(usuario, a.getComuna(), "DESACTIVACION_USUARIO", "Desactivación del usuario en edición de perfil");
                        }
                    }
                }
                asignacionRepository.deleteByInspectorId(usuario.getId());
            }
        }
        boolean passwordChanged = req.getPassword() != null && !req.getPassword().isBlank();
        if (passwordChanged) {
            usuario.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        Usuario updated = usuarioRepository.save(usuario);

        if (emailChanged) {
            sessionInvalidationService.registerEmailChange(updated.getId(), oldEmail, newEmail);
        }

        if (passwordChanged) {
            sessionInvalidationService.registerPasswordChange(updated.getId(), updated.getEmail());
        }

        if (Boolean.TRUE.equals(updated.getActivo()) && req.getComunaIds() != null) {
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
                for (AsignacionInspector a : toRemove) {
                    registrarAuditoriaAsignacion(usuario, a.getComuna(), "DESASIGNACION", "Remoción de comuna en sincronización");
                }
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
                    if (existing.getInspector() != null) {
                        registrarAuditoriaAsignacion(existing.getInspector(), c, "DESASIGNACION", "Reasignada a " + usuario.getNombre());
                    }
                    existing.setInspector(usuario);
                    asignacionRepository.save(existing);
                    registrarAuditoriaAsignacion(usuario, c, "ASIGNACION", "Asignada por reasignación");
                }
            } else {
                AsignacionInspector asignacion = AsignacionInspector.builder()
                        .inspector(usuario)
                        .comuna(c)
                        .build();
                asignacionRepository.save(asignacion);
                registrarAuditoriaAsignacion(usuario, c, "ASIGNACION", "Nueva asignación");
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

        if (isAdministradorGeneral(usuario)) {
            throw new IllegalArgumentException("No tienes permisos para desactivar al Administrador General");
        }

        if (!isCurrentRequestingUserAdmin()) {
            if (usuario.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && usuario.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("No tiene permisos para desactivar este usuario");
            }
        }
        usuario.setActivo(false);
        List<AsignacionInspector> actuales = asignacionRepository.findByInspectorId(usuario.getId());
        if (actuales != null) {
            for (AsignacionInspector a : actuales) {
                if (a.getComuna() != null) {
                    registrarAuditoriaAsignacion(usuario, a.getComuna(), "DESACTIVACION_USUARIO", "Desactivación del usuario en el sistema");
                }
            }
        }
        asignacionRepository.deleteByInspectorId(usuario.getId());
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

        if (isAdministradorGeneral(usuario)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar al Administrador General");
        }

        if (!isCurrentRequestingUserAdmin()) {
            if (usuario.getRol() != cl.reciclajelitoral.entity.Rol.INSPECTOR && usuario.getRol() != cl.reciclajelitoral.entity.Rol.CHOFER) {
                throw new IllegalArgumentException("No tiene permisos para eliminar este usuario");
            }
        }

        // VALIDAR SI EL USUARIO POSEE REGISTROS DE INSPECCIÓN ASOCIADOS
        if (hasAssociatedInspections(id)) {
            throw new IllegalArgumentException("No se puede eliminar definitivamente al usuario '" + usuario.getNombre() + 
                    "' porque cuenta con registros históricos de inspección. Por integridad de datos y auditoría, este usuario solamente puede ser desactivado.");
        }

        // Si no tiene registros de inspección, eliminar asignaciones residuales y eliminar usuario físicamente
        List<AsignacionInspector> actuales = asignacionRepository.findByInspectorId(id);
        if (actuales != null) {
            for (AsignacionInspector a : actuales) {
                if (a.getComuna() != null) {
                    registrarAuditoriaAsignacion(usuario, a.getComuna(), "ELIMINACION_USUARIO", "Eliminación definitiva de usuario sin inspecciones");
                }
            }
        }
        asignacionRepository.deleteByInspectorId(id);
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
                .administradorGeneral(isAdministradorGeneral(u))
                .tieneInspecciones(hasAssociatedInspections(u.getId()))
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
