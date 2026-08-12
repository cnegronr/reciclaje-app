package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.DetalleInspeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DetalleInspeccionRepository extends JpaRepository<DetalleInspeccion, Long> {
    Optional<DetalleInspeccion> findByInspeccionSemanalIdAndContenedorId(
            Long inspeccionSemanalId, Long contenedorId
    );
}
