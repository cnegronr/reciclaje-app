package cl.reciclajelitoral.dto;

import cl.reciclajelitoral.entity.CategoriaContenedor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerAdminDTO {
    private Long id;
    private Long comunaId;
    private String comunaNombre;
    private String nombrePunto;
    private String ubicacionDescripcion;
    private CategoriaContenedor categoria;
    private BigDecimal kilosMaximos;
    private String urlGoogleMaps;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Boolean activo;

    public Long getId() { return id; }
    public Long getComunaId() { return comunaId; }
    public String getComunaNombre() { return comunaNombre; }
    public String getNombrePunto() { return nombrePunto; }
    public String getUbicacionDescripcion() { return ubicacionDescripcion; }
    public CategoriaContenedor getCategoria() { return categoria; }
    public BigDecimal getKilosMaximos() { return kilosMaximos; }
    public String getUrlGoogleMaps() { return urlGoogleMaps; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public Boolean getActivo() { return activo; }
}
