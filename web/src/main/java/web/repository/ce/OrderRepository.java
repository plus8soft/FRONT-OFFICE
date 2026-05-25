/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.ce;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.ce.DealStatus;
import web.entity.ce.Order;
import web.entity.core.Department;
import web.entity.core.User;
import web.repository.CustomJpaRepository;

public interface OrderRepository extends CustomJpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.department WHERE o.department IN (:departments) AND NOT EXISTS (SELECT 1 FROM Order tmp " +
           "WHERE o.department = tmp.department AND tmp.date > o.date)")
    List<Order> findLastOrdersByDepartments(@Param("departments") List<Department> departments);

    Order findTopByDepartmentAndDealUserIsNullOrderByIdDesc(Department department);

    Order findTopByDepartmentAndDealUserIsNullAndDateLessThanEqualOrderByIdDesc(Department department, LocalDateTime date);

    @Query("SELECT o FROM Order o JOIN FETCH o.department JOIN FETCH o.dealUser WHERE o.department = :department AND o.dealUser = :dealUser " +
           "AND o.dealStatus = :status")
    Order findByDepartmentAndDealUserAndDealStatus(@Param("department") Department department, @Param("dealUser") User dealUser,
                                                   @Param("status") DealStatus status);

    Order findTopByDepartmentAndDateGreaterThanEqualOrderByNumberDesc(Department department, LocalDateTime date);
}
