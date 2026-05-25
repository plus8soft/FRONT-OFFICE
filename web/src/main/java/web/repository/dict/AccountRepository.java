/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import web.entity.core.Department;
import web.entity.dict.Account;
import web.repository.CustomJpaRepository;

public interface AccountRepository extends CustomJpaRepository<Account, String> {

    void deleteAllByDepartmentAndIdStartingWith(Department department, String id);
}
