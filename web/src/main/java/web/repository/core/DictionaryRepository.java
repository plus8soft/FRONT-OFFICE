/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.core.Dictionary;
import web.repository.CustomJpaRepository;

public interface DictionaryRepository extends CustomJpaRepository<Dictionary, String> {

    @Query("SELECT r.group FROM Dictionary r WHERE r.group LIKE %:group%")
    List<String> findDictionaryByGroup(@Param("group") String group);
}
