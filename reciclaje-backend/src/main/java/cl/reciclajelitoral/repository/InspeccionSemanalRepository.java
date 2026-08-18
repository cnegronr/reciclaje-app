package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.InspeccionSemanal;
import cl.reciclajelitoral.entity.TipoRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InspeccionSemanalRepository extends JpaRepository<InspeccionSemanal, Long> {
    Optional<InspeccionSemanal> findFirstByComunaIdAndTipoRutaAndSemanaNumeroAndAnioOrderByIdDesc(
            Long comunaId, TipoRuta tipoRuta, Integer semanaNumero, Integer anio
    );

    default Optional<InspeccionSemanal> findByComunaIdAndTipoRutaAndSemanaNumeroAndAnio(
            Long comunaId, TipoRuta tipoRuta, Integer semanaNumero, Integer anio
    ) {
        return findFirstByComunaIdAndTipoRutaAndSemanaNumeroAndAnioOrderByIdDesc(comunaId, tipoRuta, semanaNumero, anio);
    }

    Optional<InspeccionSemanal> findFirstByComunaIdAndInspectorIdAndSemanaNumeroAndAnioOrderByIdDesc(
            Long comunaId, Long inspectorId, Integer semanaNumero, Integer anio
    );

    default Optional<InspeccionSemanal> findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(
            Long comunaId, Long inspectorId, Integer semanaNumero, Integer anio
    ) {
        return findFirstByComunaIdAndInspectorIdAndSemanaNumeroAndAnioOrderByIdDesc(comunaId, inspectorId, semanaNumero, anio);
    }

    Optional<InspeccionSemanal> findFirstByComunaIdAndInspectorIdOrderByAnioDescSemanaNumeroDesc(
            Long comunaId, Long inspectorId
    );

    List<InspeccionSemanal> findByComunaIdAndSemanaNumeroAndAnio(
            Long comunaId, Integer semanaNumero, Integer anio
    );

    @Query("""
        SELECT i FROM InspeccionSemanal i 
        WHERE i.comuna.id = :comunaId 
          AND (i.anio < :anioActual OR (i.anio = :anioActual AND i.semanaNumero < :semanaActual))
        ORDER BY i.anio DESC, i.semanaNumero DESC
    """)
    List<InspeccionSemanal> findInspeccionesPreviasByComuna(
            @org.springframework.data.repository.query.Param("comunaId") Long comunaId,
            @org.springframework.data.repository.query.Param("semanaActual") Integer semanaActual,
            @org.springframework.data.repository.query.Param("anioActual") Integer anioActual
    );

    @Query("SELECT DISTINCT i.anio FROM InspeccionSemanal i WHERE i.anio IS NOT NULL ORDER BY i.anio DESC")
    List<Integer> findDistinctAnios();
}
