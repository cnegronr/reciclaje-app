package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContenedorDTO {
    private Long id;
    private String nombrePunto;
    private String ubicacionDescripcion;
    private String categoria;
    private BigDecimal kilosMaximos;
    private String urlGoogleMaps;
    private BigDecimal latitud;
    private BigDecimal longitud;
}
