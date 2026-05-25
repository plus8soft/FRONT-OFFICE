/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import web.entity.dict.ReportTemplate_;
import web.entity.dict.ReportType;
import web.entity.ps.TransferOperation;
import web.repository.dict.ReportTemplateRepository;
import web.service.crm.report.context.ps.Currency;
import web.service.crm.report.context.slip.CreditSlipContext;
import web.service.pat.AbstractSendingCancelRequest;
import web.service.pat.AbstractSendingConfirmRequest;
import web.service.pat.AbstractSendingTransferRequest;
import web.service.pat.AbstractSendingTransferResponse;
import web.service.pat.TransferException;
import web.service.pat.TransferSendingService;
import web.service.report.MsReportService;
import web.session.UserSession;
import web.utils.Utils;
import web.view.Message;

@Getter
@Setter
@Log4j2
public abstract class AbstractStepSixView<C extends AbstractPaymentSystemFee, O extends AbstractSendingTransferRequest, T extends
        AbstractSendingTransferResponse, A extends AbstractSendingConfirmRequest, B extends AbstractSendingCancelRequest>
        implements Serializable, Message {

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private TransferSendingService<O, T, A, B> transferSendingService;

    @Autowired(required = false)
    private MsReportService reportService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private Utils utils;

    private PaymentTransfer<C, T> paymentTransfer;

    private T transferData;

    private C paymentSystemFee;

    private List<Pair<String, String>> reports = new ArrayList<>();

    private boolean showReport;

    public void init(PaymentTransfer<C, T> paymentTransfer) {
        this.paymentTransfer = paymentTransfer;
        transferData = paymentTransfer.getTransferData();
        TransferOperation transferOperation = paymentTransfer.getTransferOperation();
        paymentSystemFee = createPaymentSystemFee();
        paymentSystemFee.setBankCommission(transferOperation.getBankCommissionAmount());
        paymentSystemFee.setPaymentSystemCommission(transferOperation.getSystemCommissionAmount());
        paymentSystemFee.setSum(transferOperation.getAmount());
        paymentSystemFee.setCommission(transferOperation.getBankCommissionAmount().add(transferOperation.getSystemCommissionAmount()));
        if (!transferData.getTransferCurrency().equals(transferData.getAcceptedCurrency())) {
            paymentSystemFee.setConversion(getConversion());
        }
        print();
    }

    protected abstract C createPaymentSystemFee();

    protected abstract BigDecimal getConversion();

    protected abstract List<Pair<String, String>> getPaymentSystemReports() throws Exception;

    private void print() {
        try {
            reports.addAll(getPaymentSystemReports());
            CreditSlipContext context = new CreditSlipContext();
            context.setDepartmentCode(userSession.getUser().getDepartment().getUnitCode());
            context.setDepartmentName(userSession.getUser().getDepartment().getName());
            context.setNumber(paymentTransfer.getTransferOperation().getExternalId());
            context.setAmount(paymentTransfer.getTransferOperation().getAmount());
            web.entity.dict.Currency currency = paymentTransfer.getAcceptedCurrency();
            context.setCurrency(new Currency(currency.getName(), currency.getIntegralCase(), currency.getFractionCase()));
            context.setDate(paymentTransfer.getTransferOperation().getDate());
            context.setPersonFirstname(paymentTransfer.getSender().getFirstname());
            context.setPersonLastname(paymentTransfer.getSender().getLastname());
            context.setPersonPatronymic(paymentTransfer.getSender().getPatronymic());
            if (reportService == null) {
                throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF report generation requires Windows with MS Word installed.");
            }
            reports.add(Pair.of("Credit slip", reportService.build(reportTemplateRepository.findOne(
                    (root, query, cb) -> cb.equal(root.get(ReportTemplate_.systemName), ReportType.CREDIT_SLIP)).getFile(),
                                                                       FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                                                                       userSession.getUser().getDepartment().getZoneId(), context)));
            showReport = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while generating report");
        }
    }

    protected abstract A buildSendingConfirmRequest();

    public String next() {
        try {
            transferSendingService.confirmSendingTransfer(transferData.getTransferOperation(), buildSendingConfirmRequest());
            return "next";
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
            return null;
        }
    }

    protected abstract B buildSendingCancelRequest();

    public String back() {
        try {
            transferSendingService.cancelSendingTransfer(transferData.getTransferOperation(), buildSendingCancelRequest());
            getPaymentTransfer().setTransferOperation(null);
            getPaymentTransfer().setTransferData(null);
            return "back";
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
            return null;
        }
    }

    public String cancel() {
        try {
            transferSendingService.cancelSendingTransfer(transferData.getTransferOperation(), buildSendingCancelRequest());
            return "cancel";
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
            return null;
        }
    }

    public void closeReport(CloseEvent event) {
        showReport = false;
        ((Dialog) event.getComponent()).setVisible(true);
    }
}
