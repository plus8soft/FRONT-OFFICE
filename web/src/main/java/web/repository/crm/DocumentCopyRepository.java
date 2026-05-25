/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.crm;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.crm.DocumentCopy;
import web.entity.crm.Person;
import web.repository.CustomJpaRepository;

public interface DocumentCopyRepository extends CustomJpaRepository<DocumentCopy, Long> {

    @Query("SELECT MAX(copies.version) FROM DocumentCopy copies WHERE copies.person = :person AND copies.external = :external " +
           "AND copies.bankDocumentType.id = :documentTypeId")
    Optional<Integer> findMaxVersion(@Param("person") Person person, @Param("external") boolean external,
                                     @Param("documentTypeId") Long documentTypeId);

    @Query("SELECT MAX(copies.version) FROM DocumentCopy copies WHERE copies.person = :person AND copies.external = :external " +
           "AND copies.clientDocumentType = :documentTypeId AND copies.name = :name")
    Optional<Integer> findMaxVersion(@Param("person") Person person, @Param("external") boolean external,
                                     @Param("documentTypeId") String documentTypeId, @Param("name") String name);

    @Query("SELECT copy FROM DocumentCopy copy JOIN FETCH copy.bankDocumentType WHERE (SELECT MAX(maxVersionCopy.version) FROM DocumentCopy " +
           "maxVersionCopy WHERE copy.person = :person AND ((copy.name IS NOT NULL AND copy.name = maxVersionCopy.name) OR " +
           "(copy.bankDocumentType = maxVersionCopy.bankDocumentType))) = copy.version AND copy.validUntilDate < :today")
    List<DocumentCopy> findExpiredCopies(@Param("person") Person person, @Param("today") LocalDate today);
}
