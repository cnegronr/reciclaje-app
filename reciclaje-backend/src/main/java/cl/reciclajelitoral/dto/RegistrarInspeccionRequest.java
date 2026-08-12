package cl.reciclajelitoral.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RegistrarInspeccionRequest {
    @NotNull(message = "El id del contenedor es requerido")
    private Long contenedorId;

    @NotNull(message = "El porcentaje estimado es requerido")
    private BigDecimal porcentajeEstimado;

    private String observaciones;

    private List<String> fotosAntesUrls;
    private List<String> fotosDespuesUrls;

    private boolean esActualizacion;
}
