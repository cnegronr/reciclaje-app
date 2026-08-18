package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.DetalleInspeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DetalleInspeccionRepository extends JpaRepository<DetalleInspeccion, Long> {
    Optional<DetalleInspeccion> findByInspeccionSemanalIdAndContenedorId(
            Long inspeccionSemanalId, Long contenedorId
    );

    @org.springframework.data.jpa.repository.Query("""
        SELECT d FROM DetalleInspeccion d 
        WHERE d.contenedor.comuna.id = :comunaId AND d.visitado = true
    """)
    java.util.List<DetalleInspeccion> findVisitadasByComunaId(
            @org.springframework.data.repository.query.Param("comunaId") Long comunaId
    );
}
