/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict.rate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RateMapper<T> {

    T map(String id, Integer ratio, BigDecimal sell, BigDecimal buy, LocalDateTime date);
}
