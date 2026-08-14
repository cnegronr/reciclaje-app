package cl.reciclajelitoral.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizacionDetalleDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private BigDecimal porcentajeEstimado;
    private BigDecimal kilosCalculados;
    private String observaciones;
    private LocalDateTime fechaHora;
    @Builder.Default
    private List<FotoInspeccionDTO> fotos = new ArrayList<>();
}
