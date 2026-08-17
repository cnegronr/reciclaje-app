package cl.reciclajelitoral.service;

import cl.reciclajelitoral.dto.DashboardMetricsDTO;
import cl.reciclajelitoral.entity.Comuna;
import cl.reciclajelitoral.entity.Contenedor;
import cl.reciclajelitoral.entity.DetalleInspeccion;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");

    private final UsuarioRepository usuarioRepository;
    private final ContenedorRepository contenedorRepository;
    private final DetalleInspeccionRepository detalleRepository;
    private final ComunaRepository comunaRepository;
    private final FotoInspeccionRepository fotoRepository;

    private int getEffectiveWeekNumber(DetalleInspeccion d) {
        LocalDateTime dt = d.getFechaHoraInicial() != null ? d.getFechaHoraInicial() :
                (d.getFechaHoraActualizacion() != null ? d.getFechaHoraActualizacion() :
                        (d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getCreadoEn() : null));
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getSemanaNumero() : -1;
        }
        return cl.reciclajelitoral.util.WeekDateUtils.getWeekNumber(dt);
    }

    private int getEffectiveYear(DetalleInspeccion d) {
        LocalDateTime dt = d.getFechaHoraInicial() != null ? d.getFechaHoraInicial() :
                (d.getFechaHoraActualizacion() != null ? d.getFechaHoraActualizacion() :
                        (d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getCreadoEn() : null));
        if (dt == null) {
            return d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getAnio() : -1;
        }
        return cl.reciclajelitoral.util.WeekDateUtils.getYear(dt);
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDTO getMetrics(String scope, String period, Long userId, Long comunaId, String role, String region) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Contenedor> contenedores = contenedorRepository.findAll();

        ZonedDateTime ahoraChile = ZonedDateTime.now(CHILE_ZONE);
        int currentWeek = ahoraChile.get(WeekFields.ISO.weekOfWeekBasedYear());
        int currentYear = ahoraChile.getYear();

        List<DetalleInspeccion> detallesVisitados = detalleRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getVisitado()))
                .filter(d -> {
                    if ("CURRENT_WEEK".equalsIgnoreCase(period)) {
                        return getEffectiveWeekNumber(d) == currentWeek && getEffectiveYear(d) == currentYear;
                    }
                    if ("PAST_WEEK".equalsIgnoreCase(period)) {
                        return getEffectiveWeekNumber(d) == (currentWeek - 1) && getEffectiveYear(d) == currentYear;
                    }
                    return true;
                })
                .filter(d -> comunaId == null || (d.getContenedor() != null && d.getContenedor().getComuna() != null && d.getContenedor().getComuna().getId().equals(comunaId)))
                .filter(d -> userId == null || (d.getActualizadoPorUsuario() != null && d.getActualizadoPorUsuario().getId().equals(userId)) || (d.getCreadoPorUsuario() != null && d.getCreadoPorUsuario().getId().equals(userId)))
                .collect(Collectors.toList());

        List<Comuna> comunas = comunaRepository.findAll();
        long totalFotos = fotoRepository.count();

        BigDecimal sumKilos = detallesVisitados.stream()
                .map(d -> Optional.ofNullable(d.getKilosCalculados()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgPorcentajeDouble = detallesVisitados.stream()
                .mapToDouble(d -> Optional.ofNullable(d.getPorcentajeEstimado()).map(BigDecimal::doubleValue).orElse(0.0))
                .average()
                .orElse(0.0);
        BigDecimal avgPorcentaje = BigDecimal.valueOf(avgPorcentajeDouble).setScale(2, RoundingMode.HALF_UP);

        // Desglose por usuario
        List<DashboardMetricsDTO.UserMetricItem> userMetrics = usuarios.stream().map(u -> {
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
        List<DashboardMetricsDTO.ComunaMetricItem> comunaMetrics = comunas.stream().map(c -> {
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
                .totalUsuarios((long) usuarios.size())
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
