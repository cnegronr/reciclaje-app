package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.ContainerAdminDTO;
import cl.reciclajelitoral.dto.CreateContainerRequest;
import cl.reciclajelitoral.dto.UpdateContainerRequest;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.entity.OutboxEvent;
import cl.reciclajelitoral.repository.ComunaRepository;
import cl.reciclajelitoral.repository.ContenedorRepository;
import cl.reciclajelitoral.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final ComunaRepository comunaRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional(readOnly = true)
    public List<ContainerAdminDTO> getAllContainers() {
        return contenedorRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContainerAdminDTO createContainer(CreateContainerRequest req) {
        Comuna comuna = comunaRepository.findById(req.getComunaId())
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada con ID: " + req.getComunaId()));

        BigDecimal kilosMax = req.getKilosMaximos() != null ? req.getKilosMaximos() :
                (req.getCategoria() != null && req.getCategoria().name().equals("EMPRESA") ? BigDecimal.valueOf(500) : BigDecimal.valueOf(1000));

        String urlMaps = req.getUrlGoogleMaps();
        if (urlMaps == null || urlMaps.isBlank()) {
            if (req.getLatitud() != null && req.getLongitud() != null) {
                urlMaps = "https://maps.google.com/?q=" + req.getLatitud() + "," + req.getLongitud();
            } else {
                urlMaps = "https://maps.google.com/?q=0,0";
            }
        }

        Contenedor contenedor = Contenedor.builder()
                .comuna(comuna)
                .nombrePunto(req.getNombrePunto())
                .ubicacionDescripcion(req.getUbicacionDescripcion())
                .categoria(req.getCategoria())
                .kilosMaximos(kilosMax)
                .urlGoogleMaps(urlMaps)
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .activo(true)
                .build();

        Contenedor saved = contenedorRepository.save(contenedor);

        // Patron Outbox
        outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("CONTENEDOR")
                .aggregateId(saved.getId().toString())
                .eventType("CONTENEDOR_CREADO")
                .payload("{\"id\":" + saved.getId() + ",\"nombrePunto\":\"" + saved.getNombrePunto() + "\"}")
                .status("PENDING")
                .build());

        return toDTO(saved);
    }

    @Transactional
    public ContainerAdminDTO updateContainer(Long id, UpdateContainerRequest req) {
        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado con ID: " + id));

        Comuna comuna = comunaRepository.findById(req.getComunaId())
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada con ID: " + req.getComunaId()));

        contenedor.setComuna(comuna);
        contenedor.setNombrePunto(req.getNombrePunto());
        contenedor.setUbicacionDescripcion(req.getUbicacionDescripcion());
        contenedor.setCategoria(req.getCategoria());
        if (req.getKilosMaximos() != null) {
            contenedor.setKilosMaximos(req.getKilosMaximos());
        }
        if (req.getLatitud() != null) contenedor.setLatitud(req.getLatitud());
        if (req.getLongitud() != null) contenedor.setLongitud(req.getLongitud());
        if (req.getActivo() != null) contenedor.setActivo(req.getActivo());

        if (req.getUrlGoogleMaps() != null && !req.getUrlGoogleMaps().isBlank()) {
            contenedor.setUrlGoogleMaps(req.getUrlGoogleMaps());
        } else if (req.getLatitud() != null && req.getLongitud() != null) {
            contenedor.setUrlGoogleMaps("https://maps.google.com/?q=" + req.getLatitud() + "," + req.getLongitud());
        }

        Contenedor updated = contenedorRepository.save(contenedor);

        // Patron Outbox
        outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("CONTENEDOR")
                .aggregateId(updated.getId().toString())
                .eventType("CONTENEDOR_ACTUALIZADO")
                .payload("{\"id\":" + updated.getId() + ",\"nombrePunto\":\"" + updated.getNombrePunto() + "\"}")
                .status("PENDING")
                .build());

        return toDTO(updated);
    }

    @Transactional
    public void deleteContainer(Long id) {
        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado con ID: " + id));
        contenedor.setActivo(false);
        contenedorRepository.save(contenedor);

        // Patron Outbox
        outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("CONTENEDOR")
                .aggregateId(id.toString())
                .eventType("CONTENEDOR_DESACTIVADO")
                .payload("{\"id\":" + id + "}")
                .status("PENDING")
                .build());
    }

    private ContainerAdminDTO toDTO(Contenedor c) {
        return ContainerAdminDTO.builder()
                .id(c.getId())
                .comunaId(c.getComuna() != null ? c.getComuna().getId() : null)
                .comunaNombre(c.getComuna() != null ? c.getComuna().getNombre() : null)
                .nombrePunto(c.getNombrePunto())
                .ubicacionDescripcion(c.getUbicacionDescripcion())
                .categoria(c.getCategoria())
                .kilosMaximos(c.getKilosMaximos())
                .urlGoogleMaps(c.getUrlGoogleMaps())
                .latitud(c.getLatitud())
                .longitud(c.getLongitud())
                .activo(c.getActivo())
                .build();
    }
}
