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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final AsignacionInspectorRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAdminDTO createUser(CreateUserRequest req) {
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

        if (req.getComunaIds() != null && !req.getComunaIds().isEmpty()) {
            List<Comuna> comunas = comunaRepository.findAllById(req.getComunaIds());
            for (Comuna c : comunas) {
                asignacionRepository.deleteByComunaId(c.getId());
                AsignacionInspector asignacion = AsignacionInspector.builder()
                        .inspector(saved)
                        .comuna(c)
                        .build();
                asignacionRepository.save(asignacion);
            }
        }

        return toDTO(saved);
    }

    @Transactional
    public UserAdminDTO updateUser(Long id, UpdateUserRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (!usuario.getEmail().equalsIgnoreCase(req.getEmail()) && usuarioRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado por otro usuario");
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
            List<AsignacionInspector> actual = asignacionRepository.findByInspectorId(id);
            asignacionRepository.deleteAll(actual);

            List<Comuna> comunas = comunaRepository.findAllById(req.getComunaIds());
            for (Comuna c : comunas) {
                asignacionRepository.deleteByComunaId(c.getId());
                AsignacionInspector asignacion = AsignacionInspector.builder()
                        .inspector(updated)
                        .comuna(c)
                        .build();
                asignacionRepository.save(asignacion);
            }
        }

        return toDTO(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
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
}
