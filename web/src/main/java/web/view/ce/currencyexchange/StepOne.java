/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.currencyexchange;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import web.entity.ce.CurrencyOperation;
import web.entity.ce.Order;
import web.entity.ce.Rate;
import web.entity.dict.Currency;
import web.entity.log.OperationCode;
import web.repository.ce.RateRepository;
import web.service.ce.order.OrderService;
import web.session.UserSession;

@Configurable
@Getter
@Setter
@Log4j2
public class StepOne implements Serializable {

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserSession userSession;

    private List<Currency> currencies;

    private CurrencyOperation operation;

    private String errorMessage;

    public void init(CurrencyOperation operation) {
        Order order = orderService.findCurrent(userSession.getUser());
        if (order == null) {
            errorMessage = "It is necessary to create an order";
        } else {
            currencies = rateRepository.findFetchCurrencyByOrder(order).stream().sorted(Comparator.comparing(Rate::getOperationCurrencyPosition))
                                       .map(Rate::getCurrency).distinct().collect(Collectors.toList());
            this.operation = new CurrencyOperation();
            this.operation.setOrder(order);
            this.operation.setCode(Optional.ofNullable(operation).map(CurrencyOperation::getCode).orElse(OperationCode.SELL));
            this.operation.setCurrency(Optional.ofNullable(operation).map(CurrencyOperation::getCurrency).map(currency -> {
                return currencies.stream().filter(currentCurrency -> Objects.equals(currency, currentCurrency)).findAny().orElse(null);
            }).orElse(currencies.get(0)));
            this.operation.setSum(Optional.ofNullable(operation).map(CurrencyOperation::getSum).orElse(null));
        }
    }

    public List<Currency> displayedCurrencies() {
        final List<Currency> result = currencies.stream().limit(currencies.size() == 3 ? 3 : 2).collect(Collectors.toList());
        if (currencies.size() > 3) {
            currencies.stream().filter(c -> c.equals(operation.getCurrency())).findFirst()
                      .ifPresent(c -> result.add(currencies.indexOf(c) > 1 ? c : null));
        }
        return result;
    }

    public List<Currency> hiddenCurrencies(List<Currency> displayedCurrencies) {
        List<Currency> currencies = new ArrayList<>(this.currencies);
        currencies.removeAll(displayedCurrencies);
        return currencies;
    }

    public String calculate() {
        operation.setDate(LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()));
        return "next";
    }
}
