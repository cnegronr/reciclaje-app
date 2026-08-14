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
import java.util.Optional;
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

        Usuario usuarioActivo = usuarioRepository.findById(inspectorId).orElse(null);
        TipoRuta tipoRuta = (usuarioActivo != null && usuarioActivo.getRol() == Rol.CHOFER) ? TipoRuta.CHOFER : TipoRuta.INSPECTOR;

        InspeccionSemanal inspeccion = (tipoRuta == TipoRuta.CHOFER)
                ? inspeccionRepository.findByComunaIdAndTipoRutaAndSemanaNumeroAndAnio(comunaId, TipoRuta.CHOFER, semanaNumero, anio).orElse(null)
                : inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(comunaId, inspectorId, semanaNumero, anio).orElse(null);

        if (inspeccion == null) {
            Comuna comuna = comunaRepository.findById(comunaId)
                    .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + comunaId));

            if (usuarioActivo == null) {
                throw new IllegalArgumentException("Inspector no encontrado: " + inspectorId);
            }

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
                    .tipoRuta(tipoRuta)
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

            inspeccion = guardada;
        }

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
        return registrarOActualizarInspeccion(inspeccionSemanalId, contenedorId, porcentajeEstimado, observaciones, fotosAntesUrls, fotosDespuesUrls, esActualizacion, null);
    }

    @Transactional
    public InspeccionSemanalDTO registrarOActualizarInspeccion(
            Long inspeccionSemanalId,
            Long contenedorId,
            BigDecimal porcentajeEstimado,
            String observaciones,
            List<String> fotosAntesUrls,
            List<String> fotosDespuesUrls,
            boolean esActualizacion,
            Long usuarioId
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

        Usuario actor = (usuarioId != null)
                ? usuarioRepository.findById(usuarioId).orElse(detalle.getInspeccionSemanal().getInspector())
                : detalle.getInspeccionSemanal().getInspector();

        Contenedor contenedor = detalle.getContenedor();
        BigDecimal kilos = contenedor.calcularKilos(porcentajeEstimado);

        detalle.setPorcentajeEstimado(porcentajeEstimado);
        detalle.setKilosCalculados(kilos);
        detalle.setObservaciones(observaciones);
        detalle.setVisitado(true);
        detalle.setActualizadoPorUsuario(actor);

        LocalDateTime ahora = LocalDateTime.now();

        if (!esActualizacion || detalle.getFechaHoraInicial() == null) {
            // INSPECCIÓN INICIAL
            detalle.setFechaHoraInicial(ahora);
            detalle.setCreadoPorUsuario(actor);

            if (fotosAntesUrls != null) {
                for (String photoData : fotosAntesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "inicial_antes");
                    detalle.addFoto(FotoInspeccion.builder()
                            .momento(MomentoFoto.INICIAL_ANTES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .usuario(actor)
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
                            .usuario(actor)
                            .build());
                }
            }
        } else {
            // EDICIÓN / ACTUALIZACIÓN (PRESERVA INICIAL Y REGISTRA ENTRADA COMPLETA EN ACTUALIZACIONES_DETALLE)
            detalle.setFechaHoraActualizacion(ahora);

            ActualizacionDetalle actDetalle = ActualizacionDetalle.builder()
                    .detalleInspeccion(detalle)
                    .usuario(actor)
                    .porcentajeEstimado(porcentajeEstimado)
                    .kilosCalculados(kilos)
                    .observaciones(observaciones)
                    .fechaHora(ahora)
                    .build();

            if (fotosAntesUrls != null) {
                for (String photoData : fotosAntesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "act_antes");
                    FotoInspeccion foto = FotoInspeccion.builder()
                            .momento(MomentoFoto.ACTUALIZACION_ANTES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .usuario(actor)
                            .build();
                    actDetalle.addFoto(foto);
                }
            }
            if (fotosDespuesUrls != null) {
                for (String photoData : fotosDespuesUrls) {
                    String urlS3 = s3StorageService.subirFotoAS3(photoData, "act_despues");
                    FotoInspeccion foto = FotoInspeccion.builder()
                            .momento(MomentoFoto.ACTUALIZACION_DESPUES)
                            .urlFoto(urlS3)
                            .creadoEn(ahora)
                            .usuario(actor)
                            .build();
                    actDetalle.addFoto(foto);
                }
            }

            detalle.addActualizacion(actDetalle);
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
                    List<FotoInspeccion> fotos = Optional.ofNullable(d.getFotos()).orElseGet(List::of);
                    List<ActualizacionDetalle> actualizaciones = Optional.ofNullable(d.getActualizaciones()).orElseGet(List::of);
                    Usuario creador = d.getCreadoPorUsuario();
                    Usuario actualizador = d.getActualizadoPorUsuario();

                    List<ActualizacionDetalleDTO> actDTOList = actualizaciones.stream()
                            .map(act -> {
                                Usuario uAct = act.getUsuario();
                                List<FotoInspeccion> fotosAct = Optional.ofNullable(act.getFotos()).orElseGet(List::of);
                                return ActualizacionDetalleDTO.builder()
                                        .id(act.getId())
                                        .usuarioId(Optional.ofNullable(uAct).map(Usuario::getId).orElse(null))
                                        .usuarioNombre(Optional.ofNullable(uAct).map(Usuario::getNombre).orElse(null))
                                        .porcentajeEstimado(act.getPorcentajeEstimado())
                                        .kilosCalculados(act.getKilosCalculados())
                                        .observaciones(act.getObservaciones())
                                        .fechaHora(act.getFechaHora())
                                        .fotos(fotosAct.stream()
                                                .map(f -> FotoInspeccionDTO.builder()
                                                        .id(f.getId())
                                                        .momento(f.getMomento().name())
                                                        .urlFoto(f.getUrlFoto())
                                                        .creadoEn(f.getCreadoEn())
                                                        .usuarioId(Optional.ofNullable(f.getUsuario()).map(Usuario::getId).orElse(null))
                                                        .usuarioNombre(Optional.ofNullable(f.getUsuario()).map(Usuario::getNombre).orElse(null))
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return DetalleInspeccionDTO.builder()
                            .id(d.getId())
                            .contenedorId(d.getContenedor().getId())
                            .creadoPorUsuarioId(Optional.ofNullable(creador).map(Usuario::getId).orElse(null))
                            .creadoPorUsuarioNombre(Optional.ofNullable(creador).map(Usuario::getNombre).orElse(null))
                            .actualizadoPorUsuarioId(Optional.ofNullable(actualizador).map(Usuario::getId).orElse(null))
                            .actualizadoPorUsuarioNombre(Optional.ofNullable(actualizador).map(Usuario::getNombre).orElse(null))
                            .porcentajeEstimado(d.getPorcentajeEstimado())
                            .kilosCalculados(d.getKilosCalculados())
                            .visitado(d.getVisitado())
                            .fechaHoraInicial(d.getFechaHoraInicial())
                            .fechaHoraActualizacion(d.getFechaHoraActualizacion())
                            .observaciones(d.getObservaciones())
                            .actualizacionesHistorial(actDTOList)
                            .fotos(fotos.stream()
                                    .map(f -> {
                                        Usuario uFoto = f.getUsuario();
                                        return FotoInspeccionDTO.builder()
                                                .id(f.getId())
                                                .momento(f.getMomento().name())
                                                .urlFoto(f.getUrlFoto())
                                                .creadoEn(f.getCreadoEn())
                                                .usuarioId(Optional.ofNullable(uFoto).map(Usuario::getId).orElse(null))
                                                .usuarioNombre(Optional.ofNullable(uFoto).map(Usuario::getNombre).orElse(null))
                                                .build();
                                    })
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
