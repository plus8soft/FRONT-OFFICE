/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.crm;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.crm.Address;
import web.entity.crm.Person;
import web.repository.CustomJpaRepository;

public interface AddressRepository extends CustomJpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a INNER JOIN a.personAddresses pa WHERE pa.person = :person AND pa.type = :type")
    Address findByPersonAndType(@Param("person") Person person, @Param("type") String type);
}
