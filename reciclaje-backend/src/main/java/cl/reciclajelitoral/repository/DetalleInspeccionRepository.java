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
        JOIN FETCH d.contenedor c 
        JOIN FETCH c.comuna com 
        WHERE com.id = :comunaId AND d.visitado = true
    """)
    java.util.List<DetalleInspeccion> findVisitadasByComunaId(
            @org.springframework.data.repository.query.Param("comunaId") Long comunaId
    );

    @org.springframework.data.jpa.repository.Query("""
        SELECT d FROM DetalleInspeccion d 
        JOIN FETCH d.contenedor c 
        JOIN FETCH c.comuna com 
        JOIN FETCH d.inspeccionSemanal ins 
        WHERE com.id = :comunaId 
          AND ins.tipoRuta = cl.reciclajelitoral.entity.TipoRuta.INSPECTOR 
          AND d.visitado = true
        ORDER BY d.fechaHoraInicial DESC, d.id DESC
    """)
    java.util.List<DetalleInspeccion> findVisitadasInspectorByComunaId(
            @org.springframework.data.repository.query.Param("comunaId") Long comunaId
    );

    @org.springframework.data.jpa.repository.Query("""
        SELECT d FROM DetalleInspeccion d 
        JOIN FETCH d.contenedor c 
        JOIN FETCH c.comuna com 
        LEFT JOIN FETCH d.creadoPorUsuario 
        LEFT JOIN FETCH d.actualizadoPorUsuario 
        LEFT JOIN FETCH d.inspeccionSemanal ins 
        LEFT JOIN FETCH ins.inspector 
        WHERE d.visitado = true
    """)
    java.util.List<DetalleInspeccion> findAllVisitadosWithRelaciones();

    @org.springframework.data.jpa.repository.Query("""
        SELECT DISTINCT YEAR(d.fechaHoraInicial) 
        FROM DetalleInspeccion d 
        WHERE d.visitado = true AND d.fechaHoraInicial IS NOT NULL
        ORDER BY YEAR(d.fechaHoraInicial) DESC
    """)
    java.util.List<Integer> findDistinctAniosVisitados();
}
