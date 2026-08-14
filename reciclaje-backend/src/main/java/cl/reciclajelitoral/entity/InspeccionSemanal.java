package cl.reciclajelitoral.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspecciones_semanales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspeccionSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private Usuario inspector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_asociado_id")
    private Usuario inspectorAsociado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ruta", length = 20)
    @Builder.Default
    private TipoRuta tipoRuta = TipoRuta.INSPECTOR;

    @Column(name = "semana_numero", nullable = false)
    private Integer semanaNumero;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoInspeccion estado = EstadoInspeccion.EN_PROGRESO;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "inspeccionSemanal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleInspeccion> detalles = new ArrayList<>();
}
