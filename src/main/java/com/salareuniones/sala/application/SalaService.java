package com.salareuniones.sala.application;

import com.salareuniones.sala.domain.Sala;
import com.salareuniones.sala.domain.SalaDuplicadaException;
import com.salareuniones.sala.domain.SalaRepository;
import com.salareuniones.shared.application.BaseService;
import org.springframework.stereotype.Service;

@Service
public class SalaService extends BaseService<Sala, SalaRepository> {

    public SalaService(SalaRepository repository) {
        super(repository, "Sala");
    }

    @Override
    protected void validateCreate(Sala sala) {
        if (repository.findByNombreIgnoreCase(sala.getNombre()).isPresent()) {
            throw new SalaDuplicadaException(sala.getNombre());
        }
    }
}
