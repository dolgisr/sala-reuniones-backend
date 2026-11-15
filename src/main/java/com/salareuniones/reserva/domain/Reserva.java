package com.salareuniones.reserva.domain;

import com.salareuniones.reserva.domain.errors.ReservaYaCanceladaException;
import com.salareuniones.sala.domain.Sala;
import com.salareuniones.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "reservas",
        indexes = @Index(name = "ix_reservas_sala_inicio", columnList = "sala_id, inicio")
)
public class Reserva extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    @Column(name = "responsable", nullable = false, length = 100)
    private String responsable;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado", nullable = false)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    private Reserva(UUID id, Sala sala, String responsable, LocalDateTime inicio, LocalDateTime fin) {
        super(id);
        this.sala = sala;
        this.responsable = responsable;
        this.inicio = inicio;
        this.fin = fin;
    }

    public static Reserva create(Sala sala, String responsable, LocalDateTime inicio, LocalDateTime fin) {
        return new Reserva(UUID.randomUUID(), sala, responsable, inicio, fin);
    }

    public void cancelar() {
        if (estado == EstadoReserva.CANCELADA) {
            throw new ReservaYaCanceladaException();
        }
        estado = EstadoReserva.CANCELADA;
    }
}
