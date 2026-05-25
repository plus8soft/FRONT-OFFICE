/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.ce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.ce.CurrencyOperation;
import web.entity.crm.Person;
import web.repository.CustomJpaRepository;

public interface CurrencyOperationRepository extends CustomJpaRepository<CurrencyOperation, Long> {

    CurrencyOperation findTopByOrderByNumberDesc();

    @Query("SELECT SUM(CASE WHEN o.commissionEnabled = true " +
           "THEN (o.baseAmount + CASE WHEN o.code = web.entity.log.OperationCode.SELL THEN o.commission ELSE -o.commission END) " +
           "ELSE o.baseAmount END) FROM CurrencyOperation o WHERE o.personHistory.person = :person AND o.date BETWEEN :startDate AND :endDate")
    BigDecimal getBaseAmountByPersonAndDate(@Param("person") Person person, @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
