package edu.uespoo135.gt3.reservapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.uespoo135.gt3.reservapi.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> { }
