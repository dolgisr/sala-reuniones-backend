package com.salareuniones.reserva.domain;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    // SELECT ... FOR UPDATE: dos cancelaciones simultáneas de la misma reserva se ejecutan de una en una
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> findByIdForUpdate(@Param("id") UUID id);

    // Dos intervalos [inicio, fin) se solapan si cada uno empieza antes de que termine el otro
    @Query("""
            select count(r) > 0 from Reserva r
            where r.sala.id = :salaId
              and r.estado = :estado
              and r.inicio < :fin
              and r.fin > :inicio
            """)
    boolean existeSolapamiento(@Param("salaId") UUID salaId,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin,
                               @Param("estado") EstadoReserva estado);

    // Reservas de la sala que ocupan algún momento del rango, incluidas las que cruzan la medianoche
    @Query("""
            select r from Reserva r
            where r.sala.id = :salaId
              and r.inicio < :hasta
              and r.fin > :desde
            """)
    Page<Reserva> buscarPorSalaEnRango(@Param("salaId") UUID salaId,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta,
                                       Pageable pageable);
}
