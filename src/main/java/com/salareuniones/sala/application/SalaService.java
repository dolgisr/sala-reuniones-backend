package com.salareuniones.sala.application;

import com.salareuniones.sala.domain.Sala;
import com.salareuniones.sala.domain.SalaRepository;
import com.salareuniones.sala.domain.errors.SalaDuplicadaException;
import com.salareuniones.shared.application.BaseService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalaService extends BaseService<Sala, SalaRepository> {

    public SalaService(SalaRepository repository) {
        super(repository);
    }

    /**
     * Bloquea la fila de la sala (SELECT ... FOR UPDATE) hasta que termine la transacción que la llama.
     * Debe usarse dentro de una transacción ya abierta para que el bloqueo sirva de algo.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Sala lockById(UUID id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
    }

    @Override
    protected void validateCreate(Sala sala) {
        if (repository.findByNombreIgnoreCase(sala.getNombre()).isPresent()) {
            throw new SalaDuplicadaException(sala.getNombre());
        }
    }
}
