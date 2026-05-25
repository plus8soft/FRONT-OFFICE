/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.Bank;
import web.entity.dict.Bank_;
import web.entity.dict.Counteragent;
import web.entity.dict.CounteragentCommission;
import web.entity.dict.CounteragentCommission_;
import web.entity.dict.CounteragentPayAction;
import web.entity.dict.Counteragent_;
import web.entity.dict.IdentificationRule;
import web.entity.dict.IdentificationRule_;
import web.entity.dict.PayAction;
import web.entity.log.OperationCode;
import web.repository.dict.BankRepository;
import web.repository.dict.CounteragentCommissionRepository;
import web.repository.dict.CounteragentRepository;
import web.repository.dict.IdentificationRuleRepository;
import web.repository.dict.PayActionRepository;
import web.session.UserSession;
import web.view.Message;
import web.view.converter.AutoCompletePojoConverter;

@Getter
@Setter
@Log4j2
public class StepOneView implements Serializable, Message {

    @Autowired
    private CounteragentRepository counteragentRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private IdentificationRuleRepository identificationRuleRepository;

    @Autowired
    private PayActionRepository payActionRepository;

    @Autowired
    private CounteragentCommissionRepository counteragentCommissionRepository;

    @Autowired
    private UserSession userSession;

    private WoaPayment payment;

    private boolean counteragentSelected;

    private List<CounteragentPayAction> availablePayActions = new ArrayList<>();

    private AutoCompletePojoConverter<Counteragent> counteragentConverter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), counteragent -> String.valueOf(counteragent.getId()));

    private final CounteragentEinValueConverter counteragentValueConverter = new CounteragentEinValueConverter();

    private AutoCompletePojoConverter<Bank> bankConverter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), bank -> String.valueOf(bank.getId()));

    public void init(WoaPayment payment) {
        this.payment = payment;
        if (this.payment.getCounteragent() == null) {
            this.payment.setCounteragent(new Counteragent());
        } else {
            counteragentConverter.setSource(Collections.singletonList(payment.getCounteragent()));
            counteragentValueConverter.setSource(payment.getCounteragent());
            bankConverter.setSource(Collections.singletonList(payment.getBank()));
        }
        availablePayActions.clear();
        updateAvailablePayActions();
        counteragentSelected = payment.getCounteragent().getId() != null;
    }

    private void updateAvailablePayActions() {
        availablePayActions.clear();
        if (!payment.getCounteragent().getPayActions().isEmpty()) {
            availablePayActions.addAll(payment.getCounteragent().getPayActions());
            payment.setPayAction(availablePayActions.stream().filter(CounteragentPayAction::getMain).findFirst().orElse(null));
            onPayActionSelect();
        } else {
            List<PayAction> payActions = payActionRepository.findAll();
            for (PayAction action : payActions) {
                CounteragentPayAction payAction = new CounteragentPayAction();
                payAction.setId(action.getId());
                payAction.setType(action.getType());
                payAction.setName(action.getName());
                payAction.setCashSymbol(action.getCashSymbol());
                availablePayActions.add(payAction);
            }
        }
    }

    public void updateCounteragent() {
        counteragentConverter.setSource(Collections.singletonList(payment.getCounteragent()));
    }

    public void onCounteragentSelect(SelectEvent event) {
        Counteragent selected = (Counteragent) event.getObject();
        if (selected != null && selected.getId() != null) {
            selected = counteragentRepository.findOne(selected.getId());
        }
        payment.setCounteragent(selected);
        payment.setBank(bankRepository.findOne((root, query, cb) -> cb.equal(root.get(Bank_.routingNumber), payment.getCounteragent().getRoutingNumber())));
        counteragentConverter.setSource(Collections.singletonList(payment.getCounteragent()));
        counteragentValueConverter.setSource(payment.getCounteragent());
        updateAvailablePayActions();
        bankConverter.setSource(Collections.singletonList(payment.getBank()));
        counteragentSelected = true;
    }

    public void onCounteragentChanged() {
        availablePayActions.clear();
        updateAvailablePayActions();
        counteragentSelected = false;
    }

    public Collection<Counteragent> completeEin(String ein) {
        counteragentConverter.setSource(counteragentRepository.findAll(
                (root, query, cb) -> cb.and(cb.like(root.get(Counteragent_.ein), "%" + ein + "%"), cb.isFalse(root.get(Counteragent_.disabled)))));
        return counteragentConverter.getSource();
    }

    public Collection<Bank> completeRoutingNumber(String routingNumber) {
        bankConverter.setSource(bankRepository.findAll((root, query, cb) -> cb.like(root.get(Bank_.routingNumber), "%" + routingNumber + "%")));
        return bankConverter.getSource();
    }

    public void onBankSelect() {
        if (payment.getCounteragent().getRoutingNumber() == null) {
            payment.getCounteragent().setRoutingNumber(payment.getBank().getRoutingNumber());
        }
    }

    public void onPayActionSelect() {
        if (payment.getPayAction() != null) {
            switch (payment.getPayAction().getType()) {
                case COMPANY:
                    if (payment.getCounteragent().isContract()) {
                        payment.setPaymentOperationCode(OperationCode.COMPANY_PARTNER_PAYMENT);
                    } else {
                        payment.setPaymentOperationCode(OperationCode.COMPANY_FREE_PAYMENT);
                    }
                    payment.setSenderEin(null);
                    break;
                case PUBLIC_SECTOR:
                    payment.setPaymentOperationCode(OperationCode.PUBLIC_SECTOR_PAYMENT);
                    break;
                case TAX:
                    payment.setPaymentOperationCode(OperationCode.TAXES_PAYMENT);
                    break;
            }
            payment.setVat(payment.getPayAction().getVat());
            payment.setVatSum(null);
            payment.setCounteragentCommission(null);
            payment.setTotal(null);
            payment.setAccount(payment.getPayAction().getAccount());
        }
    }

    public void calculate() {
        CounteragentCommission actualCommission = null;
        if (payment.getCounteragent().getId() != null) {
            List<CounteragentCommission> counteragentCommissions = counteragentCommissionRepository
                    .findAll((root, query, cb) -> cb.equal(root.get(CounteragentCommission_.counteragent), payment.getCounteragent()));
            List<LocalDate> counteragentCommissionDates =
                    counteragentCommissions.stream().map(CounteragentCommission::getDate).distinct().sorted(Comparator.reverseOrder())
                                           .collect(Collectors.toList());
            LocalDate now = LocalDate.now(userSession.getUser().getDepartment().getZoneId());
            LocalDate actualCommissionDate = !counteragentCommissionDates.isEmpty() ?
                                             counteragentCommissionDates.stream().filter(date -> date.isBefore(now) || date.isEqual(now))
                                                                        .sorted(Comparator.reverseOrder()).findFirst()
                                                                        .orElse(counteragentCommissionDates.get(0)) : now;
            actualCommission = counteragentCommissions.stream().filter(commission -> commission.getDate().isEqual(actualCommissionDate))
                                                      .filter(commission -> (commission.getCounteragentPayAction().getType()
                                                                                       .equals(payment.getPayAction().getType()) &&
                                                                             commission.getCounteragentPayAction().getName()
                                                                                       .equals(payment.getPayAction().getName()) &&
                                                                             commission.getCounteragentPayAction().getCashSymbol()
                                                                                       .equals(payment.getPayAction().getCashSymbol())))
                                                      .filter(commission -> (commission.getValueRange().compareTo(payment.getSum()) >= 0))
                                                      .sorted(Comparator.comparing(CounteragentCommission::getDate)).findFirst().orElse(null);
        }
        if (actualCommission == null) {
            payment.setCounteragentCommission(BigDecimal.ZERO);
        } else {
            if (actualCommission.getFixed() != null && actualCommission.getFixed().compareTo(BigDecimal.ZERO) > 0) {
                payment.setCounteragentCommission(actualCommission.getFixed());
            } else if (actualCommission.getPercentage() != null && actualCommission.getPercentage().compareTo(BigDecimal.ZERO) > 0) {
                payment.setCounteragentCommission(payment.getSum().multiply(actualCommission.getPercentage()).divide(BigDecimal.valueOf(100)));
                if (actualCommission.getMax() != null && payment.getCounteragentCommission().compareTo(actualCommission.getMax()) >= 0) {
                    payment.setCounteragentCommission(actualCommission.getMax());
                } else if (actualCommission.getMin() != null && payment.getCounteragentCommission().compareTo(actualCommission.getMin()) <= 0) {
                    payment.setCounteragentCommission(actualCommission.getMin());
                }
            }
        }
        if (payment.getVat() == null) {
            payment.setVat(0);
        }
        payment.setVatSum(payment.getSum().multiply(new BigDecimal(payment.getVat())).divide(new BigDecimal(100)));
        payment.setTotal(payment.getSum().add(payment.getCounteragentCommission()));
    }

    public void clearAll() {
        payment = new WoaPayment();
        payment.setCounteragent(new Counteragent());
        counteragentConverter.setSource(Collections.emptyList());
        counteragentValueConverter.setSource(payment.getCounteragent());
        bankConverter.setSource(Collections.emptyList());
        counteragentSelected = false;
        updateAvailablePayActions();
        addInfoMessage("Form cleared — you can start a new payment.");
    }

    public String toNextStep() {
        if (payment.getCounteragent().getId() == null) {
            payment.getCounteragent().setUser(userSession.getUser());
            payment.getCounteragent().setDate(LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()));
            payment.getCounteragent().setVersion(1L);
        }
        IdentificationRule rule =
                identificationRuleRepository.findOne((root, query, cb) -> cb.equal(root.get(IdentificationRule_.systemName), "PAY_PERS_IDENT"));
        if (rule != null && (payment.getPaymentOperationCode() == OperationCode.COMPANY_FREE_PAYMENT ||
             payment.getPaymentOperationCode() == OperationCode.COMPANY_PARTNER_PAYMENT) && payment.getTotal().compareTo(rule.getMin()) < 0) {
            return "next-simple";
        } else {
            return "next";
        }
    }
}
