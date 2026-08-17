package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.DashboardMetricsDTO;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.*;
import cl.reciclajelitoral.util.WeekDateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UsuarioRepository usuarioRepository;
    private final ContenedorRepository contenedorRepository;
    private final DetalleInspeccionRepository detalleRepository;
    private final ComunaRepository comunaRepository;
    private final FotoInspeccionRepository fotoRepository;

    private LocalDateTime getEffectiveLocalDateTime(DetalleInspeccion d) {
        if (d.getFechaHoraInicial() != null) return d.getFechaHoraInicial();
        if (d.getFechaHoraActualizacion() != null) return d.getFechaHoraActualizacion();
        if (d.getInspeccionSemanal() != null && d.getInspeccionSemanal().getCreadoEn() != null) {
            return d.getInspeccionSemanal().getCreadoEn();
        }
        return null;
    }

    private ZonedDateTime getEffectiveZonedDateTime(DetalleInspeccion d) {
        LocalDateTime dt = getEffectiveLocalDateTime(d);
        return dt != null ? dt.atZone(WeekDateUtils.CHILE_ZONE) : null;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDTO getMetrics(String scope, String period, Long userId, Long comunaId, String role, String region) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Contenedor> contenedores = contenedorRepository.findAll();
        List<Comuna> comunas = comunaRepository.findAll();
        long totalFotos = fotoRepository.count();

        ZonedDateTime ahoraChile = WeekDateUtils.nowInChile();
        int currentWeek = WeekDateUtils.getCurrentWeekNumber();
        int currentYear = WeekDateUtils.getCurrentYear();

        List<DetalleInspeccion> detallesVisitados = detalleRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getVisitado()))
                .filter(d -> {
                    if (period == null || "HISTORIC".equalsIgnoreCase(period) || "ALL".equalsIgnoreCase(period)) {
                        return true;
                    }
                    ZonedDateTime zdt = getEffectiveZonedDateTime(d);
                    if (zdt == null) return false;

                    if ("DAY".equalsIgnoreCase(period) || "TODAY".equalsIgnoreCase(period)) {
                        return zdt.toLocalDate().equals(ahoraChile.toLocalDate());
                    }
                    if ("WEEK".equalsIgnoreCase(period) || "CURRENT_WEEK".equalsIgnoreCase(period)) {
                        int week = WeekDateUtils.getWeekNumber(zdt.toLocalDateTime());
                        int year = WeekDateUtils.getYear(zdt.toLocalDateTime());
                        return week == currentWeek && year == currentYear;
                    }
                    if ("PAST_WEEK".equalsIgnoreCase(period)) {
                        int week = WeekDateUtils.getWeekNumber(zdt.toLocalDateTime());
                        int year = WeekDateUtils.getYear(zdt.toLocalDateTime());
                        return week == (currentWeek - 1) && year == currentYear;
                    }
                    if ("MONTH".equalsIgnoreCase(period)) {
                        return zdt.getMonth() == ahoraChile.getMonth() && zdt.getYear() == ahoraChile.getYear();
                    }
                    if ("YEAR".equalsIgnoreCase(period)) {
                        return zdt.getYear() == ahoraChile.getYear();
                    }
                    return true;
                })
                .filter(d -> {
                    if (userId == null) return true;
                    return (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(userId)) ||
                           (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(userId));
                })
                .filter(d -> {
                    if (comunaId == null) return true;
                    return d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(comunaId);
                })
                .filter(d -> {
                    if (role == null || role.trim().isEmpty()) return true;
                    Usuario u = d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario() : d.getCreadoPorUsuario();
                    return u != null && u.getRol() != null && u.getRol().name().equalsIgnoreCase(role.trim());
                })
                .filter(d -> {
                    if (region == null || region.trim().isEmpty()) return true;
                    return d.getContenedor() != null && d.getContenedor().getComuna() != null &&
                            region.equalsIgnoreCase(d.getContenedor().getComuna().getCodigoRegion());
                })
                .collect(Collectors.toList());

        BigDecimal sumKilos = detallesVisitados.stream()
                .map(d -> Optional.ofNullable(d.getKilosCalculados()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgPorcentajeDouble = detallesVisitados.stream()
                .mapToDouble(d -> Optional.ofNullable(d.getPorcentajeEstimado()).map(BigDecimal::doubleValue).orElse(0.0))
                .average()
                .orElse(0.0);
        BigDecimal avgPorcentaje = BigDecimal.valueOf(avgPorcentajeDouble).setScale(2, RoundingMode.HALF_UP);

        // Desglose por usuario
        List<DashboardMetricsDTO.UserMetricItem> userMetrics = usuarios.stream()
                .filter(u -> userId == null || u.getId().equals(userId))
                .filter(u -> role == null || role.trim().isEmpty() || (u.getRol() != null && u.getRol().name().equalsIgnoreCase(role.trim())))
                .map(u -> {
                    long countInsp = detallesVisitados.stream()
                            .filter(d -> (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(u.getId())) ||
                                    (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(u.getId())))
                            .count();

                    BigDecimal uKilos = detallesVisitados.stream()
                            .filter(d -> (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(u.getId())) ||
                                    (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(u.getId())))
                            .map(d -> Optional.ofNullable(d.getKilosCalculados()).orElse(BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DashboardMetricsDTO.UserMetricItem.builder()
                            .usuarioId(u.getId())
                            .usuarioNombre(u.getNombre())
                            .rol(u.getRol() != null ? u.getRol().name() : "INSPECTOR")
                            .inspeccionesRealizadas(countInsp)
                            .kilosAcumulados(uKilos)
                            .build();
                }).collect(Collectors.toList());

        // Desglose por comuna
        List<DashboardMetricsDTO.ComunaMetricItem> comunaMetrics = comunas.stream()
                .filter(c -> comunaId == null || c.getId().equals(comunaId))
                .filter(c -> region == null || region.trim().isEmpty() || region.equalsIgnoreCase(c.getCodigoRegion()))
                .map(c -> {
                    long totalContComuna = contenedores.stream()
                            .filter(cont -> cont.getComuna() != null && cont.getComuna().getId().equals(c.getId()))
                            .count();

                    List<DetalleInspeccion> detallesComuna = detallesVisitados.stream()
                            .filter(d -> d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(c.getId()))
                            .collect(Collectors.toList());

                    BigDecimal cKilos = detallesComuna.stream()
                            .map(d -> Optional.ofNullable(d.getKilosCalculados()).orElse(BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double cAvgPorc = detallesComuna.stream()
                            .mapToDouble(d -> Optional.ofNullable(d.getPorcentajeEstimado()).map(BigDecimal::doubleValue).orElse(0.0))
                            .average()
                            .orElse(0.0);

                    return DashboardMetricsDTO.ComunaMetricItem.builder()
                            .comunaId(c.getId())
                            .comunaNombre(c.getNombre())
                            .codigoRegion(c.getCodigoRegion())
                            .totalContenedores(totalContComuna)
                            .inspeccionesCompletadas((long) detallesComuna.size())
                            .kilosRecolectados(cKilos)
                            .porcentajeLlenadoPromedio(BigDecimal.valueOf(cAvgPorc).setScale(2, RoundingMode.HALF_UP))
                            .build();
                }).collect(Collectors.toList());

        return DashboardMetricsDTO.builder()
                .scope(scope != null ? scope : "ALL")
                .period(period != null ? period : "HISTORIC")
                .totalUsuarios((long) userMetrics.size())
                .totalContenedores((long) contenedores.size())
                .totalInspecciones((long) detallesVisitados.size())
                .totalKilosCalculados(sumKilos)
                .promedioPorcentajeLlenado(avgPorcentaje)
                .totalFotosCargadas(totalFotos)
                .userMetrics(userMetrics)
                .comunaMetrics(comunaMetrics)
                .build();
    }
}
