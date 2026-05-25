/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository;

import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class CustomJpaRepositoryFactoryBean<T extends JpaRepository<S, I>, S, I extends Serializable> extends JpaRepositoryFactoryBean<T, S, I> {

    public CustomJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new CustomJpaRepositoryFactory(em);
    }
}
