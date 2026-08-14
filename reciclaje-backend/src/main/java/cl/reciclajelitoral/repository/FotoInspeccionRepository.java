package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.FotoInspeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoInspeccionRepository extends JpaRepository<FotoInspeccion, Long> {
}
