package cl.reciclajelitoral.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_inspeccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FotoInspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_inspeccion_id", nullable = false)
    private DetalleInspeccion detalleInspeccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "momento", nullable = false, length = 25)
    private MomentoFoto momento;

    @Column(name = "url_foto", nullable = false, columnDefinition = "TEXT")
    private String urlFoto;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();
}
