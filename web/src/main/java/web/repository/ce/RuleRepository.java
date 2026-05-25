/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.ce;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import web.entity.ce.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long>, JpaSpecificationExecutor<Rule> {

    Rule findBySystemNameAndSystem(String systemName, boolean system);

    @Query("SELECT MAX(r.position) FROM Rule r WHERE r.currency = TRUE")
    Optional<Integer> findLastPosition();
}
