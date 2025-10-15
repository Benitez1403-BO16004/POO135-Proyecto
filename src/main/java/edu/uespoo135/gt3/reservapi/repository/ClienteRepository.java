package edu.uespoo135.gt3.reservapi.repository;
import edu.uespoo135.gt3.reservapi.model.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
