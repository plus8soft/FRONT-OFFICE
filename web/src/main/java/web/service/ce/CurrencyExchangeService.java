/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.ce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.configuration.Settings;
import web.entity.ce.CurrencyOperation;
import web.entity.ce.DealStatus;
import web.entity.ce.Order;
import web.entity.ce.Order_;
import web.entity.ce.Rate;
import web.entity.core.Department;
import web.entity.core.User;
import web.entity.crm.Person;
import web.entity.dict.Account;
import web.entity.dict.AccountLink;
import web.entity.dict.AccountLinkType;
import web.entity.dict.AccountLink_;
import web.entity.dict.Account_;
import web.entity.dict.Currency_;
import web.entity.log.OperationCode;
import web.entity.log.PersonHistory;
import web.repository.back.BackException;
import web.repository.ce.CurrencyOperationRepository;
import web.repository.ce.OrderRepository;
import web.repository.dict.AccountLinkRepository;
import web.repository.log.PersonHistoryRepository;
import web.service.back.CurrencyExchangeBackService;
import web.service.ce.order.OrderService;

@Service
public class CurrencyExchangeService {

    private static final String NATIONAL_CURRENCY_CODE = "840"; // USD

    @Autowired
    private AccountLinkRepository accountLinkRepository;

    @Autowired
    private CurrencyOperationRepository currencyOperationRepository;

    @Autowired
    private PersonHistoryRepository personHistoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CurrencyExchangeBackService currencyExchangeBackService;

    @Autowired
    private Settings settings;

    @Autowired
    private OrderService orderService;

    public BigDecimal receiveAccountRest(User user, String currencyId) {
        return currencyExchangeBackService
                .receiveAccountRest(user.getLogin(), user.getDepartment().getExternalId(), findCash(user, currencyId).getId(),
                                    LocalDate.now(user.getDepartment().getZoneId()));
    }

    @Transactional
    public Account findCash(User user, String currencyId) {
        LocalDateTime now = LocalDateTime.now(user.getDepartment().getZoneId());
        boolean timeBasedNightly = now.toLocalTime().isAfter(user.getDepartment().getEndOperationDay()) ||
                                   now.toLocalTime().isBefore(user.getDepartment().getStartOperationDay());
        final boolean nightly = timeBasedNightly || (settings.isBackEnabled() && !currencyExchangeBackService
                .isWorkday(user.getLogin(), user.getDepartment().getExternalId(), now.toLocalDate()));
        if (nightly && !user.getDepartment().isNightCash()) {
            throw new BackException("Night cash operation is not configured");
        }
        AccountLink accountLink = accountLinkRepository.findOne((root, query, cb) -> cb
                .and(cb.isTrue(root.get(AccountLink_.account).get(Account_.enabled)),
                     cb.equal(root.get(AccountLink_.account).get(Account_.currency).get(Currency_.id), currencyId),
                     cb.equal(root.get(AccountLink_.department), user.getDepartment()), cb.equal(root.get(AccountLink_.nightly), nightly),
                     cb.equal(root.get(AccountLink_.type), AccountLinkType.CURRENCY_EXCHANGE.name()),
                     cb.lessThanOrEqualTo(root.get(AccountLink_.openDate), now),
                     cb.or(cb.isNull(root.get(AccountLink_.closeDate)), cb.greaterThan(root.get(AccountLink_.closeDate), now))));
        if (accountLink == null) {
            throw new BackException("Department cash account is not configured for this currency");
        }
        return accountLink.getAccount();
    }

    public void saveOperation(User user, CurrencyOperation operation, Locale locale) {
        Order order = saveOperation(user, operation);
        if (!order.equals(operation.getOrder())) {
            orderService.notificationOrder(order, locale);
        }
    }

    @Transactional
    private Order saveOperation(User user, CurrencyOperation operation) {
        operation.setNumber(
                Optional.ofNullable(currencyOperationRepository.findTopByOrderByNumberDesc()).map(CurrencyOperation::getNumber).orElse(0L) + 1);
        String debetAccount = operation.getCode().equals(OperationCode.SELL) ? findCash(user, NATIONAL_CURRENCY_CODE).getId() :
                              findCash(user, operation.getCurrency().getId()).getId();
        BigDecimal debetSum = operation.getCode().equals(OperationCode.SELL) ? operation.getBaseAmount() : operation.getSum();
        String debetCurrencyId = operation.getCode().equals(OperationCode.SELL) ? NATIONAL_CURRENCY_CODE : operation.getCurrency().getId();
        String creditAccount = operation.getCode().equals(OperationCode.SELL) ? findCash(user, operation.getCurrency().getId()).getId() :
                               findCash(user, NATIONAL_CURRENCY_CODE).getId();
        BigDecimal creditSum = operation.getCode().equals(OperationCode.SELL) ? operation.getSum() : operation.getBaseAmount();
        String creditCurrencyId = operation.getCode().equals(OperationCode.SELL) ? operation.getCurrency().getId() : NATIONAL_CURRENCY_CODE;
        Long externalId = currencyExchangeBackService.processOperation(user.getLogin(), Optional.ofNullable(operation.getPersonHistory())
                                                                                                .map(PersonHistory::getPerson)
                                                                                                .map(Person::getExternalId).orElse(null),
                                                                       user.getDepartment().getExternalId(), operation.getCode(),
                                                                       operation.getDate(), operation.getRate(), debetAccount, debetSum,
                                                                       debetCurrencyId, creditAccount, creditSum, creditCurrencyId);
        operation.setExternalId(externalId);
        operation.setRegistryNumber(currencyExchangeBackService
                                            .receiveOperationData(user.getLogin(), user.getDepartment().getExternalId(), externalId,
                                                                  operation.getCode(), operation.getDate()).getNumber());
        currencyOperationRepository.save(operation);
        if (operation.getPersonHistory() != null) {
            operation.getPersonHistory().setOperation(operation);
            personHistoryRepository.save(operation.getPersonHistory());
        }
        Order order = orderRepository.findOne((root, query, cb) -> {
            root.fetch(Order_.department);
            root.fetch(Order_.user);
            root.fetch(Order_.dealUser, JoinType.LEFT);
            Fetch<Order, Order> orderCanceledFetch = root.fetch(Order_.canceled, JoinType.LEFT);
            orderCanceledFetch.fetch(Order_.department, JoinType.LEFT);
            orderCanceledFetch.fetch(Order_.user, JoinType.LEFT);
            orderCanceledFetch.fetch(Order_.dealUser, JoinType.LEFT);
            return cb.equal(root, operation.getOrder());
        });
        if (order.getDealUser() != null) {
            order.setPerformedOperationsCount(order.getPerformedOperationsCount() + 1);
            if (order.getAllowedOperationsCount().equals(order.getPerformedOperationsCount())) {
                order.setDealStatus(DealStatus.COMPLETED);
                order.setDealStatusDate(LocalDateTime.now(operation.getDepartment().getZoneId()));
                orderRepository.save(order);
                order = orderService.buildOrderFromLastNonSpecial(order);
            } else {
                orderRepository.save(order);
            }
        }
        return order;
    }

    public List<Order> saveOrders(Map<Department, List<Rate>> departmentRatesMap, User user, Locale locale) {
        List<Order> orders = saveOrders(departmentRatesMap, user);
        orders.forEach(order -> orderService.notificationOrder(order, locale));
        return orders;
    }

    @Transactional
    private List<Order> saveOrders(Map<Department, List<Rate>> departmentRatesMap, User user) {
        if (!settings.isBackEnabled()) {
            return departmentRatesMap.entrySet().stream()
                    .map(entry -> orderService.save(user, entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
        return departmentRatesMap.entrySet().stream().map(entry -> {
            Order order = orderService.save(user, entry.getKey(), entry.getValue());
            if (2001L == entry.getKey().getExternalId()) {
                currencyExchangeBackService.installRates(user.getLogin(), entry.getValue());
            }
            return order;
        }).collect(Collectors.toList());
    }
}
