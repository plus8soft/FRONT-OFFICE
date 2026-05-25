/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.hibernate.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class CustomJpaRepositoryImpl<T, I extends Serializable> extends SimpleJpaRepository<T, I> implements CustomJpaRepository<T, I> {

    private EntityManager em;

    public CustomJpaRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
    }

    @Override
    public List<T> findAll(Specification<T> spec, int offset, int limit, Sort sort, String... hints) {
        TypedQuery<T> query = getQuery(spec, sort).setFirstResult(offset).setMaxResults(limit);
        Stream.of(hints).forEach(hint -> ((Query) query).addQueryHint(hint));
        return query.getResultList();
    }

    @Override
    public List<T> findAll(Specification<T> spec, int offset, int limit, String... hints) {
        return findAll(spec, offset, limit, null, hints);
    }

    @Override
    public List<T> findAll(Specification<T> spec, int limit, Sort sort, String... hints) {
        return findAll(spec, 0, limit, sort, hints);
    }

    @Override
    public List<T> findAll(Specification<T> spec, int limit, String... hints) {
        return findAll(spec, limit, null, hints);
    }

    @Override
    public T findAny(Specification<T> spec, String... hints) {
        try {
            TypedQuery<T> query = getQuery(spec, (Sort) null).setMaxResults(1);
            Stream.of(hints).forEach(((Query) query)::addQueryHint);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean exists(Specification<T> spec) {
        return getCountQuery(spec, getDomainClass()).getSingleResult() > 0;
    }
}
