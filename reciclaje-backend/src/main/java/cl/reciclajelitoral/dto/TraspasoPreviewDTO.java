package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraspasoPreviewDTO {

    private Integer semanaOrigen;
    private Integer anioOrigen;
    private Integer semanaDestino;
    private Integer anioDestino;

    private boolean permitidoTraspaso;
    private String mensajeValidacion;

    private long totalVisitadasOrigen;
    private long totalVisitadasDestinoActual;

    private List<DetalleItemPreview> detallesVisitados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleItemPreview {
        private Long detalleId;
        private Long contenedorId;
        private String contenedorNombre;
        private String comunaNombre;
        private String categoria;
        private BigDecimal porcentajeEstimado;
        private BigDecimal kilosCalculados;
        private String inspectorNombre;
        private LocalDateTime fechaHoraInicial;
        private String observaciones;
    }
}
