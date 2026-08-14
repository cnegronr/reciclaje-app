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
    private BigDecimal promedioPorcentajeLlenado;
    private Long totalFotosCargadas;

    private List<UserMetricItem> userMetrics;
    private List<ComunaMetricItem> comunaMetrics;

    public String getScope() { return scope; }
    public String getPeriod() { return period; }
    public Long getTotalUsuarios() { return totalUsuarios; }
    public Long getTotalContenedores() { return totalContenedores; }
    public Long getTotalInspecciones() { return totalInspecciones; }
    public BigDecimal getTotalKilosCalculados() { return totalKilosCalculados; }
    public BigDecimal getPromedioPorcentajeLlenado() { return promedioPorcentajeLlenado; }
    public Long getTotalFotosCargadas() { return totalFotosCargadas; }
    public List<UserMetricItem> getUserMetrics() { return userMetrics; }
    public List<ComunaMetricItem> getComunaMetrics() { return comunaMetrics; }

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
