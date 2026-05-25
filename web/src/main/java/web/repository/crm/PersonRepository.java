/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.crm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.entity.crm.Person;

public interface PersonRepository extends PersonRepositoryCustom, JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {

}
