/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import web.entity.core.Password;
import web.entity.core.User;
import web.repository.CustomJpaRepository;

public interface PasswordRepository extends CustomJpaRepository<Password, Long> {

    Password findTopByUserOrderByIdDesc(User user);
}
