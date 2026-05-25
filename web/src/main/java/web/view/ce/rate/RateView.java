/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.rate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.DealStatus;
import web.entity.ce.DepartmentCurrency;
import web.entity.ce.DepartmentCurrency_;
import web.entity.ce.Order;
import web.entity.ce.Rate;
import web.entity.ce.RateType;
import web.entity.ce.Rate_;
import web.entity.ce.Rule;
import web.entity.ce.RuleParameter;
import web.entity.ce.RuleParameter_;
import web.entity.ce.Rule_;
import web.entity.ce.Sign;
import web.entity.core.Department;
import web.entity.core.Group_;
import web.entity.core.Role_;
import web.entity.core.Task_;
import web.entity.core.User;
import web.entity.core.User_;
import web.entity.dict.Currency;
import web.entity.dict.ExtRate;
import web.entity.log.OperationCode;
import web.repository.back.BackException;
import web.repository.ce.DepartmentCurrencyRepository;
import web.repository.ce.OrderRepository;
import web.repository.ce.RateRepository;
import web.repository.ce.RuleParameterRepository;
import web.repository.core.DepartmentRepository;
import web.repository.core.UserRepository;
import web.repository.dict.ExtRateRepository;
import web.service.ce.CurrencyExchangeService;
import web.service.ce.order.OrderService;
import web.service.dict.rate.ExtRateService;
import web.service.report.ReportService;
import web.session.UserSession;
import web.view.DefaultTree;
import web.view.Message;
import web.view.ce.MergeProperty;
import web.view.ce.item.DepartmentItem;
import web.view.ce.item.RateItem;
import web.view.ce.item.RuleItem;

@Getter
@Setter
@Log4j2
public class RateView implements MergeProperty, Message, Serializable, DefaultTree {

    private static final String CURRENCY_EXCHANGE_TASK_NAME = "menu-single-window-currency-exchange";

    private static final String UNSELECTABLE_NODE_TYPE = "unselectable";

    private static final Integer CASHIER = 7;

    @Autowired
    private DepartmentCurrencyRepository departmentCurrencyRepository;

    @Autowired
    private RuleParameterRepository ruleParameterRepository;

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private ExtRateRepository extRateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ExtRateService extRateService;

    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private OrderService orderService;

    private TreeNode departmentTree;

    private TreeNode[] selectedDepartments;

    private Map<Department, List<DepartmentCurrency>> departmentCurrencyIndex;

    private List<RateItem> rateItems;

    private List<Rule> rules;

    private boolean applyRuleExternal;

    private boolean applyRuleMarket;

    private boolean edited;

    private String reportId;

    private Order dealOrder;

    private List<RateItem> dealRateItems;

    private List<User> dealRateUsers;

    private boolean dealEdited;

    private User dealUser;

    private boolean dealRate;

    private DealModel dealModel;

    private int activeTabIndex;

    private Rule selectedRule;

    private Integer allowedOperationsCount;

    @Transactional
    public void init() {
        Set<Department> selectableDepartments = new LinkedHashSet<>(userSession.getDepartments());
        List<Department> departments = userSession.getDepartmentsGraph();
        departmentCurrencyIndex =
                departmentCurrencyRepository.findAll((root, query, cb) -> root.get(DepartmentCurrency_.department).in(departments)).stream()
                                            .collect(Collectors.groupingBy(DepartmentCurrency::getDepartment));
        departments.forEach(department -> {
            if (!departmentCurrencyIndex.containsKey(department)) {
                departmentCurrencyIndex.put(department, new ArrayList<>());
                selectableDepartments.remove(department);
            }
        });
        List<Order> orders = orderRepository.findLastOrdersByDepartments(departments);
        buildTree(departmentTree = new DefaultTreeNode(), departmentItem -> departmentItem.getDepartment().getParent(), DepartmentItem::getDepartment,
                  null, departments.stream().map(department -> {
                    DepartmentItem departmentItem = new DepartmentItem();
                    departmentItem.setDepartment(department);
                    departmentItem.setOrder(orders.stream().filter(order -> order.getDepartment().equals(department)).findFirst().orElse(null));
                    return departmentItem;
                }).collect(Collectors.toList()), selectableDepartments);
        applyRuleExternal = true;
        applyRuleMarket = true;
    }

    public void select() {
        onChangeDepartmentTree();
        if (selectedDepartments.length == 1) {
            setUnselectableTree(((DepartmentItem) selectedDepartments[0].getData()).getDepartment().getZoneId());
        }
    }

    public void unselect() {
        onChangeDepartmentTree();
        if (selectedDepartments.length == 0) {
            setSelectableTree();
        }
    }

    public void selectAllChilds() {
        String rowKey = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowKey");
        streamTree(departmentTree).filter(treeNode -> rowKey.equals(treeNode.getRowKey()) && !treeNode.isLeaf()).findFirst().ifPresent(treeNode -> {
            ZoneId zoneId = ((DepartmentItem) treeNode.getData()).getDepartment().getZoneId();
            if (selectedDepartments.length == 0) {
                treeNode.getChildren().stream()
                        .filter(node -> node.isSelectable() && zoneId.equals(((DepartmentItem) node.getData()).getDepartment().getZoneId()))
                        .forEach(node -> node.setSelected(true));
                if (treeNode.getChildren().stream().anyMatch(node -> node.isSelectable() && node.isSelected())) {
                    setUnselectableTree(zoneId);
                }
            } else if (zoneId.equals(((DepartmentItem) selectedDepartments[0].getData()).getDepartment().getZoneId())) {
                treeNode.getChildren().stream().filter(TreeNode::isSelectable).forEach(node -> node.setSelected(true));
            }
            int oldLength = selectedDepartments.length;
            selectedDepartments = getSelected(streamTree(departmentTree).skip(1));
            if (oldLength != selectedDepartments.length) {
                onChangeDepartmentTree();
            }
        });
    }

    public void deselectAllChilds() {
        if (selectedDepartments.length != 0) {
            String rowKey = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowKey");
            streamTree(departmentTree).filter(treeNode -> rowKey.equals(treeNode.getRowKey()) && !treeNode.isLeaf()).findFirst()
                                      .ifPresent(treeNode -> {
                                          ZoneId zoneId = ((DepartmentItem) treeNode.getData()).getDepartment().getZoneId();
                                          if (zoneId.equals(((DepartmentItem) selectedDepartments[0].getData()).getDepartment().getZoneId())) {
                                              treeNode.getChildren().stream().filter(TreeNode::isSelected).forEach(node -> node.setSelected(false));
                                              int oldLength = selectedDepartments.length;
                                              selectedDepartments = getSelected(streamTree(departmentTree).skip(1));
                                              if (oldLength != selectedDepartments.length) {
                                                  onChangeDepartmentTree();
                                                  if (selectedDepartments.length == 0) {
                                                      setSelectableTree();
                                                  }
                                              }
                                          }
                                      });
        }
    }

    @Transactional
    private void onChangeDepartmentTree() {
        dealUser = null;
        dealOrder = null;
        allowedOperationsCount = null;
        dealRateUsers = null;
        dealRateItems = new ArrayList<>();
        dealModel = null;
        dealRate = false;
        activeTabIndex = 0;
        buildCommonRates();
        if (selectedDepartments.length == 1) {
            updateDealRates();
        }
    }

    public void buildCommonRates() {
        edited = false;
        if (selectedDepartments.length != 0) {
            List<Department> selectedDepartments = mapTree(this.selectedDepartments, DepartmentItem::getDepartment);
            Comparator<Pair<Rule, Currency>> comparator = Comparator.<Pair<Rule, Currency>, Integer>comparing(pair -> pair.getFirst().getPosition())
                    .thenComparing(pair -> pair.getSecond().getPosition());
            Map<Pair<Rule, Currency>, List<RuleParameter>> ruleParameterUnion = ruleParameterUnion(selectedDepartments);
            rateItems = ruleParameterUnion.keySet().stream().sorted(comparator).map(pair -> {
                RateItem rateItem = new RateItem();
                rateItem.setRuleItem(new RuleItem());
                rateItem.getRuleItem().setRule(pair.getFirst());
                rateItem.setCurrency(pair.getSecond());
                rateItem.setExternalRate(findExtRate(pair.getSecond(), RateType.EXTERNAL));
                rateItem.setRatioConflict(
                        Objects.nonNull(rateItem.getExternalRate()) && !pair.getSecond().getRatio().equals(rateItem.getExternalRate().getRatio()));
                rateItem.setMarketRate(findExtRate(pair.getSecond(), RateType.MARKET));
                mergeRuleParams(ruleParameterUnion.get(pair), rateItem.getRuleItem());
                mergeRates(selectedDepartments.stream().map(department -> {
                    Order order = orderRepository.findTopByDepartmentAndDealUserIsNullOrderByIdDesc(department);
                    return Objects.nonNull(order) ? rateRepository
                            .findByCurrencyAndDepartmentAndOrderAndRuleName(rateItem.getCurrency(), department, order,
                                                                            rateItem.getRuleItem().getRule().getName()) : null;
                }).filter(Objects::nonNull).collect(Collectors.toList()), rateItem);
                return rateItem;
            }).collect(Collectors.toList());
        } else {
            rateItems = new ArrayList<>();
        }
        rules = rateItems.stream().map(rateItem -> rateItem.getRuleItem().getRule()).distinct().sorted(Comparator.comparing(Rule::getPosition))
                         .collect(Collectors.toList());
    }

    public void onCancel() {
        dealUser = null;
        dealEdited = false;
        allowedOperationsCount = null;
        Department department = ((DepartmentItem) selectedDepartments[0].getData()).getDepartment();
        dealRateItems = buildCurrentDealRates(orderRepository.findTopByDepartmentAndDealUserIsNullOrderByIdDesc(department), department);
    }

    private List<RateItem> buildCurrentDealRates(Order order, Department department) {
        List<Rate> rates = rateRepository.findAll((root, query, cb) -> {
            root.fetch(Rate_.currency);
            return cb.and(cb.equal(root.get(Rate_.department), department), cb.equal(root.get(Rate_.order), order));
        });
        return rates.stream().collect(Collectors.groupingBy(Rate::getCurrency)).entrySet().stream().map(entry -> {
            Currency currency = entry.getKey();
            RateItem rateItem = new RateItem();
            rateItem.setCurrency(currency);
            rateItem.setExternalRate(findExtRate(currency, RateType.EXTERNAL));
            rateItem.setMarketRate(findExtRate(currency, RateType.MARKET));
            rateItem.setRate(new Rate());
            List<Rate> currencyRates = entry.getValue();
            rateItem.getRate().setSellRate(currencyRates.stream().map(Rate::getSellRate).min(BigDecimal::compareTo).get());
            rateItem.getRate().setBuyRate(currencyRates.stream().map(Rate::getBuyRate).max(BigDecimal::compareTo).get());
            return rateItem;
        }).sorted(Comparator.comparing(rateItem -> rateItem.getCurrency().getPosition())).collect(Collectors.toList());
    }

    private void updateDealRates() {
        dealRateItems = rateItems.stream().collect(Collectors.groupingBy(RateItem::getCurrency)).entrySet().stream().map(entry -> {
            RateItem rateItem = new RateItem();
            Currency currency = entry.getKey();
            rateItem.setCurrency(currency);
            List<RateItem> rateItems = entry.getValue();
            rateItem.setExternalRate(rateItems.get(0).getExternalRate());
            rateItem.setMarketRate(rateItems.get(0).getMarketRate());
            rateItem.setRate(new Rate());
            List<RateItem> currencyRateItems = entry.getValue();
            rateItem.getRate().setSellRate(
                    currencyRateItems.stream().map(RateItem::getRate).map(Rate::getSellRate).filter(Objects::nonNull).min(BigDecimal::compareTo)
                                     .orElse(BigDecimal.ZERO));
            rateItem.getRate().setBuyRate(
                    currencyRateItems.stream().map(RateItem::getRate).map(Rate::getBuyRate).filter(Objects::nonNull).max(BigDecimal::compareTo)
                                     .orElse(BigDecimal.ZERO));
            return rateItem;
        }).sorted(Comparator.comparing(rateItem -> rateItem.getCurrency().getPosition())).collect(Collectors.toList());
        Department department = ((DepartmentItem) selectedDepartments[0].getData()).getDepartment();
        dealRateUsers = userRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            return cb.and(cb.notEqual(root.get(User_.status), "LOCKED"), cb.equal(root.get(User_.position), CASHIER),
                          cb.or(cb.equal(root.join(User_.tasks, JoinType.LEFT).get(Task_.systemName), CURRENCY_EXCHANGE_TASK_NAME),
                                cb.equal(root.join(User_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT).get(Task_.systemName),
                                         CURRENCY_EXCHANGE_TASK_NAME), cb.equal(
                                          root.join(User_.groups, JoinType.LEFT).join(Group_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT)
                                              .get(Task_.systemName), CURRENCY_EXCHANGE_TASK_NAME)),
                          cb.equal(root.get(User_.department), department));
        }, new Sort(User_.lastname.getName(), User_.firstname.getName()));
        dealModel = new DealModel(department);
    }

    private void setSelectableTree() {
        String defaultNodeType = "default";
        streamTree(departmentTree).skip(1).filter(node -> node.getType().equals(UNSELECTABLE_NODE_TYPE)).forEach(node -> {
            node.setSelectable(true);
            node.setType(defaultNodeType);
        });
    }

    private void setUnselectableTree(ZoneId zoneId) {
        streamTree(departmentTree).skip(1).filter(node -> node.isSelectable() &&
                                                          !zoneId.equals(((DepartmentItem) node.getData()).getDepartment().getZoneId()))
                                  .forEach(node -> {
                                      node.setSelectable(false);
                                      node.setType(UNSELECTABLE_NODE_TYPE);
                                  });
    }

    private Map<Pair<Rule, Currency>, List<RuleParameter>> ruleParameterUnion(List<Department> departments) {
        List<RuleParameter> ruleParameters = ruleParameterRepository.findAll((root, query, cb) -> {
            root.fetch(RuleParameter_.department);
            root.fetch(RuleParameter_.rule);
            root.fetch(RuleParameter_.currency);
            return cb.and(cb.isTrue(root.get(RuleParameter_.enabled)), cb.isTrue(root.get(RuleParameter_.rule).get(Rule_.enabled)),
                          cb.isTrue(root.get(RuleParameter_.rule).get(Rule_.currency)), root.get(RuleParameter_.department).in(departments));
        });
        return ruleParameters.stream().collect(Collectors.groupingBy(ruleParameter -> Pair.of(ruleParameter.getRule(), ruleParameter.getCurrency())));
    }

    public void applyExternalRate() {
        rateItems.forEach(rateItem -> applyExtRate(rateItem, rateItem.getExternalRate(), applyRuleExternal));
    }

    public void applyMarketRate() {
        rateItems.forEach(rateItem -> applyExtRate(rateItem, rateItem.getMarketRate(), applyRuleMarket));
    }

    private void applyExtRate(RateItem rateItem, ExtRate extRate, boolean applyRule) {
        if (Objects.nonNull(extRate)) {
            if (!rateItem.getRuleItem().isSellConflict() && (Objects.nonNull(rateItem.getRuleItem().getRuleParameter().getSellPercent()) ||
                                                             Objects.nonNull(rateItem.getRuleItem().getRuleParameter().getSellValue())) ||
                !applyRule) {
                rateItem.getRate().setSellRate(applyRule ? applyRule(extRate, rateItem.getRuleItem().getRuleParameter(), OperationCode.SELL) :
                                               extRate.getSellRate().setScale(2, BigDecimal.ROUND_HALF_UP));
                rateItem.setSellConflict(false);
            }
            if (!rateItem.getRuleItem().isBuyConflict() && (Objects.nonNull(rateItem.getRuleItem().getRuleParameter().getBuyPercent()) ||
                                                            Objects.nonNull(rateItem.getRuleItem().getRuleParameter().getBuyValue())) || !applyRule) {
                rateItem.getRate().setBuyRate(applyRule ? applyRule(extRate, rateItem.getRuleItem().getRuleParameter(), OperationCode.BUY) :
                                              extRate.getBuyRate().setScale(2, BigDecimal.ROUND_HALF_UP));
                rateItem.setBuyConflict(false);
            }
        }
    }

    private BigDecimal applyRule(ExtRate extRate, RuleParameter ruleParam, OperationCode operationCode) {
        BigDecimal rate = OperationCode.SELL.equals(operationCode) ? extRate.getSellRate() : extRate.getBuyRate();
        Sign sign = OperationCode.SELL.equals(operationCode) ? ruleParam.getSellSign() : ruleParam.getBuySign();
        BigDecimal value = OperationCode.SELL.equals(operationCode) ? ruleParam.getSellValue() : ruleParam.getBuyValue();
        value = Sign.MINUS.equals(sign) && Objects.nonNull(value) ? value.negate() : value;
        BigDecimal percent = OperationCode.SELL.equals(operationCode) ? ruleParam.getSellPercent() : ruleParam.getBuyPercent();
        percent = Sign.MINUS.equals(sign) && Objects.nonNull(percent) ? percent.negate() : percent;
        return Objects.nonNull(value) ? rate.add(value).setScale(2, RoundingMode.HALF_UP) :
               rate.multiply(BigDecimal.ONE.add(percent.divide(BigDecimal.valueOf(100)))).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public String getOrderUpdateTime(Order order) {
        Duration duration = Duration.between(order.getDate(), LocalDateTime.now(order.getDepartment().getZoneId()));
        return duration.isNegative() ? "prepared" : MessageFormat
                .format("{0} d. {1}", duration.toDays(), LocalTime.MIDNIGHT.plus(duration).format(DateTimeFormatter.ofPattern("HH h. mm min.")));
    }

    /**
     * Updates external currency rates from stub service.
     * 
     * Note: Currently uses stub implementation that returns empty data.
     * To enable real rate fetching, implement ExternalRateService with actual API integration.
     */
    public void updateExternalRates() {
        try {
            extRateService.loadExternalRates();
            rateItems.forEach(rateItem -> rateItem.setExternalRate(findExtRate(rateItem.getCurrency(), RateType.EXTERNAL)));
            if (dealRateItems != null) {
                dealRateItems.forEach(rateItem -> rateItem.setExternalRate(findExtRate(rateItem.getCurrency(), RateType.EXTERNAL)));
            }
            addInfoMessage("Rates updated successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Service temporarily unavailable.");
        }
    }

    /**
     * Updates market currency rates from stub service.
     * 
     * Note: Currently uses stub implementation that returns hardcoded test data.
     * You can implement your own API integration for market rates.
     */
    public void updateMarketRates() {
        try {
            extRateService.loadMarketRates();
            rateItems.forEach(rateItem -> rateItem.setMarketRate(findExtRate(rateItem.getCurrency(), RateType.MARKET)));
            if (dealRateItems != null) {
                dealRateItems.forEach(rateItem -> rateItem.setMarketRate(findExtRate(rateItem.getCurrency(), RateType.MARKET)));
            }
            addInfoMessage("Rates updated successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Service temporarily unavailable.");
        }
    }

    private ExtRate findExtRate(Currency currency, RateType rateType) {
        LocalDateTime localDateTime = RateType.EXTERNAL.equals(rateType) ?
                                      LocalDate.now(mapTree(selectedDepartments, DepartmentItem::getDepartment).get(0).getZoneId())
                                               .atTime(23, 59, 59) : LocalDateTime.now();
        return extRateRepository.findTopByCurrencyAndDateLessThanEqualAndTypeOrderByDateDesc(currency, localDateTime, rateType);
    }

    public LocalDateTime getLatestExternalInstant() {
        return rateItems.stream().map(rateItem -> Optional.ofNullable(rateItem.getExternalRate()).map(ExtRate::getDate)).filter(Optional::isPresent)
                        .map(Optional::get).sorted().findFirst().orElse(null);
    }

    /**
     * Gets the latest market rate date.
     * 
     * Note: Currently returns current date/time as stub implementation doesn't fetch real data.
     */
    public Instant getLatestMarketInstant() {
        return rateItems.stream().filter(rateItem -> Objects.nonNull(rateItem.getMarketRate()))
                        .sorted(Comparator.comparing(o -> o.getMarketRate().getDate())).findFirst()
                        .map(rateItem -> rateItem.getMarketRate().getDate().toInstant(ZoneOffset.UTC)).orElse(null);
    }

    public void saveView() {
        List<FacesMessage> messages = validateView();
        if (!messages.isEmpty()) {
            messages.stream().limit(5).forEach(message -> addMessage(null, message));
        } else {
            try {
                List<Department> departments = mapTree(selectedDepartments, DepartmentItem::getDepartment);
                List<Order> orders =
                        currencyExchangeService.saveOrders(departments.stream().collect(Collectors.toMap(Function.identity(), department -> {
                            List<Currency> currencies = departmentCurrencyIndex.get(department).stream().map(DepartmentCurrency::getCurrency)
                                                                               .collect(Collectors.toList());
                            List<RuleParameter> ruleParameters = ruleParameterRepository.findAll((root, query, cb) -> cb
                                    .and(cb.equal(root.get(RuleParameter_.department), department), cb.isTrue(root.get(RuleParameter_.enabled)),
                                         cb.isTrue(root.get(RuleParameter_.rule).get(Rule_.enabled)),
                                         cb.isTrue(root.get(RuleParameter_.rule).get(Rule_.currency))));
                            return rateItems.stream().filter(rateItem -> ruleParameters.stream().anyMatch(
                                    ruleParameter -> rateItem.getCurrency().equals(ruleParameter.getCurrency()) &&
                                                     rateItem.getRuleItem().getRule().equals(ruleParameter.getRule()))).map(rateItem -> {
                                Rate rate = new Rate();
                                BeanUtils.copyProperties(rateItem.getRate(), rate, Rate_.id.getName());
                                rate.setDepartment(department);
                                rate.setCurrency(rateItem.getCurrency());
                                rate.setCurrencyPosition(rateItem.getCurrency().getPosition());
                                rate.setOperationCurrencyPosition(currencies.indexOf(rateItem.getCurrency()));
                                rate.setRuleName(rateItem.getRuleItem().getRule().getName());
                                rate.setRulePosition(rateItem.getRuleItem().getRule().getPosition());
                                rate.setMin(rateItem.getRuleItem().getRule().getMin());
                                rate.setMax(rateItem.getRuleItem().getRule().getMax());
                                rate.setRatio(rateItem.getCurrency().getRatio());
                                rate.setExternalRate(rateItem.getExternalRate() != null ? rateItem.getExternalRate().getBuyRate() : null);
                                return rate;
                            }).collect(Collectors.toList());
                        })), userSession.getUser(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
                Stream.of(selectedDepartments).forEach(treeNode -> ((DepartmentItem) treeNode.getData()).setOrder(
                        orders.stream().filter(order -> order.getDepartment().equals(((DepartmentItem) treeNode.getData()).getDepartment()))
                              .findFirst().get()));
                edited = false;
                reportId =
                        reportService.buildReport(orderService.buildOrderReport(orders, FacesContext.getCurrentInstance().getViewRoot().getLocale()));
                addInfoMessage("Data saved successfully.");
            } catch (BackException e) {
                log.error(e.getMessage(), e);
                addErrorMessage(e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addErrorMessage("Internal error while saving data.");
            }
        }
    }

    private List<FacesMessage> validateView() {
        List<FacesMessage> result = new ArrayList<>();
        rateItems.forEach(rateItem -> {
            if (rateItem.isRatioConflict()) {
                result.add(errorMessageFormated("Category \"{0}\". Currency {1}. Central Bank currency denomination differs from currency dictionary denomination",
                                                rateItem.getRuleItem().getRule().getName(), rateItem.getCurrency().getIso()));
            }
            if (rateItem.isSellConflict()) {
                result.add(
                        errorMessageFormated("Category \"{0}\". Currency {1}. Conflict in \"Sell\" field", rateItem.getRuleItem().getRule().getName(),
                                             rateItem.getCurrency().getIso()));
            }
            if (rateItem.isBuyConflict()) {
                result.add(
                        errorMessageFormated("Category \"{0}\". Currency {1}. Conflict in \"Buy\" field", rateItem.getRuleItem().getRule().getName(),
                                             rateItem.getCurrency().getIso()));
            }
            if (Objects.isNull(rateItem.getRate().getSellRate())) {
                result.add(errorMessageFormated("Category \"{0}\". Currency {1}. \"Sell\" field is required",
                                                rateItem.getRuleItem().getRule().getName(), rateItem.getCurrency().getIso()));
            }
            if (Objects.isNull(rateItem.getRate().getBuyRate())) {
                result.add(errorMessageFormated("Category \"{0}\". Currency {1}. \"Buy\" field is required",
                                                rateItem.getRuleItem().getRule().getName(), rateItem.getCurrency().getIso()));
            }
            if (Objects.isNull(rateItem.getExternalRate())) {
                result.add(errorMessageFormated("Category \"{0}\". Currency {1}. External rate is not set", rateItem.getRuleItem().getRule().getName(),
                                                rateItem.getCurrency().getIso()));
            }
        });
        departmentRepository.findAll((root, query, cb) -> {
            Subquery<RuleParameter> subquery = query.subquery(RuleParameter.class);
            Root<RuleParameter> ruleParameterRoot = subquery.from(RuleParameter.class);
            subquery.select(ruleParameterRoot)
                    .where(cb.equal(ruleParameterRoot.get(RuleParameter_.department), root), cb.isTrue(ruleParameterRoot.get(RuleParameter_.enabled)),
                           cb.isTrue(ruleParameterRoot.get(RuleParameter_.rule).get(Rule_.enabled)),
                           cb.isTrue(ruleParameterRoot.get(RuleParameter_.rule).get(Rule_.currency)));
            return cb.and(root.in(mapTree(this.selectedDepartments, DepartmentItem::getDepartment)), cb.not(cb.exists(subquery)));
        }).forEach(department -> result.add(errorMessageFormated("Department \"{0}\". Conversion rates are not configured", department.getName())));
        return result;
    }

    public void saveDealRates() {
        List<FacesMessage> messages = validateDealRates();
        if (!messages.isEmpty()) {
            messages.stream().limit(5).forEach(message -> addMessage(null, message));
        } else {
            try {
                DepartmentItem departmentItem = (DepartmentItem) selectedDepartments[0].getData();
                dealOrder = saveRates(departmentItem.getDepartment());
                departmentItem.setOrder(dealOrder);
                orderService.notificationOrder(dealOrder, FacesContext.getCurrentInstance().getViewRoot().getLocale());
                dealEdited = false;
                reportId = reportService
                        .buildReport(orderService.buildOrderReport(dealOrder, FacesContext.getCurrentInstance().getViewRoot().getLocale()));
                dealModel = new DealModel(departmentItem.getDepartment());
                addInfoMessage("Data saved successfully.");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addErrorMessage("Internal error while saving data.");
            }
        }
    }

    private List<FacesMessage> validateDealRates() {
        List<FacesMessage> result = new ArrayList<>();
        dealRateItems.forEach(rateItem -> {
            if (Objects.isNull(rateItem.getRate().getSellRate()) || rateItem.getRate().getSellRate().equals(BigDecimal.ZERO)) {
                result.add(errorMessageFormated("Currency {0}. \"Sell\" field is required", rateItem.getCurrency().getIso()));
            }
            if (Objects.isNull(rateItem.getRate().getBuyRate()) || rateItem.getRate().getBuyRate().equals(BigDecimal.ZERO)) {
                result.add(errorMessageFormated("Currency {0}. \"Buy\" field is required", rateItem.getCurrency().getIso()));
            }
            if (Objects.isNull(rateItem.getExternalRate())) {
                result.add(errorMessageFormated("Currency {0}. External rate is not set", rateItem.getCurrency().getIso()));
            }
        });
        if (null ==
            orderRepository.findTopByDepartmentAndDealUserIsNullOrderByIdDesc(((DepartmentItem) selectedDepartments[0].getData()).getDepartment())) {
            result.add(errorMessage("Standard order is missing for the department"));
        }
        return result;
    }

    @Transactional
    private Order saveRates(Department department) {
        Order order = new Order();
        order.setDepartment(department);
        order.setUser(userSession.getUser());
        order.setDate(LocalDateTime.now(department.getZoneId()));
        order.setNumber(Optional.ofNullable(orderRepository.findTopByDepartmentAndDateGreaterThanEqualOrderByNumberDesc(department, LocalDate
                .now(department.getZoneId()).atStartOfDay())).map(Order::getNumber).orElse(0L) + 1);
        order.setDealStatus(DealStatus.DURING);
        order.setDealUser(dealUser);
        order.setAllowedOperationsCount(allowedOperationsCount);
        order.setPerformedOperationsCount(0);
        orderRepository.save(order);
        List<Currency> currencies =
                departmentCurrencyIndex.get(department).stream().map(DepartmentCurrency::getCurrency).collect(Collectors.toList());
        dealRateItems.stream().map(rateItem -> {
            Rate rate = new Rate();
            BeanUtils.copyProperties(rateItem.getRate(), rate, Rate_.id.getName());
            rate.setDepartment(department);
            rate.setCurrency(rateItem.getCurrency());
            rate.setCurrencyPosition(rateItem.getCurrency().getPosition());
            rate.setOperationCurrencyPosition(currencies.indexOf(rateItem.getCurrency()));
            rate.setRuleName("Rate for single transaction");
            rate.setRulePosition(0);
            rate.setMin(BigDecimal.ZERO);
            rate.setRatio(rateItem.getCurrency().getRatio());
            rate.setExternalRate(rateItem.getExternalRate() != null ? rateItem.getExternalRate().getBuyRate() : null);
            return rate;
        }).forEach(rate -> {
            rate.setOrder(order);
            rateRepository.save(rate);
        });
        return order;
    }

    public void cancelDealOrder() {
        Order order = null;
        try {
            order = orderService.cancelDeal(dealOrder, FacesContext.getCurrentInstance().getViewRoot().getLocale());
            ((DepartmentItem) selectedDepartments[0].getData()).setOrder(order);
            dealOrder = null;
            dealUser = null;
            allowedOperationsCount = null;
                addInfoMessage("Data saved successfully.");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addErrorMessage("Internal error while saving data.");
        } finally {
            Department department = ((DepartmentItem) selectedDepartments[0].getData()).getDepartment();
            dealRateItems = buildCurrentDealRates(order != null ? order : (dealOrder =
                    orderRepository.findByDepartmentAndDealUserAndDealStatus(department, dealUser, DealStatus.DURING)), department);
            dealModel = new DealModel(department);
        }
    }

    public void closeReport(CloseEvent event) {
        reportId = null;
        ((Dialog) event.getComponent()).setVisible(true);
    }

    public void changeTab(TabChangeEvent event) {
        dealRate = event.getTab().getTitle().equals("Deal Rates");
    }

    public void changeDealUser() {
        Department department = ((DepartmentItem) selectedDepartments[0].getData()).getDepartment();
        Order order = dealUser == null ? null : orderRepository.findByDepartmentAndDealUserAndDealStatus(department, dealUser, DealStatus.DURING);
        if (order != null) {
            dealOrder = order;
            allowedOperationsCount = dealOrder.getAllowedOperationsCount();
            dealRateItems = buildCurrentDealRates(order, department);
        } else if (dealOrder != null) {
            dealOrder = null;
            allowedOperationsCount = null;
            dealRateItems = buildCurrentDealRates(orderRepository.findTopByDepartmentAndDealUserIsNullOrderByIdDesc(department), department);
        }
    }
}
