/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.log;

import org.springframework.data.repository.NoRepositoryBean;
import web.entity.log.AbstractEvent;
import web.repository.CustomJpaRepository;

@NoRepositoryBean
public interface AbstractEventRepository<T extends AbstractEvent> extends CustomJpaRepository<T, Long> {

}
