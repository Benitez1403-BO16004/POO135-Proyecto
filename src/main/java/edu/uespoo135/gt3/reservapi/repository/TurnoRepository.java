package edu.uespoo135.gt3.reservapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.uespoo135.gt3.reservapi.model.Turno;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    boolean existsByNombre(String nombre);
}
