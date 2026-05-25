/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.core.Department;
import web.repository.CustomJpaRepository;
import web.repository.core.projection.DepartmentIdUsersCount;

public interface DepartmentRepository extends CustomJpaRepository<Department, Long> {

    @Query("SELECT d.id AS id, COUNT(u) AS usersCount FROM Department d LEFT JOIN d.users u GROUP BY d.id")
    List<DepartmentIdUsersCount> countUsers();

    @Query(value = "With tree(id, status) AS  (SELECT DEPARTMENTS_ID,  STATUS FROM CORE.DEPARTMENTS WHERE  " +
                   "PARENT_DEPARTMENT = :id UNION ALL SELECT CORE.DEPARTMENTS.DEPARTMENTS_ID, CORE.DEPARTMENTS" +
                   ".status FROM tree, CORE.DEPARTMENTS WHERE tree.id = CORE.DEPARTMENTS.PARENT_DEPARTMENT) " +
                   "SELECT COUNT(*) FROM tree WHERE tree.status = 1", nativeQuery = true)
    Long countActiveChildren(@Param("id") Long id);

    @Query(value = "With tree(id, pid, status) AS  (SELECT DEPARTMENTS_ID, PARENT_DEPARTMENT, STATUS FROM CORE" +
                   ".DEPARTMENTS WHERE DEPARTMENTS_ID = (SELECT PARENT_DEPARTMENT from CORE.DEPARTMENTS where " +
                   "DEPARTMENTS_ID = :id) UNION ALL SELECT CORE.DEPARTMENTS.DEPARTMENTS_ID, Core" +
                   ".DEPARTMENTS.PARENT_DEPARTMENT, CORE.DEPARTMENTS.status FROM tree, CORE.DEPARTMENTS WHERE " +
                   "tree.pid = CORE.DEPARTMENTS.DEPARTMENTS_ID) SELECT COUNT(*) FROM tree WHERE status = 0", nativeQuery = true)
    Long countNotActiveParents(@Param("id") Long id);
}
