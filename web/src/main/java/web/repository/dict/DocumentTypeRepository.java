/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.crm.Person;
import web.entity.dict.DocumentType;
import web.repository.CustomJpaRepository;

public interface DocumentTypeRepository extends CustomJpaRepository<DocumentType, String> {

    @Query("SELECT type FROM DocumentType type WHERE type.required = true AND type NOT IN (SELECT copy.bankDocumentType " +
           "FROM DocumentCopy copy WHERE copy.person = :person)")
    List<DocumentType> findDismissRequiredDocuments(@Param("person") Person person);
}
