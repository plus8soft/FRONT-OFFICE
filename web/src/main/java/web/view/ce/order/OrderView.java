/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.ce.Order;
import web.entity.ce.Order_;
import web.entity.ce.Rate;
import web.entity.ce.RateType;
import web.entity.core.Department;
import web.repository.ce.OrderRepository;
import web.repository.ce.RateRepository;
import web.repository.dict.ExtRateRepository;
import web.service.ce.order.OrderService;
import web.service.report.ReportService;
import web.session.UserSession;
import web.view.Message;
import web.view.ce.item.RateItem;

@Getter
@Setter
@Log4j2
public class OrderView implements Message, Serializable {

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private ExtRateRepository extRateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserSession userSession;

    private String reportId;

    private Department selectedDepartment;

    private Set<Department> departments;

    private Order selectedOrder;

    private List<Order> orders;

    private DateRange dateRange;

    private LocalDate customDate;

    private LocalDate customPeriodStartDate;

    private LocalDate customPeriodEndDate;

    private List<RateItem> rateItems;

    private List<String> ruleNames;

    private String selectedRuleName;

    public void init() {
        departments = userSession.getDepartments();
        selectedDepartment = userSession.getUser().getDepartment();
        dateRange = DateRange.TODAY;
        onChangeRange();
    }

    public void onChangeRange() {
        LocalDateTime start;
        LocalDateTime end;
        switch (dateRange) {
            case TODAY:
                customDate = customPeriodStartDate = customPeriodEndDate = null;
                start = LocalDate.now(selectedDepartment.getZoneId()).atStartOfDay();
                end = LocalDate.now(selectedDepartment.getZoneId()).atTime(LocalTime.MAX);
                break;
            case YESTERDAY:
                customDate = customPeriodStartDate = customPeriodEndDate = null;
                start = LocalDate.now(selectedDepartment.getZoneId()).minusDays(1).atStartOfDay();
                end = LocalDate.now(selectedDepartment.getZoneId()).minusDays(1).atTime(LocalTime.MAX);
                break;
            case DAY_BEFORE_YESTERDAY:
                customDate = customPeriodStartDate = customPeriodEndDate = null;
                start = LocalDate.now(selectedDepartment.getZoneId()).minusDays(2).atStartOfDay();
                end = LocalDate.now(selectedDepartment.getZoneId()).minusDays(2).atTime(LocalTime.MAX);
                break;
            case THIS_WEEK:
                customDate = customPeriodStartDate = customPeriodEndDate = null;
                start = LocalDate.now(selectedDepartment.getZoneId()).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                end = LocalDate.now(selectedDepartment.getZoneId()).atTime(LocalTime.MAX);
                break;
            case PREVIOUS_WEEK:
                customDate = customPeriodStartDate = customPeriodEndDate = null;
                start = LocalDate.now(selectedDepartment.getZoneId()).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                 .with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).atStartOfDay();
                end = start.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
                break;
            case CUSTOM_DATE:
                customPeriodStartDate = customPeriodEndDate = null;
                customDate = LocalDate.now(selectedDepartment.getZoneId());
                start = customDate.atStartOfDay();
                end = customDate.atTime(LocalTime.MAX);
                break;
            case CUSTOM_PERIOD:
                customDate = null;
                customPeriodStartDate = LocalDate.now(selectedDepartment.getZoneId());
                customPeriodEndDate = LocalDate.now(selectedDepartment.getZoneId());
                start = customPeriodStartDate.atStartOfDay();
                end = customPeriodEndDate.atTime(LocalTime.MAX);
                break;
            default:
                throw new RuntimeException();
        }
        findOrders(start, end);
    }

    public void onChangeCustomDate() {
        if (customDate != null) {
            findOrders(customDate.atStartOfDay(), customDate.atTime(LocalTime.MAX));
        }
    }

    public void onChangeCustomPeriod() {
        if (customPeriodStartDate != null && customPeriodEndDate != null) {
            findOrders(customPeriodStartDate.atStartOfDay(), customPeriodEndDate.atTime(LocalTime.MAX));
        }
    }

    private void findOrders(LocalDateTime start, LocalDateTime end) {
        orders = orderRepository.findAll((root, query, cb) -> {
            root.fetch(Order_.department);
            root.fetch(Order_.user);
            root.fetch(Order_.dealUser, JoinType.LEFT);
            Fetch<Order, Order> orderCanceledFetch = root.fetch(Order_.canceled, JoinType.LEFT);
            orderCanceledFetch.fetch(Order_.department, JoinType.LEFT);
            orderCanceledFetch.fetch(Order_.user, JoinType.LEFT);
            orderCanceledFetch.fetch(Order_.dealUser, JoinType.LEFT);
            return cb.and(cb.equal(root.get(Order_.department), selectedDepartment), cb.between(root.get(Order_.date), start, end));
        });
        selectedOrder = null;
    }

    public void onChangeOrder() {
        List<Rate> rates = rateRepository.findFetchCurrencyByOrder(selectedOrder);
        rateItems = rates.stream().sorted(Comparator.comparing(Rate::getRulePosition).thenComparing(Rate::getCurrencyPosition)).map(rate -> {
            RateItem rateItem = new RateItem();
            rateItem.setRate(rate);
            BigDecimal externalRate = rate.getExternalRate() != null ? rate.getExternalRate().setScale(2, BigDecimal.ROUND_HALF_EVEN) : BigDecimal.ZERO;
            rateItem.setDifferenceSell(rate.getSellRate().subtract(externalRate));
            rateItem.setDifferenceBuy(rate.getBuyRate().subtract(externalRate));
            LocalDateTime localDateTime =
                    selectedOrder.getDate().atZone(selectedOrder.getDepartment().getZoneId()).toInstant().atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
            rateItem.setMarketRate(
                    extRateRepository.findTopByCurrencyAndDateLessThanEqualAndTypeOrderByDateDesc(rate.getCurrency(), localDateTime, RateType.MARKET));
            return rateItem;
        }).collect(Collectors.toList());
        ruleNames = rateItems.stream().map(rateItem -> rateItem.getRate().getRuleName()).distinct().collect(Collectors.toList());
    }

    public void print() {
        reportId =
                reportService.buildReport(orderService.buildOrderReport(selectedOrder, FacesContext.getCurrentInstance().getViewRoot().getLocale()));
    }

    public void send() {
        orderService.notificationOrder(selectedOrder, FacesContext.getCurrentInstance().getViewRoot().getLocale());
        addInfoMessage("Order sent.");
    }

    public void closeReport(CloseEvent event) {
        reportId = null;
        ((Dialog) event.getComponent()).setVisible(true);
    }
}
