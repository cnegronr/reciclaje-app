package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InspeccionSemanalDTO {
    private Long id;
    private Long comunaId;
    private Long inspectorId;
    private Long inspectorAsociadoId;
    private String inspectorAsociadoNombre;
    private String rolUsuario;
    private Integer semanaNumero;
    private Integer anio;
    private LocalDateTime fechaLimite;
    private String estado;
    private Boolean tieneRespaldoLimpieza;
    private List<DetalleInspeccionDTO> detalles;
}
