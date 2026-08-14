package cl.reciclajelitoral.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actualizaciones_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizacionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_inspeccion_id", nullable = false)
    private DetalleInspeccion detalleInspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "porcentaje_estimado", precision = 5, scale = 2)
    private BigDecimal porcentajeEstimado;

    @Column(name = "kilos_calculados", precision = 7, scale = 2)
    private BigDecimal kilosCalculados;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_hora")
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    @OneToMany(mappedBy = "actualizacionDetalle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FotoInspeccion> fotos = new ArrayList<>();

    public void addFoto(FotoInspeccion foto) {
        foto.setActualizacionDetalle(this);
        foto.setDetalleInspeccion(this.detalleInspeccion);
        this.fotos.add(foto);
        if (this.detalleInspeccion != null && !this.detalleInspeccion.getFotos().contains(foto)) {
            this.detalleInspeccion.getFotos().add(foto);
        }
    }
}
