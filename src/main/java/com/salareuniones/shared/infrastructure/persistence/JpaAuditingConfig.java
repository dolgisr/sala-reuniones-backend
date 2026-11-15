package com.salareuniones.shared.infrastructure.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Activa la auditoría de Spring Data JPA para que se rellenen {@code @CreatedDate} y {@code @LastModifiedDate}. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
