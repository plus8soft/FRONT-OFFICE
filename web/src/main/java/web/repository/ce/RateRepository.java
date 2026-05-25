/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.ce;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.ce.Order;
import web.entity.ce.Rate;
import web.entity.core.Department;
import web.entity.dict.Currency;

public interface RateRepository extends JpaRepository<Rate, Long>, JpaSpecificationExecutor<Rate> {

    Rate findByCurrencyAndDepartmentAndOrderAndRuleName(Currency currency, Department department, Order order, String ruleName);

    @Query("SELECT r FROM Rate r JOIN FETCH r.currency WHERE r.order = :order")
    List<Rate> findFetchCurrencyByOrder(@Param("order") Order order);
}
