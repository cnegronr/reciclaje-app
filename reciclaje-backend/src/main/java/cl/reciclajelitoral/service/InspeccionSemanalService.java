package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.*;
import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspeccionSemanalService {

    private final InspeccionSemanalRepository inspeccionRepository;
    private final DetalleInspeccionRepository detalleRepository;
    private final ContenedorRepository contenedorRepository;
    private final ComunaRepository comunaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionInspectorRepository asignacionRepository;
    private final S3StorageService s3StorageService;

    public LocalDateTime calcularFechaLimiteSemanal() {
        return calcularFechaLimiteSemanal(LocalDateTime.now());
    }

    public LocalDateTime calcularFechaLimiteSemanal(LocalDateTime ahora) {
        LocalDateTime domingo20 = ahora.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(20).withMinute(0).withSecond(0).withNano(0);

        if (ahora.isAfter(domingo20)) {
            domingo20 = domingo20.plusWeeks(1);
        }
        return domingo20;
    }

    @Transactional
    public InspeccionSemanalDTO obtenerOCrearInspeccionSemanal(Long comunaId, Long inspectorId) {
        LocalDateTime ahora = LocalDateTime.now();
        int semanaNumero = ahora.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int anio = ahora.getYear();

        InspeccionSemanal inspeccion = inspeccionRepository
                .findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(comunaId, inspectorId, semanaNumero, anio)
                .orElseGet(() -> {
                    Comuna comuna = comunaRepository.findById(comunaId)
                            .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + comunaId));
                    Usuario usuarioActivo = usuarioRepository.findById(inspectorId)
                            .orElseThrow(() -> new IllegalArgumentException("Inspector no encontrado: " + inspectorId));

                    // Buscar el Inspector primario asignado a la comuna para vincularlo a las inspecciones del CHOFER
                    Usuario inspectorAsociado = asignacionRepository.findByComunaId(comunaId).stream()
                            .map(AsignacionInspector::getInspector)
                            .filter(u -> u.getRol() == Rol.INSPECTOR)
                            .findFirst()
                            .orElse(usuarioActivo);

                    InspeccionSemanal nuevaInspeccion = InspeccionSemanal.builder()
                            .comuna(comuna)
                            .inspector(usuarioActivo)
                            .inspectorAsociado(inspectorAsociado)
                            .semanaNumero(semanaNumero)
                            .anio(anio)
                            .fechaLimite(calcularFechaLimiteSemanal(ahora))
                            .estado(EstadoInspeccion.EN_PROGRESO)
                            .build();

                    InspeccionSemanal guardada = inspeccionRepository.save(nuevaInspeccion);

                    // Inicializar detalles de inspección para cada contenedor de la comuna
                    List<Contenedor> contenedores = contenedorRepository.findByComunaId(comunaId);
                    for (Contenedor cont : contenedores) {
                        DetalleInspeccion detalle = DetalleInspeccion.builder()
                                .inspeccionSemanal(guardada)
                                .contenedor(cont)
                                .porcentajeEstimado(BigDecimal.ZERO)
                                .kilosCalculados(BigDecimal.ZERO)
                                .visitado(false)
                                .build();
                        detalleRepository.save(detalle);
                    }

                    return inspeccionRepository.findById(guardada.getId()).orElse(guardada);
                });

        return convertirADTO(inspeccion);
    }

    @Transactional
    public InspeccionSemanalDTO registrarOActualizarInspeccion(
            Long inspeccionSemanalId,
            Long contenedorId,
            BigDecimal porcentajeEstimado,
            String observaciones,
            List<String> fotosAntesUrls,
            List<String> fotosDespuesUrls,
            boolean esActualizacion
    ) {
        DetalleInspeccion detalle = detalleRepository.findByInspeccionSemanalIdAndContenedorId(inspeccionSemanalId, contenedorId)
                .orElseGet(() -> {
                    InspeccionSemanal inspeccion = inspeccionRepository.findById(inspeccionSemanalId)
                            .orElseThrow(() -> new IllegalArgumentException("Inspección semanal no encontrada: " + inspeccionSemanalId));
                    Contenedor contenedor = contenedorRepository.findById(contenedorId)
                            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + contenedorId));
                    return DetalleInspeccion.builder()
                            .inspeccionSemanal(inspeccion)
                            .contenedor(contenedor)
                            .visitado(false)
                            .build();
                });

        Contenedor contenedor = detalle.getContenedor();
        BigDecimal kilos = contenedor.calcularKilos(porcentajeEstimado);

        detalle.setPorcentajeEstimado(porcentajeEstimado);
        detalle.setKilosCalculados(kilos);
        detalle.setObservaciones(observaciones);
        detalle.setVisitado(true);

        LocalDateTime ahora = LocalDateTime.now();

        if (!esActualizacion || detalle.getFechaHoraInicial() == null) {
            // INSPECCIÓN INICIAL
            detalle.setFechaHoraInicial(ahora);

            if (fotosAntesUrls != null) {
                for (String photoData : fotosAntesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "inicial_antes");
                    detalle.addFoto(FotoInspeccion.builder()
                            .momento(MomentoFoto.INICIAL_ANTES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .build());
                }
            }
            if (fotosDespuesUrls != null) {
                for (String photoData : fotosDespuesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "inicial_despues");
                    detalle.addFoto(FotoInspeccion.builder()
                            .momento(MomentoFoto.INICIAL_DESPUES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .build());
                }
            }
        } else {
            // EDICIÓN / ACTUALIZACIÓN (PRESERVA FOTOS INICIALES E INSERTA FOTOS DE ACTUALIZACIÓN)
            detalle.setFechaHoraActualizacion(ahora);

            if (fotosAntesUrls != null) {
                for (String photoData : fotosAntesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "act_antes");
                    detalle.addFoto(FotoInspeccion.builder()
                            .momento(MomentoFoto.ACTUALIZACION_ANTES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .build());
                }
            }
            if (fotosDespuesUrls != null) {
                for (String photoData : fotosDespuesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "act_despues");
                    detalle.addFoto(FotoInspeccion.builder()
                            .momento(MomentoFoto.ACTUALIZACION_DESPUES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .build());
                }
            }
        }

        DetalleInspeccion detalleGuardado = detalleRepository.save(detalle);
        InspeccionSemanal inspeccionActualizada = detalleGuardado.getInspeccionSemanal();
        inspeccionActualizada.setEstado(EstadoInspeccion.EN_PROGRESO);
        inspeccionRepository.save(inspeccionActualizada);

        return convertirADTO(inspeccionActualizada);
    }

    @Transactional
    public InspeccionSemanalDTO finalizarRutaSemanal(Long inspeccionSemanalId) {
        InspeccionSemanal inspeccion = inspeccionRepository.findById(inspeccionSemanalId)
                .orElseThrow(() -> new IllegalArgumentException("Inspección no encontrada: " + inspeccionSemanalId));

        inspeccion.setEstado(EstadoInspeccion.FINALIZADO);
        InspeccionSemanal guardada = inspeccionRepository.save(inspeccion);
        return convertirADTO(guardada);
    }

    private InspeccionSemanalDTO convertirADTO(InspeccionSemanal i) {
        List<DetalleInspeccion> detalles = i.getDetalles() != null ? i.getDetalles() : List.of();
        List<DetalleInspeccionDTO> detallesDTO = detalles.stream()
                .map(d -> {
                    List<FotoInspeccion> fotos = d.getFotos() != null ? d.getFotos() : List.of();
                    return DetalleInspeccionDTO.builder()
                            .id(d.getId())
                            .contenedorId(d.getContenedor().getId())
                            .porcentajeEstimado(d.getPorcentajeEstimado())
                            .kilosCalculados(d.getKilosCalculados())
                            .visitado(d.getVisitado())
                            .fechaHoraInicial(d.getFechaHoraInicial())
                            .fechaHoraActualizacion(d.getFechaHoraActualizacion())
                            .observaciones(d.getObservaciones())
                            .fotos(fotos.stream()
                                    .map(f -> FotoInspeccionDTO.builder()
                                            .id(f.getId())
                                            .momento(f.getMomento().name())
                                            .urlFoto(f.getUrlFoto())
                                            .creadoEn(f.getCreadoEn())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());

        Usuario inspectorAsociado = i.getInspectorAsociado() != null ? i.getInspectorAsociado() : i.getInspector();

        return InspeccionSemanalDTO.builder()
                .id(i.getId())
                .comunaId(i.getComuna().getId())
                .inspectorId(i.getInspector().getId())
                .inspectorAsociadoId(inspectorAsociado.getId())
                .inspectorAsociadoNombre(inspectorAsociado.getNombre())
                .rolUsuario(i.getInspector().getRol().name())
                .semanaNumero(i.getSemanaNumero())
                .anio(i.getAnio())
                .fechaLimite(i.getFechaLimite())
                .estado(i.getEstado().name())
                .detalles(detallesDTO)
                .build();
    }
}
