package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.InspeccionSemanal;
import cl.reciclajelitoral.entity.TipoRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InspeccionSemanalRepository extends JpaRepository<InspeccionSemanal, Long> {
    Optional<InspeccionSemanal> findByComunaIdAndTipoRutaAndSemanaNumeroAndAnio(
            Long comunaId, TipoRuta tipoRuta, Integer semanaNumero, Integer anio
    );

    Optional<InspeccionSemanal> findByComunaIdAndInspectorIdAndSemanaNumeroAndAnio(
            Long comunaId, Long inspectorId, Integer semanaNumero, Integer anio
    );
}
