package com.salareuniones.sala.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaRepository extends JpaRepository<Sala, UUID> {

    Optional<Sala> findByNombreIgnoreCase(String nombre);

    // SELECT ... FOR UPDATE: bloquea la fila de la sala hasta que termine la transacción
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> findByIdForUpdate(@Param("id") UUID id);
}
