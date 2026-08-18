package cl.reciclajelitoral.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "contenedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "nombre_punto", nullable = false, length = 150)
    private String nombrePunto;

    @Column(name = "ubicacion_descripcion", columnDefinition = "TEXT")
    private String ubicacionDescripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private CategoriaContenedor categoria; // EMPRESA | MUNICIPAL

    @Column(name = "kilos_maximos", nullable = false, precision = 6, scale = 2)
    private BigDecimal kilosMaximos; // 500.00 o 1000.00

    @Column(name = "url_google_maps", nullable = false, columnDefinition = "TEXT")
    private String urlGoogleMaps;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id")
    private Comuna comuna;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    // Método de negocio: Cálculo automático de kilos según categoría
    public BigDecimal calcularKilos(BigDecimal porcentajeEstimado) {
        if (porcentajeEstimado == null) return BigDecimal.ZERO;
        BigDecimal max = (this.categoria == CategoriaContenedor.EMPRESA) ? 
            BigDecimal.valueOf(500) : BigDecimal.valueOf(1000);
        return porcentajeEstimado.divide(BigDecimal.valueOf(100)).multiply(max);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSector() { return sector; }
    public void setSector(String s) { this.sector = s; }
    public String getNombrePunto() { return nombrePunto; }
    public void setNombrePunto(String nombrePunto) { this.nombrePunto = nombrePunto; }
    public String getUbicacionDescripcion() { return ubicacionDescripcion; }
    public void setUbicacionDescripcion(String u) { this.ubicacionDescripcion = u; }
    public CategoriaContenedor getCategoria() { return categoria; }
    public void setCategoria(CategoriaContenedor c) { this.categoria = c; }
    public BigDecimal getKilosMaximos() { return kilosMaximos; }
    public void setKilosMaximos(BigDecimal k) { this.kilosMaximos = k; }
    public String getUrlGoogleMaps() { return urlGoogleMaps; }
    public void setUrlGoogleMaps(String url) { this.urlGoogleMaps = url; }
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal l) { this.latitud = l; }
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal l) { this.longitud = l; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Comuna getComuna() { return comuna; }
    public void setComuna(Comuna c) { this.comuna = c; }
}
