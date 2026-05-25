/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.UpdateResult;
import web.repository.CustomJpaRepository;

public interface DictionaryParameterRepository extends CustomJpaRepository<DictionaryParameter, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE DictionaryParameter a SET a.schedule = :schedule, a.enabled = :enabled WHERE a.id = :id")
    void update(@Param("id") Long id, @Param("schedule") String schedule, @Param("enabled") Boolean enabled);

    @Transactional
    @Modifying
    @Query("UPDATE DictionaryParameter a SET a.lastUpdateResult = :updateResult, a.errorMessage = :errorMessage WHERE a.id = :id")
    void update(@Param("id") Long id, @Param("updateResult") UpdateResult updateResult, @Param("errorMessage") String errorMessage);

    @Transactional
    @Modifying
    @Query("UPDATE DictionaryParameter a SET a.lastUpdateResult = :updateResult, a.errorMessage = :errorMessage, a.version = :version, " +
           "a.sourceUpdateDate = :sourceUpdateDate, a.successUpdateDate = :successUpdateDate WHERE a.id = :id")
    void update(@Param("id") Long id, @Param("updateResult") UpdateResult updateResult, @Param("errorMessage") String errorMessage,
                @Param("version") Long version, @Param("sourceUpdateDate") LocalDateTime sourceUpdateDate,
                @Param("successUpdateDate") Instant successUpdateDate);
}
