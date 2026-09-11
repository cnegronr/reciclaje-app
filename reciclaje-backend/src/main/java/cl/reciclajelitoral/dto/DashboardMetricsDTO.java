package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMetricsDTO {
    private String scope;
    private String period;
    private Long totalUsuarios;
    private Long totalContenedores;
    private Long totalInspecciones;
    private BigDecimal totalKilosCalculados;
    private BigDecimal totalKilosAcumulados;
    private BigDecimal totalKilosRetirados;
    private BigDecimal promedioPorcentajeLlenado;
    private BigDecimal promedioPorcentajeAcumulados;
    private BigDecimal promedioPorcentajeRetirados;
    private Long totalFotosCargadas;

    private List<UserMetricItem> userMetrics;
    private List<ComunaMetricItem> comunaMetrics;
    private List<InspectorComunaMetricItem> inspectorComunaMetrics;
    private List<ChoferComunaMetricItem> choferComunaMetrics;

    public String getScope() { return scope; }
    public String getPeriod() { return period; }
    public Long getTotalUsuarios() { return totalUsuarios; }
    public Long getTotalContenedores() { return totalContenedores; }
    public Long getTotalInspecciones() { return totalInspecciones; }
    public BigDecimal getTotalKilosCalculados() { return totalKilosCalculados; }
    public BigDecimal getTotalKilosAcumulados() { return totalKilosAcumulados; }
    public BigDecimal getTotalKilosRetirados() { return totalKilosRetirados; }
    public BigDecimal getPromedioPorcentajeLlenado() { return promedioPorcentajeLlenado; }
    public BigDecimal getPromedioPorcentajeAcumulados() { return promedioPorcentajeAcumulados; }
    public BigDecimal getPromedioPorcentajeRetirados() { return promedioPorcentajeRetirados; }
    public Long getTotalFotosCargadas() { return totalFotosCargadas; }
    public List<UserMetricItem> getUserMetrics() { return userMetrics; }
    public List<ComunaMetricItem> getComunaMetrics() { return comunaMetrics; }
    public List<InspectorComunaMetricItem> getInspectorComunaMetrics() { return inspectorComunaMetrics; }
    public List<ChoferComunaMetricItem> getChoferComunaMetrics() { return choferComunaMetrics; }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContenedorInspeccionadoItem {
        private Long contenedorId;
        private String nombrePunto;
        private String sector;
        private String categoria;
        private BigDecimal porcentaje;
        private BigDecimal kilos;
        private BigDecimal kilosRetirados;
        private java.time.LocalDateTime fechaInspeccion;
        private String inspectorNombre;
        private String choferNombre;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InspectorComunaMetricItem {
        private Long comunaId;
        private String comunaNombre;
        private String codigoRegion;
        private Long totalContenedores;
        private String inspectorNombre;
        private Long inspeccionesCompletadas;
        private BigDecimal kilosCalculados;
        private BigDecimal porcentajeLlenadoPromedio;
        private List<ContenedorInspeccionadoItem> contenedoresInspeccionados;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChoferComunaMetricItem {
        private Long comunaId;
        private String comunaNombre;
        private String codigoRegion;
        private Long totalContenedores;
        private Long inspeccionesCompletadas;
        private BigDecimal kilosRetirados;
        private BigDecimal porcentajeLlenadoPromedio;
        private List<ContenedorInspeccionadoItem> contenedoresInspeccionados;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserMetricItem {
        private Long usuarioId;
        private String usuarioNombre;
        private String rol;
        private Long inspeccionesRealizadas;
        private BigDecimal kilosAcumulados;

        public Long getUsuarioId() { return usuarioId; }
        public String getUsuarioNombre() { return usuarioNombre; }
        public String getRol() { return rol; }
        public Long getInspeccionesRealizadas() { return inspeccionesRealizadas; }
        public BigDecimal getKilosAcumulados() { return kilosAcumulados; }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComunaMetricItem {
        private Long comunaId;
        private String comunaNombre;
        private String codigoRegion;
        private Long totalContenedores;
        private Long inspeccionesCompletadas;
        private BigDecimal kilosRecolectados;
        private BigDecimal porcentajeLlenadoPromedio;

        public Long getComunaId() { return comunaId; }
        public String getComunaNombre() { return comunaNombre; }
        public String getCodigoRegion() { return codigoRegion; }
        public Long getTotalContenedores() { return totalContenedores; }
        public Long getInspeccionesCompletadas() { return inspeccionesCompletadas; }
        public BigDecimal getKilosRecolectados() { return kilosRecolectados; }
        public BigDecimal getPorcentajeLlenadoPromedio() { return porcentajeLlenadoPromedio; }
    }
}
