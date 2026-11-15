package com.salareuniones.sala.infrastructure;

import com.salareuniones.sala.domain.Sala;
import com.salareuniones.shared.infrastructure.http.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class SalaMapper implements BaseMapper<Sala, SalaDto> {

    @Override
    public SalaDto toDto(Sala sala) {
        return new SalaDto(sala.getId(), sala.getNombre());
    }
}
