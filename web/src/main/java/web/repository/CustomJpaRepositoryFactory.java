/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository;

import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

public class CustomJpaRepositoryFactory extends JpaRepositoryFactory {

    private EntityManager em;

    public CustomJpaRepositoryFactory(EntityManager em) {
        super(em);
        this.em = em;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        return new CustomJpaRepositoryImpl(information.getDomainType(), em);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return CustomJpaRepositoryImpl.class;
    }
}
