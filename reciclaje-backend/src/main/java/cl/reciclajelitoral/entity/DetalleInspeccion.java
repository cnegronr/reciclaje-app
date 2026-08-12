package cl.reciclajelitoral.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "detalle_inspecciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleInspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspeccion_semanal_id", nullable = false)
    private InspeccionSemanal inspeccionSemanal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @Column(name = "porcentaje_estimado", precision = 5, scale = 2)
    private BigDecimal porcentajeEstimado;

    @Column(name = "kilos_calculados", precision = 7, scale = 2)
    private BigDecimal kilosCalculados;

    @Column(name = "visitado")
    private Boolean visitado = false;

    @Column(name = "fecha_hora_inicial")
    private LocalDateTime fechaHoraInicial;

    @Column(name = "fecha_hora_actualizacion")
    private LocalDateTime fechaHoraActualizacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "detalleInspeccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FotoInspeccion> fotos = new ArrayList<>();

    // Helper para agregar foto
    public void addFoto(FotoInspeccion foto) {
        foto.setDetalleInspeccion(this);
        this.fotos.add(foto);
    }
}
