package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComunaDTO {
    private Long id;
    private String nombre;
    private String codigoRegion;
    private Long inspectorAsociadoId;
    private String inspectorAsociadoNombre;
    private List<ContenedorDTO> contenedores;
}
