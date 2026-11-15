package com.salareuniones.sala.infrastructure.http;

import com.salareuniones.sala.domain.Sala;
import com.salareuniones.shared.infrastructure.http.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class SalaMapper implements BaseMapper<Sala, SalaResponse> {

    @Override
    public SalaResponse toDto(Sala sala) {
        return new SalaResponse(sala.getId(), sala.getNombre());
    }
}
