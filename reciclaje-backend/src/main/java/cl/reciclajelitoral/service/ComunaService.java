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

    public List<ComunaDTO> listarTodasLasComunas() {
        return comunaRepository.findAll().stream()
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
                        .nombrePunto(cont.getNombrePunto())
                        .ubicacionDescripcion(cont.getUbicacionDescripcion())
                        .categoria(cont.getCategoria().name())
                        .kilosMaximos(cont.getKilosMaximos())
                        .urlGoogleMaps(cont.getUrlGoogleMaps())
                        .latitud(cont.getLatitud())
                        .longitud(cont.getLongitud())
                        .build())
                .collect(Collectors.toList());

        return ComunaDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .codigoRegion(c.getCodigoRegion())
                .contenedores(contenedoresDTO)
                .build();
    }
}
