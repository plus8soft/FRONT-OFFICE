/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.log.operation;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.Currency;
import web.entity.pay.Payment;
import web.repository.dict.CurrencyRepository;

@Getter
@Setter
public class OperationPaymentShowView implements Serializable {

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency baseCurrency;

    private Payment operation;

    public void init(Payment operation) {
        this.operation = operation;
        baseCurrency = currencyRepository.findOne("840"); // USD
    }
}
