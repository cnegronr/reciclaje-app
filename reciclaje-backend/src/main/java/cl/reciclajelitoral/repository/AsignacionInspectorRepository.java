package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.AsignacionInspector;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsignacionInspectorRepository extends JpaRepository<AsignacionInspector, Long> {
    @EntityGraph(attributePaths = {"inspector", "comuna"})
    List<AsignacionInspector> findByInspectorId(Long inspectorId);

    @EntityGraph(attributePaths = {"inspector", "comuna"})
    List<AsignacionInspector> findByComunaId(Long comunaId);
    void deleteByComunaId(Long comunaId);
    void deleteByInspectorId(Long inspectorId);
}
