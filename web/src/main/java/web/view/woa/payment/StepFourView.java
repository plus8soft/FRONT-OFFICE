/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.faces.context.FacesContext;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.crm.Address;
import web.entity.dict.ReportTemplate_;
import web.entity.dict.ReportType;
import web.entity.log.OperationType;
import web.entity.log.PersonHistory;
import web.entity.pay.Payment;
import web.repository.back.BackException;
import web.repository.crm.AddressRepository;
import web.repository.crm.PersonRepository;
import web.repository.dict.CurrencyRepository;
import web.repository.dict.ReportTemplateRepository;
import web.repository.log.PersonHistoryRepository;
import web.repository.pay.PaymentRepository;
import web.service.crm.report.context.woa.PaymentContext;
import web.service.report.MsReportService;
import web.service.report.ReportService;
import web.service.woa.payment.WoaPaymentService;
import web.session.UserSession;
import web.utils.Addresses;
import web.utils.Utils;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class StepFourView implements Serializable, Message {

    @Autowired
    private WoaPaymentService woaPaymentService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PersonHistoryRepository personHistoryRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ReportService reportService;

    @Autowired(required = false)
    private MsReportService msReportService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private Utils utils;

    private WoaPayment payment;

    private String result;

    private String reportId;

    private boolean error;

    private boolean showReport;

    public void init(WoaPayment payment) {
        this.payment = payment;
        if (payment.getPurpose() == null) {
            if (this.payment.getPerson() != null) {
                this.payment.setPurpose(utils.getStrings().joinFio(this.payment.getPerson().getLastname(), this.payment.getPerson().getFirstname(),
                                                                   this.payment.getPerson().getPatronymic()) + " " + this.payment.getSum());
            } else {
                this.payment.setPurpose(this.payment.getSum().toString());
            }
        }
        try {
            LocalDateTime time = LocalDateTime.now(userSession.getUser().getDepartment().getZoneId());
            Long transferId = woaPaymentService.transferPayment(userSession.getUser(), payment, time);
            Payment savingPayment = new Payment();
            savingPayment.setExternalId(transferId);
            savingPayment.setUser(userSession.getUser());
            savingPayment.setDepartment(userSession.getUser().getDepartment());
            savingPayment.setPayer(payment.getPerson() != null ? utils.getStrings().joinFio(payment.getPerson().getLastname(), 
                    payment.getPerson().getFirstname(), payment.getPerson().getPatronymic()) : "");
            savingPayment.setPersonHistory(new PersonHistory());
            savingPayment.getPersonHistory().setPerson(this.payment.getPerson());
            savingPayment.setDate(time);
            savingPayment.setType(OperationType.PAY);
            savingPayment.setAmount(payment.getSum());
            savingPayment.setCommission(payment.getCounteragentCommission());
            savingPayment.setVat(payment.getVat());
            savingPayment.setAccount(payment.getPayAction().getAccount().getId());
            savingPayment.setEin(payment.getCounteragent().getEin());
            savingPayment.setCounteragentName(payment.getCounteragent().getName());
            savingPayment.setCounteragentAddress(payment.getCounteragent().getAddress());
            savingPayment.setRoutingNumber(payment.getCounteragent().getRoutingNumber());
            savingPayment.setBankName(payment.getBank().getName());
            savingPayment.setCorrespondentAccount(payment.getBank().getCorrespondentAccount());
            savingPayment.setPurpose(payment.getPurpose());
            savingPayment.setCashSymbol(payment.getPayAction().getCashSymbol());
            savingPayment.setPayActionName(payment.getPayAction().getName());
            savingPayment.setPayerEin(payment.getSenderEin());
            savePayment(savingPayment);
            result = "Payment completed successfully";
            reportId = printPaymentReport(savingPayment, userSession);
            error = false;
        } catch (BackException e) {
            error = true;
            result = "Operation interrupted: " + String.format("Error code: \"%s\". Error description: \"%s\".", e.getCode(), e.getMessage());
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            error = true;
            result = "Operation interrupted: Error interacting with core banking system";
            log.error(e.getMessage(), e);
        }
    }

    @Transactional
    private void savePayment(Payment savingPayment) {
        savingPayment.getPersonHistory().setOperation(savingPayment);
        paymentRepository.save(savingPayment);
        personHistoryRepository.save(savingPayment.getPersonHistory());
    }

    public String onClose() {
        payment.setCounteragent(null);
        payment.setBank(null);
        payment.setPerson(null);
        payment.setPaymentOperationCode(null);
        payment.setPayAction(null);
        payment.setAccount(null);
        payment.setVat(null);
        payment.setPurpose(null);
        payment.setSum(null);
        payment.setCounteragentCommission(null);
        payment.setVatSum(null);
        payment.setTotal(null);
        payment.setSenderEin(null);
        return "close";
    }

    public void printReport() {
        showReport = true;
        reportId = build(payment);
    }

    private String build(WoaPayment payment) {
        try {
            String payer = payment.getPerson() == null ? "" : utils.getStrings().joinFio(payment.getPerson().getLastname(),
                                                                                                         payment.getPerson().getFirstname(),
                                                                                                         payment.getPerson().getPatronymic());
            if (payment.getPerson() != null) {
                Address personAddress = addressRepository.findByPersonAndType(payment.getPerson(), Addresses.STAYING_TYPE);
                if (personAddress == null) {
                    personAddress = addressRepository.findByPersonAndType(payment.getPerson(), Addresses.RESIDENTIAL_TYPE);
                }
                payer = payer + "//" + utils.getAddresses().formatAddress(personAddress);
            }
            String date = payment.getDocDate() == null ? "0" :
                          DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(FacesContext.getCurrentInstance().getViewRoot().getLocale())
                                           .withZone(userSession.getUser().getDepartment().getZoneId()).format(payment.getDocDate().atStartOfDay());
            PaymentContext context =
                    new PaymentContext(payment.getSenderEin(), payment.getTotal(), payer, "000", payment.getBank(), payment.getCounteragent(),
                                       payment.getAccount().getId(), payment.getPurpose());
            if (msReportService == null) {
                throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF report generation requires Windows with MS Word installed.");
            }
            web.entity.dict.ReportTemplate template = reportTemplateRepository.findOne((root, query, cb) -> cb.equal(root.get(ReportTemplate_.systemName), ReportType.PAYMENTS));
            if (template == null || template.getFile() == null) {
                throw new UnsupportedOperationException("Payment report template not found");
            }
            return msReportService
                    .build(template.getFile(), FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                           userSession.getUser().getDepartment().getZoneId(), context);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while generating report");
        }
        showReport = false;
        return null;
    }

    private String printPaymentReport(Payment operation, UserSession userSession) {
        return reportService.buildReport(document -> {
            document.setFontSize(8);
            document.add(new Paragraph("PAYMENT REPORT").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold().setMarginTop(2)
                                                         .setMarginBottom(0));
            document.add(new Paragraph(new Text("Payment for: ").setBold()).add(operation.getCounteragentName()).setWidthPercent(35)
                                                                          .setTextAlignment(TextAlignment.JUSTIFIED).setMarginTop(0)
                                                                          .setMarginBottom(0));
            document.add(new Paragraph(new Text("Amount: ").setBold()).add(operation.getAmount().toString()).setWidthPercent(35)
                                                                        .setTextAlignment(TextAlignment.JUSTIFIED).setMarginTop(0)
                                                                        .setMarginBottom(0));
            document.add(new Paragraph(new Text("Performed by: ").setBold()).add(utils.getStrings().joinFio(userSession.getUser().getLastname(),
                                                                                                          userSession.getUser().getFirstname(),
                                                                                                          userSession.getUser().getPatronymic()))
                                                                          .setWidthPercent(35).setTextAlignment(TextAlignment.JUSTIFIED)
                                                                          .setMarginTop(0).setMarginBottom(0));
        });
    }

    public void closeReport(CloseEvent event) {
        showReport = false;
        ((Dialog) event.getComponent()).setVisible(true);
    }
}
