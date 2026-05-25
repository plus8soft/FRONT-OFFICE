/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.dict.Bank;
import web.repository.CustomJpaRepository;

public interface BankRepository extends CustomJpaRepository<Bank, Long> {

    @Query("SELECT r.name FROM Bank r WHERE r.name LIKE %:name%")
    List<String> findBankByName(@Param("name") String name);
}
