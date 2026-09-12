package cl.reciclajelitoral.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_asignaciones_comuna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialAsignacionComuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private Usuario inspector;

    @Column(name = "inspector_nombre", nullable = false, length = 100)
    private String inspectorNombre;

    @Column(name = "inspector_email", nullable = false, length = 100)
    private String inspectorEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id")
    private Comuna comuna;

    @Column(name = "comuna_nombre", nullable = false, length = 100)
    private String comunaNombre;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "ejecutado_por_email", length = 100)
    private String ejecutadoPorEmail;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
