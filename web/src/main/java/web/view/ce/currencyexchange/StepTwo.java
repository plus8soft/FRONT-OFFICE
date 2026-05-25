/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.currencyexchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import web.entity.ce.CurrencyOperation;
import web.entity.ce.Order;
import web.entity.ce.Rate;
import web.entity.ce.Rate_;
import web.entity.ce.RuleParameter;
import web.entity.ce.RuleParameter_;
import web.entity.ce.Rule_;
import web.entity.ce.Sign;
import web.entity.dict.Currency;
import web.entity.log.OperationCode;
import web.repository.back.BackException;
import web.repository.ce.OrderRepository;
import web.repository.ce.RateRepository;
import web.repository.ce.RuleParameterRepository;
import web.repository.ce.RuleRepository;
import web.service.ce.CurrencyExchangeService;
import web.service.ce.order.OrderService;
import web.session.UserSession;

@Configurable
@Getter
@Setter
@Log4j2
public class StepTwo implements Serializable {

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleParameterRepository ruleParameterRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserSession userSession;

    private Rate rate;

    private CurrencyOperation operation;

    private BigDecimal cash;

    private BigDecimal equivalentCash;

    private String errorMessage;

    private boolean enoughCash;

    private boolean identificationRequired;

    private boolean commissionAvailable;

    public void init(CurrencyOperation operation) {
        this.operation = operation;
        try {
            rate = findRate(operation.getOrder(), operation.getCurrency(), operation.getSum());
            if (rate == null) {
                throw new CurrencyExchangeException("Exchange rate is not set for this operation amount");
            } else {
                operation.setRatio(rate.getRatio());
                operation.setRate(OperationCode.SELL.equals(operation.getCode()) ? rate.getSellRate() : rate.getBuyRate());
                operation.setExternalRate(rate.getExternalRate());
                operation.setBaseAmount(operation.getSum().multiply(operation.getRate().divide(BigDecimal.valueOf(operation.getRatio())))
                                             .setScale(2, RoundingMode.HALF_UP));
                calculateCommission();
                if (commissionAvailable) {
                    operation.setCommissionEnabled(true);
                }
                checkCash();
            }
        } catch (CurrencyExchangeException e) {
            log.error(e.getMessage(), e);
            errorMessage = e.getMessage();
        }
    }

    private void checkCash() {
        enoughCash = false;
        try {
            cash = currencyExchangeService
                    .receiveAccountRest(userSession.getUser(), OperationCode.SELL == operation.getCode() ? operation.getCurrency().getId() : "840"); // USD
        } catch (BackException e) {
            throw new CurrencyExchangeException(e.getMessage(), e);
        }
        enoughCash = OperationCode.SELL.equals(operation.getCode()) && operation.getSum().compareTo(cash) == 1 ||
                     OperationCode.BUY.equals(operation.getCode()) && operation.getBaseAmount().compareTo(cash) == 1;
        if (enoughCash) {
            equivalentCash = calculateEquivalentCash(operation, cash);
            throw new CurrencyExchangeException("Insufficient amount for exchange operation");
        }
    }

    private boolean identificationRequired() {
        return Optional.ofNullable(ruleRepository.findOne(
                (root, query, cb) -> cb.and(cb.equal(root.get(Rule_.system), true), cb.equal(root.get(Rule_.systemName), "PERS_IDENT"))))
                       .map(rule -> operation.getBaseAmount().compareTo(rule.getMin()) > 0).orElse(false);
    }

    private BigDecimal calculateEquivalentCash(CurrencyOperation operation, BigDecimal cash) {
        if (OperationCode.SELL.equals(operation.getCode())) {
            return cash.multiply(operation.getRate().divide(BigDecimal.valueOf(operation.getRatio()))).setScale(2, RoundingMode.HALF_UP);
        } else {
            return cash.multiply(BigDecimal.valueOf(operation.getRatio())).divide(operation.getRate(), 2, RoundingMode.DOWN);
        }
    }

    private Rate findRate(Order order, Currency currency, BigDecimal sum) {
        return rateRepository.findOne((root, cq, cb) -> {
            root.fetch(Rate_.order);
            return cb.and(cb.equal(root.get(Rate_.order), order), cb.equal(root.get(Rate_.currency), currency), cb.le(root.get(Rate_.min), sum),
                          cb.or(cb.ge(root.get(Rate_.max), sum), cb.isNull(root.get(Rate_.max))));
        });
    }

    private void calculateCommission() {
        RuleParameter ruleParameter = ruleParameterRepository.findOne((root, cq, cb) -> {
            root.fetch(RuleParameter_.rule);
            return cb.and(cb.equal(root.get(RuleParameter_.enabled), true),
                          cb.equal(root.get(RuleParameter_.department), userSession.getUser().getDepartment()),
                          cb.equal(root.get(RuleParameter_.rule).get(Rule_.enabled), true),
                          cb.equal(root.get(RuleParameter_.rule).get(Rule_.commision), true),
                          cb.le(root.get(RuleParameter_.rule).get(Rule_.min), operation.getBaseAmount()),
                          cb.or(cb.ge(root.get(RuleParameter_.rule).get(Rule_.max), operation.getBaseAmount()),
                                cb.isNull(root.get(RuleParameter_.rule).get(Rule_.max))));
        });
        if (ruleParameter != null) {
            commissionAvailable = true;
            Sign sign = OperationCode.SELL.equals(operation.getCode()) ? ruleParameter.getSellSign() : ruleParameter.getBuySign();
            BigDecimal value =
                    Optional.ofNullable(OperationCode.SELL.equals(operation.getCode()) ? ruleParameter.getSellValue() : ruleParameter.getBuyValue())
                            .map(val -> Sign.MINUS.equals(sign) ? val.negate() : val).orElse(null);
            BigDecimal percent = Optional.ofNullable(
                    OperationCode.SELL.equals(operation.getCode()) ? ruleParameter.getSellPercent() : ruleParameter.getBuyPercent())
                                         .map(val -> Sign.MINUS.equals(sign) ? val.negate() : val).orElse(null);
            operation.setCommission(Optional.ofNullable(value).map(val -> val.setScale(2, RoundingMode.HALF_UP)).orElseGet(
                    () -> operation.getBaseAmount().multiply(percent.divide(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP)));
        }
    }

    public BigDecimal getBaseAmountWithCommission() {
        return operation.isCommissionEnabled() ?
               operation.getBaseAmount().add(operation.getCode() == OperationCode.SELL ? operation.getCommission() : operation.getCommission().negate()) :
               operation.getBaseAmount();
    }

    public String next() {
        return identificationRequired() ? "select-person" : "next";
    }

    public String cancel() {
        return "cancel";
    }
}
