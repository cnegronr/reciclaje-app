package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    Optional<Comuna> findByNombre(String nombre);
}
