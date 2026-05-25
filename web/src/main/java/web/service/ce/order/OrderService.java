/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.ce.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.configuration.Settings;
import web.entity.ce.DealStatus;
import web.entity.ce.Order;
import web.entity.ce.Order_;
import web.entity.ce.Rate;
import web.entity.ce.Rate_;
import web.entity.core.Department;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.ce.OrderRepository;
import web.repository.ce.RateRepository;
import web.repository.core.UserRepository;
import web.service.MailService;
import web.service.report.ReportService;
import web.utils.DateTimes;
import web.utils.Utils;

@Service
@Log4j2
public class OrderService {

    private static final String ATTACHMENT_FILENAME = "Order.pdf";

    private static final Integer CASHIER = 7;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private Utils utils;

    @Autowired
    private Settings settings;

    @Transactional
    public Order save(User user, Department department, List<Rate> rates) {
        Order order = new Order();
        order.setDepartment(department);
        order.setUser(user);
        order.setDate(LocalDateTime.now(department.getZoneId()).plusMinutes(2));
        order.setNumber(Optional.ofNullable(orderRepository.findTopByDepartmentAndDateGreaterThanEqualOrderByNumberDesc(department, LocalDate
                .now(department.getZoneId()).atStartOfDay())).map(Order::getNumber).orElse(0L) + 1);
        orderRepository.save(order);
        rates.forEach(rate -> rate.setOrder(order));
        rateRepository.save(rates);
        return order;
    }

    public Order findCurrent(User user) {
        Order order = orderRepository.findOne((root, query, cb) -> {
            root.fetch(Order_.dealUser);
            return cb.and(cb.equal(root.get(Order_.department), user.getDepartment()), cb.equal(root.get(Order_.dealUser), user),
                          cb.equal(root.get(Order_.dealStatus), DealStatus.DURING));
        });
        if (order == null) {
            order = orderRepository.findTopByDepartmentAndDealUserIsNullAndDateLessThanEqualOrderByIdDesc(user.getDepartment(), LocalDateTime
                    .now(user.getDepartment().getZoneId()));
        }
        return order;
    }

    public Consumer<Document> buildOrderReport(List<Order> orders, Locale locale) {
        DateTimeFormatter titleDateFormatter = new DateTimeFormatterBuilder().appendLiteral("dated ").appendPattern("dd ")
                                                                             .appendText(ChronoField.MONTH_OF_YEAR, DateTimes.MONTH_OF_YEAR_TEXT)
                                                                             .appendPattern(" yyyy").toFormatter(locale);
        DateTimeFormatter dateFormatter =
                new DateTimeFormatterBuilder().appendPattern("«dd» ").appendText(ChronoField.MONTH_OF_YEAR, DateTimes.MONTH_OF_YEAR_TEXT)
                                              .appendPattern(" yyyy").toFormatter(locale);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale);
        float[] columns = {1, 1, 1};
        return document -> IntStream.range(0, orders.size()).forEach(i -> {
            Order order = orders.get(i);
            if (i > 0) {
                document.add(new AreaBreak());
            }
            Table buyRateTable = new Table(columns);
            Table sellRateTable = new Table(columns);
            Table externalRateTable = new Table(columns);
            List<Rate> rates = rateRepository.findFetchCurrencyByOrder(order);
            rates.stream().collect(Collectors.groupingBy(Rate::getRulePosition, () -> new TreeMap<>(Comparator.naturalOrder()), Collectors
                    .toCollection(() -> new TreeSet<>(Comparator.comparing(Rate::getCurrencyPosition))))).forEach((position, ruleRates) -> {
                Rate firstRate = ruleRates.first();
                String groupName = firstRate.getMax() == null ? String.format(locale, "for amounts above %,.2f", firstRate.getMin()) :
                                   String.format(locale, "for amounts from %,.2f to %,.2f", firstRate.getMin(), firstRate.getMax());
                buyRateTable.addCell(new Cell(1, 3).setPaddingLeft(5).setBold().setBackgroundColor(new DeviceRgb(224, 224, 224)).add(groupName));
                sellRateTable.addCell(new Cell(1, 3).setPaddingLeft(5).setBold().setBackgroundColor(new DeviceRgb(224, 224, 224)).add(groupName));
                ruleRates.forEach(rate -> {
                    String name = rate.getCurrency().getName();
                    String ratio = "USD per " + rate.getRatio() + " currency units";
                    buyRateTable.addCell(new Cell().add(name).setPaddingLeft(5).setPaddingRight(5))
                                .addCell(new Cell().add(String.format(locale, "%,.2f", rate.getBuyRate())).setPaddingLeft(5).setPaddingRight(5))
                                .addCell(new Cell().add(ratio).setPaddingLeft(5).setPaddingRight(5));
                    sellRateTable.addCell(new Cell().add(name).setPaddingLeft(5).setPaddingRight(5))
                                 .addCell(new Cell().add(String.format(locale, "%,.2f", rate.getSellRate())).setPaddingLeft(5).setPaddingRight(5))
                                 .addCell(new Cell().add(ratio).setPaddingLeft(5).setPaddingRight(5));
                });
            });
            rates.stream().filter(rate -> rate.getExternalRate() != null)
                 .map(rate -> new ExternalRate(rate.getCurrencyPosition(), rate.getCurrency().getName(), rate.getExternalRate(), rate.getRatio()))
                 .distinct().sorted(Comparator.comparing(ExternalRate::getCurrencyPosition)).forEach(
                    externalRate -> externalRateTable.addCell(new Cell().add(externalRate.getCurrencyName()).setPaddingLeft(5).setPaddingRight(5)).addCell(
                            new Cell().add(String.format(locale, "%,.4f", externalRate.getRate())).setPaddingLeft(5).setPaddingRight(5)).addCell(
                            new Cell().add("USD per " + externalRate.getRatio() + " currency units").setPaddingLeft(5).setPaddingRight(5)));
            LocalDateTime date = order.getDate();
            document.add(new Paragraph("BANK").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14))
                    .add(new Paragraph("ORDER").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14))
                    .add(new Paragraph(date.format(titleDateFormatter)).setTextAlignment(TextAlignment.CENTER)).add(new Paragraph(String.format(
                    "Set from %s year from %s hours the following buy-sell rates for foreign currency cash in the branch",
                    date.format(dateFormatter), date.format(timeFormatter))).setFixedLeading(15))
                    .add(new Paragraph(order.getDepartment().getFullName()).setMarginLeft(20));
            document.add(new Paragraph(utils.getAddresses().formatAddress(order.getDepartment())).setMarginLeft(20));
            document.add(new Paragraph("Buy Rates:").setBold().setUnderline());
            document.add(buyRateTable);
            document.add(new Paragraph("Sell Rates:").setBold().setUnderline());
            document.add(sellRateTable);
            document.add(new Paragraph("External Rates:").setBold().setUnderline());
            document.add(externalRateTable);
            document.add(new Paragraph(String.join("          ", order.getUser().getPositionText(), joinFio(order.getUser()))).setMarginTop(15));
        });
    }

    public Consumer<Document> buildOrderReport(Order order, Locale locale) {
        return buildOrderReport(Stream.of(order).collect(Collectors.toList()), locale);
    }

    @Async("emailTaskExecutor")
    public void notificationOrder(Order order, Locale locale) {
        List<User> users =
                order.getDealUser() != null ? Stream.of(order.getDealUser()).filter(user -> user.getEmail() != null).collect(Collectors.toList()) :
                userRepository.findAll((root, query, cb) -> cb
                        .and(cb.equal(root.get(User_.department), order.getDepartment()), cb.isNotNull(root.get(User_.email)),
                             cb.notEqual(root.get(User_.status), "LOCKED"), cb.equal(root.get(User_.position), CASHIER)));
        if (!users.isEmpty()) {
            mailService.sendMail(mimeMessageHelper -> {
                mimeMessageHelper.setSubject(
                        String.format("%4$sCurrency Exchange Rate Order No. %1$d dated %2$td.%2$tm.%2$tY %2$tR%3$s", order.getNumber(),
                                      order.getDate(), getSubject(order), settings.isProduction() ? "" : "TEST "));
                User user = order.getUser();
                mimeMessageHelper.setText(String.format("%s.\nPlease print the updated order.\n\n--------------------\n%s\n%s",
                                                        order.getDealUser() != null ? String.format("%s, new currency exchange rates have been set for the deal",
                                                                                                    joinFirstnameAndPatronymic(order.getDealUser())) :
                                                        "Colleagues, your department's currency exchange rates have changed", user.getPositionText(),
                                                        utils.getStrings()
                                                             .capitalizeFio(user.getLastname(), user.getFirstname(), user.getPatronymic())));
                mimeMessageHelper.setTo(users.stream().map(User::getEmail).toArray(String[]::new));
                User copyTo = order.getCanceled() == null ? order.getUser() : order.getCanceled().getUser();
                if (copyTo.getEmail() != null) {
                    mimeMessageHelper.setCc(copyTo.getEmail());
                }
                mimeMessageHelper
                        .addAttachment(ATTACHMENT_FILENAME, new ByteArrayResource(reportService.buildReportBytes(buildOrderReport(order, locale))));
            });
        }
    }

    private String joinFio(User user) {
        return utils.getStrings().joinFio(user.getLastname(), user.getFirstname(), user.getPatronymic());
    }

    private String joinFirstnameAndPatronymic(User user) {
        return utils.getStrings().joinFio(null, user.getFirstname(), user.getPatronymic());
    }

    private String getSubject(Order order) {
        Order canceled = order.getCanceled();
        return canceled != null ? String.format(
                ". Reverting previously set rates. Cancellation of Order No. %1$d dated %2$td.%2$tm.%2$tY %2$tR for deal %3$s. %4$s %5$s",
                canceled.getNumber(), canceled.getDate(), canceled.getDepartment().getName(), canceled.getDealUser().getPositionText(),
                joinFio(canceled.getDealUser())) : order.getDealUser() != null ?
                                                   String.format(" for deal %s. %s %s", order.getDepartment().getName(),
                                                                 order.getDealUser().getPositionText(), joinFio(order.getDealUser())) :
                                                   " for " + order.getDepartment().getName();
    }

    @Transactional
    public Order buildOrderFromLastNonSpecial(Order canceledOrder) {
        Department department = canceledOrder.getDepartment();
        Order lastOrder = orderRepository.findTopByDepartmentAndDealUserIsNullOrderByIdDesc(department);
        Order order = new Order();
        order.setDepartment(department);
        order.setUser(lastOrder.getUser());
        order.setCanceled(canceledOrder);
        order.setDate(LocalDateTime.now(department.getZoneId()).plusMinutes(1));
        order.setNumber(Optional.ofNullable(orderRepository.findTopByDepartmentAndDateGreaterThanEqualOrderByNumberDesc(department, LocalDate
                .now(department.getZoneId()).atStartOfDay())).map(Order::getNumber).orElse(0L) + 1);
        orderRepository.save(order);
        rateRepository.findFetchCurrencyByOrder(lastOrder).forEach(lastRate -> {
            Rate rate = new Rate();
            BeanUtils.copyProperties(lastRate, rate, Rate_.id.getName(), Rate_.order.getName());
            rate.setOrder(order);
            rateRepository.save(rate);
        });
        return order;
    }

    public Order cancelDeal(Order order, Locale locale) {
        Order newOrder = cancelDeal(order);
        notificationOrder(newOrder, locale);
        return newOrder;
    }

    @Transactional
    private Order cancelDeal(Order order) {
        order.setDealStatus(DealStatus.CANCELED);
        order.setDealStatusDate(LocalDateTime.now(order.getDepartment().getZoneId()));
        orderRepository.save(order);
        return buildOrderFromLastNonSpecial(order);
    }
}
