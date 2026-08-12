package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    List<Contenedor> findByComunaId(Long comunaId);
}
