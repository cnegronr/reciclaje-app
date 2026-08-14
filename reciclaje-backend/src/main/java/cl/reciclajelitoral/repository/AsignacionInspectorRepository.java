package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.AsignacionInspector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsignacionInspectorRepository extends JpaRepository<AsignacionInspector, Long> {
    List<AsignacionInspector> findByInspectorId(Long inspectorId);
    List<AsignacionInspector> findByComunaId(Long comunaId);
}
