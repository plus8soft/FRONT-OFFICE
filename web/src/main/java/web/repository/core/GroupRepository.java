/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import java.util.List;
import web.entity.core.Group;
import web.repository.CustomJpaRepository;

public interface GroupRepository extends CustomJpaRepository<Group, Long> {

    List<Group> findByAreUserIs(boolean areUser);
}
