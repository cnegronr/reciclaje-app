package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.ActualizacionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActualizacionDetalleRepository extends JpaRepository<ActualizacionDetalle, Long> {
}
