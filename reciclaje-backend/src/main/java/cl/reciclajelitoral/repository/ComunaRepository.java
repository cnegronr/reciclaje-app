package cl.reciclajelitoral.repository;

import cl.reciclajelitoral.entity.Comuna;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    Optional<Comuna> findByNombre(String nombre);

    @Override
    @EntityGraph(attributePaths = {"contenedores"})
    List<Comuna> findAll();

    @Override
    @EntityGraph(attributePaths = {"contenedores"})
    Optional<Comuna> findById(Long id);
}
