/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.dict.Counteragent;
import web.repository.CustomJpaRepository;

public interface CounteragentRepository extends CustomJpaRepository<Counteragent, Long> {

    @Query("SELECT MAX(c.version) FROM Counteragent c WHERE c.ein = :ein")
    Optional<Long> findMaxVersion(@Param("ein") String ein);
}
