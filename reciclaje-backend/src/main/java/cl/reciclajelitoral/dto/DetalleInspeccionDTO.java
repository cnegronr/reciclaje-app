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
@AllArgsConstructor
@NoArgsConstructor
public class DetalleInspeccionDTO {
    private Long id;
    private Long contenedorId;
    private Long creadoPorUsuarioId;
    private String creadoPorUsuarioNombre;
    private Long actualizadoPorUsuarioId;
    private String actualizadoPorUsuarioNombre;
    private BigDecimal porcentajeEstimado;
    private BigDecimal kilosCalculados;
    private Boolean visitado;
    private LocalDateTime fechaHoraInicial;
    private LocalDateTime fechaHoraActualizacion;
    private String observaciones;
    private List<FotoInspeccionDTO> fotos;
    private List<ActualizacionDetalleDTO> actualizacionesHistorial;
}
