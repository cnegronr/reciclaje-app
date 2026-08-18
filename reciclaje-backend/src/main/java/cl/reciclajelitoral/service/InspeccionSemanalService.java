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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@lombok.extern.slf4j.Slf4j
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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public static final java.time.ZoneId CHILE_ZONE = java.time.ZoneId.of("America/Santiago");

    public LocalDateTime calcularFechaLimiteSemanal() {
        return cl.reciclajelitoral.util.WeekDateUtils.calcularFechaLimiteSemanal(null);
    }

    public LocalDateTime calcularFechaLimiteSemanal(LocalDateTime ahora) {
        return cl.reciclajelitoral.util.WeekDateUtils.calcularFechaLimiteSemanal(ahora);
    }

    @Transactional
    public InspeccionSemanalDTO obtenerOCrearInspeccionSemanal(Long comunaId, Long inspectorId) {
        int semanaNumero = cl.reciclajelitoral.util.WeekDateUtils.getCurrentWeekNumber();
        int anio = cl.reciclajelitoral.util.WeekDateUtils.getCurrentYear();

        Usuario usuarioActivo = usuarioRepository.findById(inspectorId).orElse(null);
        TipoRuta tipoRuta = (usuarioActivo != null && usuarioActivo.getRol() == Rol.CHOFER) ? TipoRuta.CHOFER : TipoRuta.INSPECTOR;

        // Buscar el Inspector primario asignado a la comuna
        Usuario inspectorAsociado = asignacionRepository.findByComunaId(comunaId).stream()
                .map(AsignacionInspector::getInspector)
                .filter(u -> u != null && u.getRol() == Rol.INSPECTOR)
                .findFirst()
                .orElse(usuarioActivo);

        InspeccionSemanal inspeccion = (tipoRuta == TipoRuta.CHOFER)
                ? inspeccionRepository.findByComunaIdAndTipoRutaAndSemanaNumeroAndAnio(comunaId, TipoRuta.CHOFER, semanaNumero, anio).orElse(null)
                : inspeccionRepository.findByComunaIdAndTipoRutaAndSemanaNumeroAndAnio(comunaId, TipoRuta.INSPECTOR, semanaNumero, anio)
                .orElseGet(() -> (inspectorAsociado != null)
                        ? inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(comunaId, inspectorAsociado.getId(), semanaNumero, anio).orElse(null)
                        : inspeccionRepository.findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(comunaId, inspectorId, semanaNumero, anio).orElse(null));

        if (inspeccion == null) {
            Comuna comuna = comunaRepository.findById(comunaId)
                    .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + comunaId));

            if (usuarioActivo == null) {
                throw new IllegalArgumentException("Inspector no encontrado: " + inspectorId);
            }

            Usuario inspectorPrincipal = (tipoRuta == TipoRuta.CHOFER) ? usuarioActivo : ((inspectorAsociado != null) ? inspectorAsociado : usuarioActivo);

            InspeccionSemanal nuevaInspeccion = InspeccionSemanal.builder()
                    .comuna(comuna)
                    .inspector(inspectorPrincipal)
                    .inspectorAsociado(inspectorAsociado)
                    .tipoRuta(tipoRuta)
                    .semanaNumero(semanaNumero)
                    .anio(anio)
                    .fechaLimite(calcularFechaLimiteSemanal())
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

        boolean tieneFotosNuevas = tieneFotosLista(fotosAntesUrls) || tieneFotosLista(fotosDespuesUrls);
        boolean tieneObservacionesInput = observaciones != null && !observaciones.trim().isEmpty();

        String observacionesFinales;

        if (!esActualizacion || detalle.getFechaHoraInicial() == null) {
            observacionesFinales = tieneObservacionesInput ? observaciones.trim() : "Registro inicial";
        } else {
            if (tieneFotosNuevas) {
                if (!tieneObservacionesInput || "Actualización de porcentaje".equals(observaciones.trim()) || "Registro inicial".equals(observaciones.trim())) {
                    observacionesFinales = "Actualización de fotos";
                } else {
                    observacionesFinales = observaciones.trim();
                }
            } else {
                if (!tieneObservacionesInput || "Actualización de fotos".equals(observaciones.trim())) {
                    observacionesFinales = "Actualización de porcentaje";
                } else {
                    String obsTrim = observaciones.trim();
                    if (obsTrim.startsWith("Comentario actualizado") || obsTrim.startsWith("Actualización de porcentaje")) {
                        observacionesFinales = obsTrim;
                    } else {
                        observacionesFinales = "Comentario actualizado: " + obsTrim;
                    }
                }
            }
        }

        if (detalle.getPorcentajeEstimadoInicial() == null) {
            detalle.setPorcentajeEstimadoInicial(Optional.ofNullable(detalle.getPorcentajeEstimado()).orElse(porcentajeEstimado));
            detalle.setKilosCalculadosInicial(Optional.ofNullable(detalle.getKilosCalculados()).orElse(kilos));
            detalle.setObservacionesInicial(Optional.ofNullable(detalle.getObservaciones()).orElse(observacionesFinales));
        }

        detalle.setPorcentajeEstimado(porcentajeEstimado);
        detalle.setKilosCalculados(kilos);
        detalle.setObservaciones(observacionesFinales);
        detalle.setVisitado(true);
        detalle.setActualizadoPorUsuario(actor);

        LocalDateTime ahora = LocalDateTime.now();

        if (!esActualizacion || detalle.getFechaHoraInicial() == null) {
            // INSPECCIÓN INICIAL
            detalle.setFechaHoraInicial(ahora);
            detalle.setCreadoPorUsuario(actor);
            detalle.setPorcentajeEstimadoInicial(porcentajeEstimado);
            detalle.setKilosCalculadosInicial(kilos);
            detalle.setObservacionesInicial(observacionesFinales);

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
                    .observaciones(observacionesFinales)
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
                            .creadoPorRol(Optional.ofNullable(creador).map(u -> u.getRol().name()).orElse(null))
                            .actualizadoPorUsuarioId(Optional.ofNullable(actualizador).map(Usuario::getId).orElse(null))
                            .actualizadoPorUsuarioNombre(Optional.ofNullable(actualizador).map(Usuario::getNombre).orElse(null))
                            .actualizadoPorRol(Optional.ofNullable(actualizador).map(u -> u.getRol().name()).orElse(null))
                            .porcentajeEstimado(d.getPorcentajeEstimado())
                            .kilosCalculados(d.getKilosCalculados())
                            .porcentajeEstimadoInicial(Optional.ofNullable(d.getPorcentajeEstimadoInicial()).orElse(d.getPorcentajeEstimado()))
                            .kilosCalculadosInicial(Optional.ofNullable(d.getKilosCalculadosInicial()).orElse(d.getKilosCalculados()))
                            .visitado(d.getVisitado())
                            .fechaHoraInicial(d.getFechaHoraInicial())
                            .fechaHoraActualizacion(d.getFechaHoraActualizacion())
                            .observaciones(d.getObservaciones())
                            .observacionesInicial(Optional.ofNullable(d.getObservacionesInicial()).orElse(d.getObservaciones()))
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
                .tieneRespaldoLimpieza(Boolean.TRUE.equals(i.getTieneRespaldoLimpieza()))
                .detalles(detallesDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public TraspasoPreviewDTO obtenerPreviewTraspasoVisitadas(Long comunaId, Long inspectorId) {
        Usuario ejecutor = usuarioRepository.findById(inspectorId).orElse(null);
        if (ejecutor != null && ejecutor.getRol() != Rol.ADMIN) {
            return TraspasoPreviewDTO.builder()
                    .semanaOrigen(-1)
                    .anioOrigen(-1)
                    .semanaDestino(cl.reciclajelitoral.util.WeekDateUtils.getCurrentWeekNumber())
                    .anioDestino(cl.reciclajelitoral.util.WeekDateUtils.getCurrentYear())
                    .permitidoTraspaso(false)
                    .mensajeValidacion("Solamente los usuarios administradores pueden traspasar visitas previas.")
                    .totalVisitadasOrigen(0)
                    .totalVisitadasDestinoActual(0)
                    .detallesVisitados(List.of())
                    .build();
        }
        int semanaActual = cl.reciclajelitoral.util.WeekDateUtils.getCurrentWeekNumber();
        int anioActual = cl.reciclajelitoral.util.WeekDateUtils.getCurrentYear();

        int semanaOrigen = semanaActual > 1 ? semanaActual - 1 : 52;
        int anioOrigen = semanaActual > 1 ? anioActual : anioActual - 1;

        InspeccionSemanalDTO destDTO = obtenerOCrearInspeccionSemanal(comunaId, inspectorId);
        InspeccionSemanal rutaDestino = inspeccionRepository.findById(destDTO.getId()).orElse(null);

        long totalVisitadasDestinoActual = (rutaDestino != null)
                ? rutaDestino.getDetalles().stream().filter(d -> Boolean.TRUE.equals(d.getVisitado())).count()
                : 0L;

        boolean permitido = totalVisitadasDestinoActual == 0;
        String mensajeValidacion = permitido ?
                "Traspaso permitido. La semana actual no tiene inspecciones ingresadas." :
                "La semana actual ya contiene " + totalVisitadasDestinoActual + " inspección(es) ingresada(s). Debe limpiar la semana actual primero antes de traspasar desde la semana previa.";

        // Buscar inspecciones origen de la semana previa considerando fecha efectiva
        List<DetalleInspeccion> todasVisitadasComuna = detalleRepository.findVisitadasByComunaId(comunaId);

        int targetSemana = semanaOrigen;
        int targetAnio = anioOrigen;

        List<DetalleInspeccion> visitadasOrigen = todasVisitadasComuna.stream()
                .filter(d -> getEffectiveWeekNumber(d) == targetSemana && getEffectiveYear(d) == targetAnio)
                .toList();

        if (visitadasOrigen.isEmpty()) {
            visitadasOrigen = todasVisitadasComuna.stream()
                    .filter(d -> d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getSemanaNumero() == targetSemana && d.getInspeccionSemanal().getAnio() == targetAnio)
                    .toList();
        }

        if (visitadasOrigen.isEmpty()) {
            // Fallback: buscar la semana previa más reciente que SÍ contenga registros
            List<DetalleInspeccion> previasComuna = todasVisitadasComuna.stream()
                    .filter(d -> {
                        int w = getEffectiveWeekNumber(d);
                        int y = getEffectiveYear(d);
                        return y < anioActual || (y == anioActual && w < semanaActual);
                    })
                    .sorted((a, b) -> {
                        int cmpY = Integer.compare(getEffectiveYear(b), getEffectiveYear(a));
                        if (cmpY != 0) return cmpY;
                        return Integer.compare(getEffectiveWeekNumber(b), getEffectiveWeekNumber(a));
                    })
                    .toList();

            if (!previasComuna.isEmpty()) {
                DetalleInspeccion masReciente = previasComuna.get(0);
                semanaOrigen = getEffectiveWeekNumber(masReciente);
                anioOrigen = getEffectiveYear(masReciente);
                int sO = semanaOrigen;
                int aO = anioOrigen;
                visitadasOrigen = previasComuna.stream()
                        .filter(d -> getEffectiveWeekNumber(d) == sO && getEffectiveYear(d) == aO)
                        .toList();
            }
        }

        Map<Long, DetalleInspeccion> mapByContenedor = new HashMap<>();
        for (DetalleInspeccion d : visitadasOrigen) {
            if (d.getContenedor() != null && d.getContenedor().getId() != null) {
                mapByContenedor.put(d.getContenedor().getId(), d);
            }
        }
        List<DetalleInspeccion> visitadasUnicas = new ArrayList<>(mapByContenedor.values());

        List<TraspasoPreviewDTO.DetalleItemPreview> itemsPreview = visitadasUnicas.stream()
                .map(d -> TraspasoPreviewDTO.DetalleItemPreview.builder()
                        .detalleId(d.getId())
                        .contenedorId(d.getContenedor() != null ? d.getContenedor().getId() : null)
                        .contenedorNombre(d.getContenedor() != null ? d.getContenedor().getNombrePunto() : "Contenedor")
                        .comunaNombre(d.getContenedor() != null && d.getContenedor().getComuna() != null ? d.getContenedor().getComuna().getNombre() : "N/A")
                        .categoria(d.getContenedor() != null && d.getContenedor().getCategoria() != null ? d.getContenedor().getCategoria().name() : "N/A")
                        .porcentajeEstimado(d.getPorcentajeEstimado())
                        .kilosCalculados(d.getKilosCalculados())
                        .inspectorNombre(d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getNombre() : (d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getNombre() : "Sistema"))
                        .fechaHoraInicial(d.getFechaHoraInicial())
                        .observaciones(d.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        return TraspasoPreviewDTO.builder()
                .semanaOrigen(semanaOrigen)
                .anioOrigen(anioOrigen)
                .semanaDestino(semanaActual)
                .anioDestino(anioActual)
                .permitidoTraspaso(permitido)
                .mensajeValidacion(mensajeValidacion)
                .totalVisitadasOrigen(itemsPreview.size())
                .totalVisitadasDestinoActual(totalVisitadasDestinoActual)
                .detallesVisitados(itemsPreview)
                .build();
    }

    @Transactional
    public InspeccionSemanalDTO aplicarTraspasoVisitadas(Long comunaId, Long inspectorId) {
        TraspasoPreviewDTO preview = obtenerPreviewTraspasoVisitadas(comunaId, inspectorId);
        if (!preview.isPermitidoTraspaso()) {
            throw new IllegalStateException(preview.getMensajeValidacion());
        }

        InspeccionSemanalDTO rutaDestinoDTO = obtenerOCrearInspeccionSemanal(comunaId, inspectorId);
        InspeccionSemanal rutaDestino = inspeccionRepository.findById(rutaDestinoDTO.getId()).orElseThrow();

        // Generar respaldo de seguridad del estado previo al traspaso
        try {
            List<SnapshotItem> itemsSnapshot = rutaDestino.getDetalles().stream()
                    .map(d -> SnapshotItem.builder()
                            .contenedorId(d.getContenedor() != null ? d.getContenedor().getId() : null)
                            .visitado(d.getVisitado())
                            .porcentajeEstimado(d.getPorcentajeEstimado())
                            .kilosCalculados(d.getKilosCalculados())
                            .porcentajeEstimadoInicial(d.getPorcentajeEstimadoInicial())
                            .kilosCalculadosInicial(d.getKilosCalculadosInicial())
                            .observaciones(d.getObservaciones())
                            .observacionesInicial(d.getObservacionesInicial())
                            .fechaHoraInicial(d.getFechaHoraInicial())
                            .fechaHoraActualizacion(d.getFechaHoraActualizacion())
                            .creadoPorUsuarioId(d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getId() : null)
                            .actualizadoPorUsuarioId(d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getId() : null)
                            .fotos(d.getFotos() != null ? d.getFotos().stream()
                                    .map(f -> FotoSnapshotItem.builder()
                                            .momento(f.getMomento() != null ? f.getMomento().name() : null)
                                            .urlFoto(f.getUrlFoto())
                                            .creadoEn(f.getCreadoEn())
                                            .usuarioId(f.getUsuario() != null ? f.getUsuario().getId() : null)
                                            .build())
                                    .collect(Collectors.toList()) : List.of())
                            .build())
                    .collect(Collectors.toList());

            String jsonSnapshot = objectMapper.writeValueAsString(itemsSnapshot);
            rutaDestino.setRespaldoEstadoPrevio(jsonSnapshot);
            rutaDestino.setTieneRespaldoLimpieza(true);
        } catch (Exception e) {
            log.warn("Aviso: no se pudo guardar el respaldo pre-traspaso: {}", e.getMessage());
        }

        List<DetalleInspeccion> todasVisitadasComuna = detalleRepository.findVisitadasByComunaId(comunaId);
        int targetSemana = preview.getSemanaOrigen();
        int targetAnio = preview.getAnioOrigen();

        List<DetalleInspeccion> visitadasOrigen = todasVisitadasComuna.stream()
                .filter(d -> (getEffectiveWeekNumber(d) == targetSemana && getEffectiveYear(d) == targetAnio) ||
                             (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getSemanaNumero() == targetSemana && d.getInspeccionSemanal().getAnio() == targetAnio))
                .toList();

        Map<Long, DetalleInspeccion> mapByContenedor = new HashMap<>();
        for (DetalleInspeccion d : visitadasOrigen) {
            if (d.getContenedor() != null && d.getContenedor().getId() != null) {
                mapByContenedor.put(d.getContenedor().getId(), d);
            }
        }

        for (DetalleInspeccion dOrig : mapByContenedor.values()) {
            Optional<DetalleInspeccion> dDestOpt = rutaDestino.getDetalles().stream()
                    .filter(d -> d.getContenedor() != null && d.getContenedor().getId().equals(dOrig.getContenedor().getId()))
                    .findFirst();

            if (dDestOpt.isPresent()) {
                DetalleInspeccion dDest = dDestOpt.get();
                LocalDateTime ahora = LocalDateTime.now();
                Usuario inspectorDestino = rutaDestino.getInspector();
                dDest.setVisitado(true);
                dDest.setPorcentajeEstimado(dOrig.getPorcentajeEstimado());
                dDest.setKilosCalculados(dOrig.getKilosCalculados());
                dDest.setPorcentajeEstimadoInicial(dOrig.getPorcentajeEstimadoInicial());
                dDest.setKilosCalculadosInicial(dOrig.getKilosCalculadosInicial());
                dDest.setObservaciones(dOrig.getObservaciones());
                dDest.setObservacionesInicial(dOrig.getObservacionesInicial());
                dDest.setFechaHoraInicial(ahora);
                dDest.setFechaHoraActualizacion(ahora);
                dDest.setCreadoPorUsuario(inspectorDestino != null ? inspectorDestino : dOrig.getCreadoPorUsuario());
                dDest.setActualizadoPorUsuario(inspectorDestino != null ? inspectorDestino : dOrig.getActualizadoPorUsuario());

                if (dOrig.getFotos() != null) {
                    for (FotoInspeccion fOrig : dOrig.getFotos()) {
                        boolean fotoExiste = dDest.getFotos().stream().anyMatch(f -> f.getUrlFoto() != null && f.getUrlFoto().equals(fOrig.getUrlFoto()));
                        if (!fotoExiste) {
                            FotoInspeccion nuevaFoto = FotoInspeccion.builder()
                                    .detalleInspeccion(dDest)
                                    .momento(fOrig.getMomento())
                                    .urlFoto(fOrig.getUrlFoto())
                                    .creadoEn(fOrig.getCreadoEn())
                                    .usuario(fOrig.getUsuario())
                                    .build();
                            dDest.getFotos().add(nuevaFoto);
                        }
                    }
                }
            }
        }

        InspeccionSemanal guardada = inspeccionRepository.save(rutaDestino);
        return convertirADTO(guardada);
    }

    public int getEffectiveWeekNumber(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getSemanaNumero() : -1;
        }
        return cl.reciclajelitoral.util.WeekDateUtils.getWeekNumber(dt);
    }

    public int getEffectiveYear(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getAnio() : -1;
        }
        return cl.reciclajelitoral.util.WeekDateUtils.getYear(dt);
    }

    private LocalDateTime getEffectiveLocalDateTime(DetalleInspeccion d) {
        if (d.getFechaHoraInicial() != null) return d.getFechaHoraInicial();
        if (d.getFechaHoraActualizacion() != null) return d.getFechaHoraActualizacion();
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getCreadoEn() != null) {
            return d.getInspeccionSemanal().getCreadoEn();
        }
        return null;
    }

    @Transactional
    public InspeccionSemanalDTO limpiarSemanaActualConRespaldo(Long comunaId, Long inspectorId) {
        InspeccionSemanalDTO rutaDTO = obtenerOCrearInspeccionSemanal(comunaId, inspectorId);
        InspeccionSemanal ruta = inspeccionRepository.findById(rutaDTO.getId()).orElseThrow();

        try {
            List<SnapshotItem> itemsSnapshot = ruta.getDetalles().stream()
                    .map(d -> SnapshotItem.builder()
                            .contenedorId(d.getContenedor() != null ? d.getContenedor().getId() : null)
                            .visitado(d.getVisitado())
                            .porcentajeEstimado(d.getPorcentajeEstimado())
                            .kilosCalculados(d.getKilosCalculados())
                            .porcentajeEstimadoInicial(d.getPorcentajeEstimadoInicial())
                            .kilosCalculadosInicial(d.getKilosCalculadosInicial())
                            .observaciones(d.getObservaciones())
                            .observacionesInicial(d.getObservacionesInicial())
                            .fechaHoraInicial(d.getFechaHoraInicial())
                            .fechaHoraActualizacion(d.getFechaHoraActualizacion())
                            .creadoPorUsuarioId(d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getId() : null)
                            .actualizadoPorUsuarioId(d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getId() : null)
                            .fotos(d.getFotos() != null ? d.getFotos().stream()
                                    .map(f -> FotoSnapshotItem.builder()
                                            .momento(f.getMomento() != null ? f.getMomento().name() : null)
                                            .urlFoto(f.getUrlFoto())
                                            .creadoEn(f.getCreadoEn())
                                            .usuarioId(f.getUsuario() != null ? f.getUsuario().getId() : null)
                                            .build())
                                    .collect(Collectors.toList()) : List.of())
                            .build())
                    .collect(Collectors.toList());

            String jsonSnapshot = objectMapper.writeValueAsString(itemsSnapshot);
            ruta.setRespaldoEstadoPrevio(jsonSnapshot);
            ruta.setTieneRespaldoLimpieza(true);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el respaldo pre-limpieza: " + e.getMessage(), e);
        }

        for (DetalleInspeccion d : ruta.getDetalles()) {
            d.setVisitado(false);
            d.setPorcentajeEstimado(BigDecimal.ZERO);
            d.setKilosCalculados(BigDecimal.ZERO);
            d.setPorcentajeEstimadoInicial(null);
            d.setKilosCalculadosInicial(null);
            d.setObservaciones(null);
            d.setObservacionesInicial(null);
            d.setFechaHoraInicial(null);
            d.setFechaHoraActualizacion(null);
            d.getFotos().clear();
        }

        InspeccionSemanal guardada = inspeccionRepository.save(ruta);
        return convertirADTO(guardada);
    }

    @Transactional
    public InspeccionSemanalDTO revertirLimpiezaSemanaActual(Long comunaId, Long inspectorId) {
        InspeccionSemanalDTO rutaDTO = obtenerOCrearInspeccionSemanal(comunaId, inspectorId);
        InspeccionSemanal ruta = inspeccionRepository.findById(rutaDTO.getId()).orElseThrow();

        if (!Boolean.TRUE.equals(ruta.getTieneRespaldoLimpieza()) || ruta.getRespaldoEstadoPrevio() == null) {
            throw new IllegalStateException("No existe un respaldo pre-limpieza disponible para revertir.");
        }

        try {
            List<SnapshotItem> items = objectMapper.readValue(ruta.getRespaldoEstadoPrevio(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SnapshotItem.class));

            for (SnapshotItem item : items) {
                Optional<DetalleInspeccion> dOpt = ruta.getDetalles().stream()
                        .filter(d -> d.getContenedor() != null && d.getContenedor().getId().equals(item.getContenedorId()))
                        .findFirst();

                if (dOpt.isPresent()) {
                    DetalleInspeccion d = dOpt.get();
                    d.setVisitado(Boolean.TRUE.equals(item.getVisitado()));
                    d.setPorcentajeEstimado(item.getPorcentajeEstimado());
                    d.setKilosCalculados(item.getKilosCalculados());
                    d.setPorcentajeEstimadoInicial(item.getPorcentajeEstimadoInicial());
                    d.setKilosCalculadosInicial(item.getKilosCalculadosInicial());
                    d.setObservaciones(item.getObservaciones());
                    d.setObservacionesInicial(item.getObservacionesInicial());
                    d.setFechaHoraInicial(item.getFechaHoraInicial());
                    d.setFechaHoraActualizacion(item.getFechaHoraActualizacion());

                    if (item.getCreadoPorUsuarioId() != null) {
                        usuarioRepository.findById(item.getCreadoPorUsuarioId()).ifPresent(d::setCreadoPorUsuario);
                    }
                    if (item.getActualizadoPorUsuarioId() != null) {
                        usuarioRepository.findById(item.getActualizadoPorUsuarioId()).ifPresent(d::setActualizadoPorUsuario);
                    }

                    d.getFotos().clear();
                    if (item.getFotos() != null) {
                        for (FotoSnapshotItem fSnap : item.getFotos()) {
                            Usuario uFoto = fSnap.getUsuarioId() != null ? usuarioRepository.findById(fSnap.getUsuarioId()).orElse(null) : null;
                            FotoInspeccion f = FotoInspeccion.builder()
                                    .detalleInspeccion(d)
                                    .momento(fSnap.getMomento() != null ? MomentoFoto.valueOf(fSnap.getMomento()) : MomentoFoto.INICIAL_ANTES)
                                    .urlFoto(fSnap.getUrlFoto())
                                    .creadoEn(fSnap.getCreadoEn() != null ? fSnap.getCreadoEn() : LocalDateTime.now())
                                    .usuario(uFoto)
                                    .build();
                            d.getFotos().add(f);
                        }
                    }
                }
            }

            ruta.setTieneRespaldoLimpieza(false);
            ruta.setRespaldoEstadoPrevio(null);

            InspeccionSemanal guardada = inspeccionRepository.save(ruta);
            return convertirADTO(guardada);
        } catch (Exception e) {
            throw new RuntimeException("Error al revertir la limpieza: " + e.getMessage(), e);
        }
    }

    private boolean tieneFotosLista(List<String> fotos) {
        return Optional.ofNullable(fotos).map(l -> !l.isEmpty()).orElse(false);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SnapshotItem {
        private Long contenedorId;
        private Boolean visitado;
        private BigDecimal porcentajeEstimado;
        private BigDecimal kilosCalculados;
        private BigDecimal porcentajeEstimadoInicial;
        private BigDecimal kilosCalculadosInicial;
        private String observaciones;
        private String observacionesInicial;
        private LocalDateTime fechaHoraInicial;
        private LocalDateTime fechaHoraActualizacion;
        private Long creadoPorUsuarioId;
        private Long actualizadoPorUsuarioId;
        private List<FotoSnapshotItem> fotos;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FotoSnapshotItem {
        private String momento;
        private String urlFoto;
        private LocalDateTime creadoEn;
        private Long usuarioId;
    }
}
