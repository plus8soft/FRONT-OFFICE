/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.util.Set;
import web.entity.dict.Currency;
import web.entity.dict.PaymentPoint;
import web.entity.dict.PaymentSystem;
import web.entity.dict.PaymentSystemName;
import web.entity.dict.Region;
import web.repository.CustomJpaRepository;

public interface PaymentPointRepository extends CustomJpaRepository<PaymentPoint, Long> {

    PaymentPoint findTopByPaymentSystem_IdAndRegionAndCurrenciesContains(PaymentSystemName id, Region region, Set<Currency> currencies);

    void deleteByPaymentSystem(PaymentSystem paymentSystem);
}
