package com.salareuniones.shared.application;

import com.salareuniones.shared.domain.BaseEntity;
import com.salareuniones.shared.domain.EntidadNoEncontradaException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseService<T extends BaseEntity, R extends JpaRepository<T, UUID>> {

    protected final R repository;
    private final String entityName;

    protected BaseService(R repository, String entityName) {
        this.repository = repository;
        this.entityName = entityName;
    }

    @Transactional(readOnly = true)
    public T findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException(entityName, id));
    }

    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public T create(T entity) {
        validateCreate(entity);
        return repository.save(entity);
    }

    /** Reglas de negocio que debe cumplir la entidad antes de crearse; cada servicio las sobrescribe. */
    protected void validateCreate(T entity) {
    }

    protected T save(T entity) {
        return repository.save(entity);
    }
}
