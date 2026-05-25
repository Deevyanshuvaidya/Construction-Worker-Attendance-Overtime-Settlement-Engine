package com.company.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration enabling automatic auditing and declarative transaction management.
 *
 * <p>With {@link EnableJpaAuditing}, entity fields annotated with
 * {@link org.springframework.data.annotation.CreatedDate @CreatedDate} and
 * {@link org.springframework.data.annotation.LastModifiedDate @LastModifiedDate}
 * are populated automatically by the persistence layer.</p>
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaAuditingConfig {
}
