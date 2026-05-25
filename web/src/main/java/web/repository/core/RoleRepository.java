/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.core.Role;
import web.repository.CustomJpaRepository;

public interface RoleRepository extends CustomJpaRepository<Role, Long> {

    @Query("SELECT r.groupName FROM Role r WHERE r.groupName LIKE %:groupName%")
    List<String> findDistinctGroupNameByGroupName(@Param("groupName") String groupName);
}
