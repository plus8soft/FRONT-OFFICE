/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.currency;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.repository.dict.CurrencyRepository;

@Getter
@Setter
@Component
public class CurrencyView implements Serializable {

    @Autowired
    private CurrencyRepository currencyRepository;

    private List<Currency> currencies;

    public void init() {
        currencies = currencyRepository.findAll(new Sort(Currency_.position.getName()));
    }
}
