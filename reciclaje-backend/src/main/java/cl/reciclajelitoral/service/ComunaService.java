package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.ComunaDTO;
import cl.reciclajelitoral.dto.ContenedorDTO;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.repository.ComunaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComunaService {

    private final ComunaRepository comunaRepository;
    private final cl.reciclajelitoral.repository.UsuarioRepository usuarioRepository;
    private final cl.reciclajelitoral.repository.AsignacionInspectorRepository asignacionRepository;

    public List<ComunaDTO> listarTodasLasComunas() {
        return comunaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<ComunaDTO> obtenerComunasParaUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return listarTodasLasComunas();
        }

        cl.reciclajelitoral.entity.Usuario user = usuarioRepository.findById(usuarioId).orElse(null);
        if (user == null || user.getRol() == cl.reciclajelitoral.entity.Rol.ADMIN || user.getRol() == cl.reciclajelitoral.entity.Rol.CHOFER) {
            return listarTodasLasComunas();
        }

        List<cl.reciclajelitoral.entity.AsignacionInspector> asignaciones = asignacionRepository.findByInspectorId(usuarioId);
        List<Long> comunaIds = asignaciones.stream()
                .filter(a -> a.getComuna() != null)
                .map(a -> a.getComuna().getId())
                .collect(Collectors.toList());

        if (comunaIds.isEmpty()) {
            return List.of();
        }

        return comunaRepository.findAllById(comunaIds).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public ComunaDTO obtenerPorId(Long id) {
        Comuna comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada con ID: " + id));
        return convertirADTO(comuna);
    }

    private ComunaDTO convertirADTO(Comuna c) {
        List<ContenedorDTO> contenedoresDTO = c.getContenedores().stream()
                .map(cont -> ContenedorDTO.builder()
                        .id(cont.getId())
                        .sector(cont.getSector())
                        .nombrePunto(cont.getNombrePunto())
                        .ubicacionDescripcion(cont.getUbicacionDescripcion())
                        .categoria(cont.getCategoria().name())
                        .kilosMaximos(cont.getKilosMaximos())
                        .urlGoogleMaps(cont.getUrlGoogleMaps())
                        .latitud(cont.getLatitud())
                        .longitud(cont.getLongitud())
                        .build())
                .collect(Collectors.toList());

        cl.reciclajelitoral.entity.Usuario inspectorAsociado = asignacionRepository.findByComunaId(c.getId()).stream()
                .map(cl.reciclajelitoral.entity.AsignacionInspector::getInspector)
                .filter(u -> u != null && u.getRol() == cl.reciclajelitoral.entity.Rol.INSPECTOR)
                .findFirst()
                .orElse(null);

        return ComunaDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .codigoRegion(c.getCodigoRegion())
                .inspectorAsociadoId(inspectorAsociado != null ? inspectorAsociado.getId() : null)
                .inspectorAsociadoNombre(inspectorAsociado != null ? inspectorAsociado.getNombre() : null)
                .contenedores(contenedoresDTO)
                .build();
    }
}
