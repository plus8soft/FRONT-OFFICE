/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress_;
import web.entity.dict.ReportTemplate_;
import web.entity.dict.ReportType;
import web.entity.ps.TransferOperation;
import web.repository.back.BackException;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.crm.PersonRepository;
import web.repository.dict.ReportTemplateRepository;
import web.service.crm.ContactService;
import web.service.crm.report.context.address.Address;
import web.service.crm.report.context.document.Document;
import web.service.crm.report.context.ps.AbstractPayoutContext;
import web.service.crm.report.context.ps.Currency;
import web.service.crm.report.context.slip.DebitSlipContext;
import web.service.pat.AbstractReceivingConfirmRequest;
import web.service.pat.AbstractReceivingTransferRequest;
import web.service.pat.AbstractReceivingTransferResponse;
import web.service.pat.AbstractSendingTransferResponse;
import web.service.pat.TransferException;
import web.service.pat.TransferReceivingService;
import web.service.report.MsReportService;
import web.session.UserSession;
import web.utils.Addresses;
import web.utils.Contacts;
import web.view.Message;

@Getter
@Setter
@Log4j2
public abstract class AbstractStepFourView<B extends AbstractPayoutTransfer, T extends AbstractSendingTransferResponse, I extends
        AbstractReceivingTransferRequest, R extends AbstractReceivingTransferResponse, C extends AbstractReceivingConfirmRequest>
        implements Serializable, Message {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private TransferReceivingService<T, I, R, C> transferSendingService;

    @Autowired(required = false)
    private MsReportService reportService;

    @Autowired
    private UserSession userSession;

    private TransferOperation transferOperation;

    private B payoutTransfer;

    private List<Pair<String, String>> reports = new ArrayList<>();

    private boolean showReport;

    public void init(B payoutTransfer, Long recipientId) {
        try {
            this.payoutTransfer = payoutTransfer;
            this.payoutTransfer.setPerson(personRepository.findOne(recipientId));
            I payoutTransferData = createPayoutTransferData();
            payoutTransferData.setPerson(payoutTransfer.getPerson());
            payoutTransferData.setCurrency(payoutTransfer.getCurrency());
            payoutTransferData.setAmount(payoutTransfer.getAmount());
            payoutTransferData.setPaymentSystem(payoutTransfer.getPaymentSystem());
            payoutTransferData.setDepartmentCode(payoutTransfer.getDepartmentCode());
            payoutTransferData.setControlNumber(payoutTransfer.getControlNumber());
            R receivingTransferResponse = transferSendingService.blockTransfer(userSession.getUser(), payoutTransferData);
            transferOperation = receivingTransferResponse.getTransferOperation();
            handleBlockResponse(receivingTransferResponse);
            print();
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
        } catch (BackException e) {
            log.error(e.getMessage(), e);
            addErrorMessage(e.getMessage());
        }
    }

    protected abstract I createPayoutTransferData();

    protected void handleBlockResponse(R receivingTransferResponse) {
    }

    protected abstract C buildReceivingConfirmRequest();

    public String next() {
        try {
            transferSendingService.confirmReceivingTransfer(transferOperation, buildReceivingConfirmRequest());
            return "cancel";
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
            return null;
        }
    }

    protected String getCode() {
        return null;
    }

    public String back() {
        try {
            transferSendingService.cancelReceivingTransfer(transferOperation, getCode(), payoutTransfer.getDepartmentCode());
            return "back";
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
            return null;
        }
    }

    public String cancel() {
        try {
            transferSendingService.cancelReceivingTransfer(transferOperation, getCode(), payoutTransfer.getDepartmentCode());
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

    private void print() {
        try {
            ReportType reportType = getReportType();
            if (reportType != null) {
                AbstractPayoutContext context = buildReportContext();
                Person person = payoutTransfer.getPerson();
                context.setTransferNumber(payoutTransfer.getControlNumber());
                context.setIssuanceDate(LocalDate.now());
                context.setAmount(payoutTransfer.getAmount());
                web.entity.dict.Currency currency = payoutTransfer.getCurrency();
                context.setCurrency(new Currency(currency.getName(), currency.getIntegralCase(), currency.getFractionCase()));
                context.setReceiverPaymentPoint(userSession.getUser().getDepartment().getName());
                context.setReceiverLastName(person.getLastname());
                context.setReceiverFirstName(person.getFirstname());
                context.setReceiverPatronymic(person.getPatronymic());
                context.setReceiverBirthDate(person.getBirthDate());
                context.setPhone(contactService.findMainOrAnyOtherContact(person, Contacts.MOBILE_PHONE_TYPE).getData());
                context.setAddress(new Address(personAddressRepository.findOne((root, query, cb) -> cb
                        .and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), Addresses.STAYING_TYPE)))
                                                                      .getAddress()));
                web.entity.crm.Document document = documentRepository.findMain(person);
                context.setDocument(new Document(document.getType(), document.getSeries(), document.getNumber(), document.getIssuanceUnit(),
                                                 document.getIssuanceUnitCode(), document.getIssuanceDate(), document.getValidUntilDate()));
                if (reportService == null) {
                    throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF report generation requires Windows with MS Word installed.");
                }
                reports.add(Pair.of("Payout application", reportService
                        .build(reportTemplateRepository.findOne((root, query, cb) -> cb.equal(root.get(ReportTemplate_.systemName), reportType))
                                                       .getFile(), FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                               userSession.getUser().getDepartment().getZoneId(), context)));
            }
            DebitSlipContext context = new DebitSlipContext();
            context.setDepartmentCode(userSession.getUser().getDepartment().getUnitCode());
            context.setDepartmentName(userSession.getUser().getDepartment().getName());
            context.setNumber(transferOperation.getExternalId());
            context.setAmount(payoutTransfer.getAmount());
            web.entity.dict.Currency currency = payoutTransfer.getCurrency();
            context.setCurrency(new Currency(currency.getName(), currency.getIntegralCase(), currency.getFractionCase()));
            context.setDate(transferOperation.getDate());
            context.setPersonFirstname(payoutTransfer.getPerson().getFirstname());
            context.setPersonLastname(payoutTransfer.getPerson().getLastname());
            context.setPersonPatronymic(payoutTransfer.getPerson().getPatronymic());
            context.setDocument(Optional.ofNullable(documentRepository.findMain(payoutTransfer.getPerson()))
                                        .map(document -> new Document(document.getType(), document.getSeries(), document.getNumber(),
                                                                      document.getIssuanceUnit(), document.getIssuanceUnitCode(),
                                                                      document.getIssuanceDate(), document.getValidUntilDate())).orElse(null));
            if (reportService == null) {
                throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF report generation requires Windows with MS Word installed.");
            }
            reports.add(Pair.of("Debit slip", reportService.build(reportTemplateRepository.findOne(
                    (root, query, cb) -> cb.equal(root.get(ReportTemplate_.systemName), ReportType.DEBIT_SLIP)).getFile(),
                                                                       FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                                                                       userSession.getUser().getDepartment().getZoneId(), context)));
            showReport = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while generating report");
        }
    }

    protected AbstractPayoutContext buildReportContext() {
        return null;
    }

    protected ReportType getReportType() {
        return null;
    }
}
