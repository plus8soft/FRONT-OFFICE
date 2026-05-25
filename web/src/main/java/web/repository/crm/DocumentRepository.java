/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.crm;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.crm.Document;
import web.entity.crm.Person;
import web.repository.CustomJpaRepository;

public interface DocumentRepository extends CustomJpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE d.person = :person " +
           "AND d.type IN (web.utils.Documents.NATIONAL_PASSPORT_CODE, web.utils.Documents.FOREIGN_CITIZEN_PASSPORT_CODE)")
    Document findMain(@Param("person") Person person);

    @Query("SELECT d FROM Document d WHERE d.person = :person " +
           "AND d.type NOT IN (web.utils.Documents.NATIONAL_PASSPORT_CODE, web.utils.Documents.FOREIGN_CITIZEN_PASSPORT_CODE)")
    List<Document> findAdditional(@Param("person") Person person);
}
