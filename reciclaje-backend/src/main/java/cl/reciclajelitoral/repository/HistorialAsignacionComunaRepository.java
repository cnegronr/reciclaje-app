package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.HistorialAsignacionComuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialAsignacionComunaRepository extends JpaRepository<HistorialAsignacionComuna, Long> {
    List<HistorialAsignacionComuna> findByInspectorIdOrderByFechaHoraDesc(Long inspectorId);
    List<HistorialAsignacionComuna> findByComunaIdOrderByFechaHoraDesc(Long comunaId);
}
