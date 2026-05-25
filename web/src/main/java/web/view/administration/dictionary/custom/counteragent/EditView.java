/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.component.StringValueConverter;
import web.component.ValueConverter;
import web.entity.dict.Account;
import web.entity.dict.Account_;
import web.entity.dict.Bank;
import web.entity.dict.Bank_;
import web.entity.dict.Counteragent;
import web.entity.dict.CounteragentCommission;
import web.entity.dict.CounteragentCommission_;
import web.entity.dict.CounteragentPayAction;
import web.entity.dict.CounteragentPayAction_;
import web.entity.dict.Counteragent_;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.PayAction;
import web.entity.dict.PayAction_;
import web.entity.dict.PurposeMacros;
import web.repository.dict.AccountRepository;
import web.repository.dict.BankRepository;
import web.repository.dict.CounteragentCommissionRepository;
import web.repository.dict.CounteragentPayActionRepository;
import web.repository.dict.CounteragentRepository;
import web.repository.dict.PayActionRepository;
import web.session.UserSession;
import web.view.Message;
import web.view.converter.AutoCompletePojoConverter;

@Getter
@Setter
@Log4j2
public class EditView implements Message, Serializable {

    @Autowired
    private CounteragentRepository counteragentRepository;

    @Autowired
    private CounteragentPayActionRepository counteragentPayActionRepository;

    @Autowired
    private CounteragentCommissionRepository counteragentCommissionRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PayActionRepository payActionRepository;

    @Autowired
    private UserSession userSession;

    private DictionaryParameter dictionary;

    private Counteragent counteragent;

    private CounteragentPayAction editedPayAction;

    private CounteragentPayAction oldEditedPayAction;

    private List<PayActionItem> payActionItems;

    private List<CounteragentPayAction> deletingPayActions;

    private boolean newVersion;

    private Bank bank;

    private Long maxVersion;

    private String purpose;

    private List<CounteragentCommission> deletingCommissions;

    private List<LocalDate> copyCommissionDates;

    private LocalDate selectedCommissionDate;

    private LocalDate actualCommissionDate;

    private LocalDate copyCommissionDate;

    private LocalDate copyCommissionFromDate;

    private LocalDate now;

    private boolean commissionAdding;

    private boolean copyCommissions;

    private boolean startCopy;

    private CommissionItem editedCommission;

    private CommissionItem oldEditedCommission;

    private Map<LocalDate, List<CommissionItem>> commissionItems;

    private AutoCompletePojoConverter<Bank> bankConverter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), bank -> String.valueOf(bank.getId()));

    private AutoCompletePojoConverter<Account> accountConverter = new AutoCompletePojoConverter<>(Collections.emptyList(), Account::getId);

    private AutoCompletePojoConverter<PayAction> payActionConverter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), payAction -> String.valueOf(payAction.getId()));

    private ValueConverter payActionValueConverter = new StringValueConverter();

    public void init() {
        if (counteragent.getRoutingNumber() != null) {
            bank = bankRepository.findOne((root, query, cb) -> cb.equal(root.get(Bank_.routingNumber), counteragent.getRoutingNumber()));
            bankConverter.setSource(Collections.singletonList(bank));
        }
        maxVersion = (counteragent.getEin() == null) ? 0L : counteragentRepository.findMaxVersion(counteragent.getEin()).get();
        commissionItems = new TreeMap<>(Comparator.reverseOrder());
        if (counteragent.getId() != null) {
            payActionItems = counteragentPayActionRepository
                    .findAll((root, query, cb) -> cb.equal(root.get(CounteragentPayAction_.counteragent), counteragent)).stream()
                    .map(PayActionItem::new).collect(Collectors.toList());
            counteragentCommissionRepository.findAll((root, query, cb) -> cb.equal(root.get(CounteragentCommission_.counteragent), counteragent))
                                            .forEach(commission -> {
                                                if (!commissionItems.containsKey(commission.getDate())) {
                                                    commissionItems.put(commission.getDate(), new ArrayList<>());
                                                }
                                                Predicate<PayActionItem> findItemPredicate = item -> item.getPayAction().getName()
                                                                                                         .equals(commission.getCounteragentPayAction()
                                                                                                                           .getName()) &&
                                                                                                     item.getPayAction().getType()
                                                                                                         .equals(commission.getCounteragentPayAction()
                                                                                                                           .getType()) &&
                                                                                                     item.getPayAction().getCashSymbol()
                                                                                                         .equals(commission.getCounteragentPayAction()
                                                                                                                           .getCashSymbol());
                                                PayActionItem payActionItem =
                                                        payActionItems.stream().filter(findItemPredicate).findFirst().orElse(null);
                                                commissionItems.get(commission.getDate()).add(new CommissionItem(commission, payActionItem));
                                            });
        } else {
            payActionItems = new ArrayList<>();
        }
        deletingPayActions = new ArrayList<>();
        deletingCommissions = new ArrayList<>();
        now = LocalDate.now(userSession.getUser().getDepartment().getZoneId());
        actualCommissionDate =
                commissionItems.keySet().stream().filter(date -> date.isBefore(now) || date.isEqual(now)).sorted(Comparator.reverseOrder())
                               .findFirst().orElse(now);
        selectedCommissionDate = actualCommissionDate;
        startCopy = false;
    }

    public void addCounteragentPayAction() {
        CounteragentPayAction payAction = new CounteragentPayAction();
        payAction.setCounteragent(counteragent);
        payAction.setMain(false);
        payActionItems.add(new PayActionItem(payAction));
    }

    public void onPayActionStartEdit(RowEditEvent event) {
        editedPayAction = ((PayActionItem) event.getObject()).getPayAction();
        BeanUtils.copyProperties(editedPayAction, oldEditedPayAction = new CounteragentPayAction());
        if (editedPayAction.getAccount() != null) {
            accountConverter.setSource(Collections.singletonList(editedPayAction.getAccount()));
        }
    }

    public void onPayActionCancelEdit() {
        BeanUtils.copyProperties(oldEditedPayAction, editedPayAction);
        if (editedPayAction.getType() == null && editedPayAction.getName() == null && editedPayAction.getCashSymbol() == null) {
            payActionItems.removeIf(
                    payActionItem -> (payActionItem.getPayAction().getType() == null && payActionItem.getPayAction().getName() == null &&
                                      payActionItem.getPayAction().getCashSymbol() == null));
        }
    }

    public void onPayActionRowDelete(int index) {
        CounteragentPayAction payAction = payActionItems.get(index).getPayAction();
        if (commissionItems.values().stream().flatMap(Collection::stream).filter(commission -> commission.getPayActionItem().getPayAction() != null)
                           .anyMatch(commission -> commission.getPayActionItem().getPayAction().getCashSymbol().equals(payAction.getCashSymbol()) &&
                                                   commission.getPayActionItem().getPayAction().getType().equals(payAction.getType()) &&
                                                   commission.getPayActionItem().getPayAction().getName().equals(payAction.getName()))) {
            addErrorMessage("Cannot delete, as it is used in one of the commissions");
        } else {
            payActionItems.remove(index);
            if (payAction.getId() != null) {
                deletingPayActions.add(payAction);
            }
        }
    }

    public boolean hasMainPayAction() {
        long count = payActionItems.stream().filter(item -> item.getPayAction().getMain()).count();
        return editedPayAction.getMain() ? count > 1 : count > 0;
    }

    public Collection<Account> completeAccount(String account) {
        accountConverter.setSource(accountRepository.findAll((root, query, cb) -> cb.like(root.get(Account_.id), "%" + account + "%")));
        return accountConverter.getSource();
    }

    public void onPayActionSelect(SelectEvent event) {
        PayAction payAction = ((PayAction) event.getObject());
        editedPayAction.setName(payAction.getName());
        editedPayAction.setCashSymbol(payAction.getCashSymbol());
        editedPayAction.setType(payAction.getType());
    }

    public Collection<PayAction> completePayAction(String name) {
        payActionConverter.setSource(payActionRepository.findAll((root, query, cb) -> cb.like(root.get(PayAction_.name), "%" + name + "%")));
        payActionConverter.getSource().removeAll(payActionConverter.getSource().stream().filter(action -> payActionItems.stream().anyMatch(
                item -> action.getName().equals(item.getPayAction().getName()))).collect(Collectors.toList()));
        return payActionConverter.getSource();
    }

    public Collection<Bank> completeName(String nameOrRoutingNumber) {
        bankConverter.setSource(bankRepository.findAll((root, query, cb) -> cb
                .or(cb.like(root.get(Bank_.name), "%" + nameOrRoutingNumber + "%"), cb.like(root.get(Bank_.routingNumber), "%" + nameOrRoutingNumber + "%"))));
        return bankConverter.getSource();
    }

    public void addCounteragentCommission() {
        commissionAdding = true;
        CounteragentCommission commission = new CounteragentCommission();
        commission.setCounteragent(counteragent);
        if (selectedCommissionDate == null) {
            selectedCommissionDate = now;
        }
        commission.setDate(selectedCommissionDate);
        if (!commissionItems.containsKey(commission.getDate())) {
            commissionItems.put(commission.getDate(), new ArrayList<>());
        }
        commissionItems.get(commission.getDate()).add(new CommissionItem(commission, null));
    }

    public void onCommissionConfirmEdit() {
        long count = 0;
        Predicate<CommissionItem> equalCommissionsPredicate =
                item -> (item.getCommission().getDate().equals(editedCommission.getCommission().getDate()) &&
                         item.getCommission().getValueRange().compareTo(editedCommission.getCommission().getValueRange()) == 0 &&
                         ((item.getPayActionItem().getPayAction() == null && editedCommission.getPayActionItem().getPayAction() == null) ||
                          (item.getPayActionItem().getPayAction().getType().equals(editedCommission.getPayActionItem().getPayAction().getType()) &&
                           item.getPayActionItem().getPayAction().getCashSymbol()
                               .equals(editedCommission.getPayActionItem().getPayAction().getCashSymbol()) &&
                           item.getPayActionItem().getPayAction().getName().equals(editedCommission.getPayActionItem().getPayAction().getName()))));
        if (commissionItems.containsKey(editedCommission.getCommission().getDate()) &&
            !commissionItems.get(editedCommission.getCommission().getDate()).isEmpty()) {
            count = commissionItems.get(editedCommission.getCommission().getDate()).stream().filter(equalCommissionsPredicate).count();
        }
        if (count > 1) {
            if (commissionAdding) {
                commissionItems.get(editedCommission.getCommission().getDate()).remove(editedCommission);
                if (commissionItems.get(editedCommission.getCommission().getDate()).isEmpty()) {
                    commissionItems.remove(editedCommission.getCommission().getDate());
                }
            } else {
                BeanUtils.copyProperties(oldEditedCommission, editedCommission);
            }
            addErrorMessage("Commission for this date, payment operation type and payment amount already exists.");
        } else if (commissionAdding) {
            commissionItems.get(selectedCommissionDate).removeIf(equalCommissionsPredicate);
            if (commissionItems.get(selectedCommissionDate).isEmpty()) {
                commissionItems.remove(selectedCommissionDate);
            }
            if (!commissionItems.containsKey(editedCommission.getCommission().getDate())) {
                commissionItems.put(editedCommission.getCommission().getDate(), new ArrayList<>());
            }
            commissionItems.get(editedCommission.getCommission().getDate()).add(editedCommission);
            actualCommissionDate =
                    commissionItems.keySet().stream().filter(date -> date.isBefore(now) || date.isEqual(now)).sorted(Comparator.reverseOrder())
                                   .findFirst().orElse(commissionItems.keySet().iterator().next());
        }
        commissionAdding = false;
    }

    public void onCommissionRowDelete(int index) {
        CounteragentCommission commission = commissionItems.get(selectedCommissionDate).remove(index).getCommission();
        if (commission.getId() != null) {
            deletingCommissions.add(commission);
        }
        actualCommissionDate =
                commissionItems.keySet().stream().filter(date -> date.isBefore(now) || date.isEqual(now)).sorted(Comparator.reverseOrder())
                               .findFirst().orElse(now);
    }

    public void onCommissionStartEdit(RowEditEvent event) {
        editedCommission = (CommissionItem) event.getObject();
        BeanUtils.copyProperties(editedCommission, oldEditedCommission = new CommissionItem());
    }

    public void onCommissionCancelEdit() {
        commissionAdding = false;
        BeanUtils.copyProperties(oldEditedCommission, editedCommission);
        if (editedCommission.getPayActionItem().getPayAction() == null && editedCommission.getCommission().getValueRange() == null &&
            editedCommission.getCommission().getPercentage() == null && editedCommission.getCommission().getFixed() == null) {
            commissionItems.get(editedCommission.getCommission().getDate()).removeIf(
                    item -> (item.getCommission().getCounteragentPayAction() == null && item.getCommission().getValueRange() == null &&
                             item.getCommission().getPercentage() == null && item.getCommission().getFixed() == null));
            if (commissionItems.get(editedCommission.getCommission().getDate()).isEmpty()) {
                commissionItems.remove(editedCommission.getCommission().getDate());
            }
        }
    }

    public void copyCommission() {
        if (copyCommissions) {
            commissionItems.put(copyCommissionDate, commissionItems.get(copyCommissionFromDate).stream().map(item -> {
                CounteragentCommission newComission = new CounteragentCommission();
                newComission.setCounteragent(counteragent);
                newComission.setValueRange(item.getCommission().getValueRange());
                newComission.setPercentage(item.getCommission().getPercentage());
                newComission.setFixed(item.getCommission().getFixed());
                newComission.setMin(item.getCommission().getMin());
                newComission.setMax(item.getCommission().getMax());
                newComission.setCounteragentPayAction(item.getCommission().getCounteragentPayAction());
                newComission.setDate(copyCommissionDate);
                PayActionItem payActionItem = payActionItems.stream().filter(action -> action.getPayAction().getName()
                                                                                             .equals(newComission.getCounteragentPayAction()
                                                                                                                 .getName()) &&
                                                                                       action.getPayAction().getType()
                                                                                             .equals(newComission.getCounteragentPayAction()
                                                                                                                 .getType()) &&
                                                                                       action.getPayAction().getCashSymbol()
                                                                                             .equals(newComission.getCounteragentPayAction()
                                                                                                                 .getCashSymbol())).findFirst()
                                                            .orElse(null);
                return new CommissionItem(newComission, payActionItem);
            }).collect(Collectors.toList()));
        } else {
            commissionItems.put(copyCommissionDate, new ArrayList<>());
        }
        selectedCommissionDate = copyCommissionDate;
        copyCommissionDate = null;
        copyCommissionFromDate = null;
        copyCommissions = false;
        actualCommissionDate =
                commissionItems.keySet().stream().filter(date -> date.isBefore(now) || date.isEqual(now)).sorted(Comparator.reverseOrder())
                               .findFirst().orElse(now);
        startCopy = false;
    }

    public void cancelCopyCommission() {
        copyCommissionDate = null;
        copyCommissionFromDate = null;
        copyCommissions = false;
        startCopy = false;
    }

    public void updateCopyCommissionDates() {
        copyCommissionDates = commissionItems.keySet().stream().filter(date -> date.isBefore(copyCommissionDate)).collect(Collectors.toList());
    }

    @Transactional
    public String save() {
        try {
            if (bank.getRoutingNumber() != null) {
                counteragent.setRoutingNumber(bank.getRoutingNumber());
            }
            if (counteragent.getDate() == null) {
                counteragent.setDate(LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()));
            }
            if (counteragent.getUser() == null) {
                counteragent.setUser(userSession.getUser());
            }
            if (isNewVersion()) {
                Counteragent counteragent = new Counteragent();
                BeanUtils.copyProperties(this.counteragent, counteragent, Counteragent_.id.getName());
                counteragent.setVersion(maxVersion + 1);
                counteragentRepository.save(counteragent);
            } else {
                if (counteragent.getVersion() == null) {
                    counteragent.setVersion(1L);
                }
                counteragentRepository.save(counteragent);
            }
            counteragentPayActionRepository.delete(deletingPayActions);
            counteragentPayActionRepository.save(payActionItems.stream().map(PayActionItem::getPayAction).collect(Collectors.toList()));
            counteragentCommissionRepository.delete(deletingCommissions);
            counteragentCommissionRepository.save(commissionItems.values().stream().flatMap(Collection::stream).map(item -> {
                item.getCommission().setCounteragentPayAction(item.getPayActionItem().getPayAction());
                return item.getCommission();
            }).collect(Collectors.toList()));
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }

    public void makePurpose() {
        if (counteragent.getPurposeTemplate() != null) {
            purpose = counteragent.getPurposeTemplate().replace("#" + PurposeMacros.PARTNER_NAME.name() + "#",
                                                                counteragent.getName() == null ? "#PARTNER_NAME#" : counteragent.getName());
            PayActionItem mainPayActionItem = payActionItems.stream().filter(item -> item.getPayAction().getMain()).findFirst().orElse(null);
            CounteragentPayAction mainPayAction = mainPayActionItem != null ? mainPayActionItem.getPayAction() : null;
            String name = mainPayAction == null ? "#OPERATION#" : mainPayAction.getName();
            String vat = (mainPayAction == null || mainPayAction.getVat() == null) ? "#VAT#" : mainPayAction.getVat() + "%";
            purpose = purpose.replace("#" + PurposeMacros.OPERATION.name() + "#", name).replace("#" + PurposeMacros.VAT.name() + "#", vat)
                             .replace("#" + PurposeMacros.CLIENT_FIO.name() + "#", "John Smith")
                             .replace("#" + PurposeMacros.AMOUNT.name() + "#", "1000.00");
        }
    }
}
