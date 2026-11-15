package com.salareuniones.sala.domain;

import com.salareuniones.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "salas")
public class Sala extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    private Sala(UUID id, String nombre) {
        super(id);
        this.nombre = nombre;
    }

    public static Sala create(String nombre) {
        return new Sala(UUID.randomUUID(), nombre);
    }
}
