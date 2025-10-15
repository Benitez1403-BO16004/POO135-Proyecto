package edu.uespoo135.gt3.reservapi.model;

import jakarta.persistence.*;
import lombok.*;

// Lombok genera automáticamente el código repetitivo
@Data                   // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Constructor vacío
@AllArgsConstructor      // Constructor con todos los campos
@Builder                 // Permite crear objetos con patrón Builder
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 10)
    private String telefono;
}
