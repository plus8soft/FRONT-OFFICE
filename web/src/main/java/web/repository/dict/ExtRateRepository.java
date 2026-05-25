/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.time.LocalDateTime;
import web.entity.ce.RateType;
import web.entity.dict.Currency;
import web.entity.dict.ExtRate;
import web.repository.CustomJpaRepository;

public interface ExtRateRepository extends CustomJpaRepository<ExtRate, Long> {

    ExtRate findTopByCurrencyAndDateLessThanEqualAndTypeOrderByDateDesc(Currency currency, LocalDateTime date, RateType rateType);
}
