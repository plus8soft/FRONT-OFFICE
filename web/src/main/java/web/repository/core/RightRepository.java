/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.entity.core.Right;

public interface RightRepository extends JpaRepository<Right, Long>, JpaSpecificationExecutor<Right> {

}
