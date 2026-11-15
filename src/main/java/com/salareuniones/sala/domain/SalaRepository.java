package com.salareuniones.sala.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, UUID> {

    Optional<Sala> findByNombreIgnoreCase(String nombre);
}
