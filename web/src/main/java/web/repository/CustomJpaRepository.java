/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomJpaRepository<T, I extends Serializable> extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

    String MSSQL_RECOMPILE_HINT = "RECOMPILE";

    List<T> findAll(Specification<T> spec, int offset, int limit, Sort sort, String... hints);

    List<T> findAll(Specification<T> spec, int offset, int limit, String... hints);

    List<T> findAll(Specification<T> spec, int limit, Sort sort, String... hints);

    List<T> findAll(Specification<T> spec, int limit, String... hints);

    T findAny(Specification<T> spec, String... hints);

    boolean exists(Specification<T> spec);
}
