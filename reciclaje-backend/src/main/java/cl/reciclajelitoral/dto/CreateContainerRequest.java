package cl.reciclajelitoral.dto;

import cl.reciclajelitoral.entity.CategoriaContenedor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateContainerRequest {
    @NotNull(message = "La comuna es requerida")
    private Long comunaId;

    @NotBlank(message = "El nombre del punto es requerido")
    private String nombrePunto;

    private String ubicacionDescripcion;

    @NotNull(message = "La categoría es requerida")
    private CategoriaContenedor categoria;

    private BigDecimal kilosMaximos;

    private String urlGoogleMaps;

    private BigDecimal latitud;

    private BigDecimal longitud;

    public Long getComunaId() { return comunaId; }
    public String getNombrePunto() { return nombrePunto; }
    public String getUbicacionDescripcion() { return ubicacionDescripcion; }
    public CategoriaContenedor getCategoria() { return categoria; }
    public BigDecimal getKilosMaximos() { return kilosMaximos; }
    public String getUrlGoogleMaps() { return urlGoogleMaps; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
}
