/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import web.entity.core.Department;
import web.entity.crm.Person;
import web.entity.dict.ReportTemplate_;
import web.entity.dict.ReportType;
import web.entity.ps.Recipient;
import web.entity.ps.TransferOperation;
import web.repository.crm.AddressRepository;
import web.repository.crm.DocumentRepository;
import web.repository.dict.CountryRepository;
import web.repository.dict.ReportTemplateRepository;
import web.service.crm.ContactService;
import web.service.crm.report.context.address.Address;
import web.service.crm.report.context.document.Document;
import web.service.crm.report.context.ps.Currency;
import web.service.crm.report.context.ps.payment.PaymentTransferContext;
import web.service.pat.payment.PaymentTransferRequestData;
import web.service.pat.payment.SendingCancelData;
import web.service.pat.payment.SendingConfirmData;
import web.service.pat.payment.PaymentTransferData;
import web.view.transfer.send.PaymentSystemFee;
import web.service.report.MsReportService;
import web.session.UserSession;
import web.utils.Addresses;
import web.utils.Contacts;

public class StepSixTransferView extends
        AbstractStepSixView<PaymentSystemFee, PaymentTransferRequestData, PaymentTransferData, SendingConfirmData,
                SendingCancelData> {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ContactService contactService;

    @Autowired(required = false)
    private MsReportService msReportService;

    @Autowired
    private UserSession userSession;

    @Override
    protected PaymentSystemFee createPaymentSystemFee() {
        return new PaymentSystemFee();
    }

    @Override
    protected BigDecimal getConversion() {
        return BigDecimal.ONE.divide(getTransferData().getRate(), 4, BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    protected List<Pair<String, String>> getPaymentSystemReports() throws Exception {
        PaymentTransferContext paymentTransferContext = new PaymentTransferContext();
        PaymentTransfer<PaymentSystemFee, PaymentTransferData> paymentTransfer = getPaymentTransfer();
        Recipient recipient = paymentTransfer.getRecipient();
        paymentTransferContext.setReceiverLastName(recipient.getLastname());
        paymentTransferContext.setReceiverFirstName(recipient.getFirstname());
        paymentTransferContext.setReceiverPatronymic(recipient.getPatronymic());
        paymentTransferContext.setReceiverCitizenship(countryRepository.findOne(recipient.getCitizenship()).getName());
        TransferOperation transferOperation = paymentTransfer.getTransferOperation();
        web.entity.dict.Currency currency = transferOperation.getCurrency();
        paymentTransferContext.setAcceptedCurrency(new Currency(currency.getName(), currency.getIntegralCase(), currency.getFractionCase()));
        currency = transferOperation.getTransferCurrency();
        paymentTransferContext.setIssuanceCurrency(new Currency(currency.getName(), currency.getIntegralCase(), currency.getFractionCase()));
        PaymentSystemFee paymentSystemFee = getPaymentSystemFee();
        paymentTransferContext.setAcceptedAmount(transferOperation.getAmount().subtract(paymentSystemFee.getCommission()));
        paymentTransferContext.setIssuanceAmount(transferOperation.getTransferAmount());
        paymentTransferContext.setExchangeRate(paymentSystemFee.getConversion());
        paymentTransferContext.setDestinationCountry(paymentTransfer.getDestinationCountry().getName());
        paymentTransferContext.setDestinationRegion(paymentTransfer.getDestinationRegion().getName());
        Department senderDepartment = userSession.getUser().getDepartment();
        paymentTransferContext.setSenderPaymentPoint(senderDepartment.getName());
        Person sender = paymentTransfer.getSender();
        paymentTransferContext.setSenderFirstName(sender.getFirstname());
        paymentTransferContext.setSenderLastName(sender.getLastname());
        paymentTransferContext.setSenderPatronymic(sender.getPatronymic());
        paymentTransferContext.setSenderBirthDate(sender.getBirthDate());
        paymentTransferContext.setSenderCitizenship(countryRepository.findOne(sender.getCitizenship()).getName());
        paymentTransferContext.setSenderPhone(contactService.findMainOrAnyOtherContact(sender, Contacts.MOBILE_PHONE_TYPE).getData());
        paymentTransferContext.setAdditionalInfo("");
        web.entity.crm.Document senderDocument = documentRepository.findMain(sender);
        paymentTransferContext.setDocument(
                new Document(senderDocument.getType(), senderDocument.getSeries(), senderDocument.getNumber(), senderDocument.getIssuanceUnit(),
                             senderDocument.getIssuanceUnitCode(), senderDocument.getIssuanceDate(), senderDocument.getValidUntilDate()));
        paymentTransferContext.setSenderAddress(new Address(addressRepository.findByPersonAndType(sender, Addresses.STAYING_TYPE)));
        paymentTransferContext.setTransferNumber(transferOperation.getNumber());
        paymentTransferContext.setAmount(transferOperation.getAmount());
        paymentTransferContext.setTransferDate(transferOperation.getDate().toLocalDate());
        paymentTransferContext.setCommission(paymentSystemFee.getCommission());
        if (msReportService == null) {
            throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF report generation requires Windows with MS Word installed.");
        }
        // Use generic payment report template if available, otherwise skip
        web.entity.dict.ReportTemplate template = reportTemplateRepository.findAll().stream()
                .filter(t -> t.getSystemName() != null && (t.getSystemName() == ReportType.PAYMENTS 
                        || t.getSystemName() == ReportType.PAYMENT_TRANSFER_PAYOUT 
                        || t.getSystemName() == ReportType.PAYMENT_TRANSFER_PAYMENT))
                .findFirst()
                .orElse(null);
        if (template == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Pair.of("Transfer application", msReportService.build(template.getFile(),
                                                                                               FacesContext.getCurrentInstance().getViewRoot()
                                                                                                           .getLocale(), senderDepartment.getZoneId(),
                                                                                               paymentTransferContext)));
    }

    @Override
    protected SendingConfirmData buildSendingConfirmRequest() {
        SendingConfirmData sendingConfirmData = new SendingConfirmData();
        sendingConfirmData.setDepartmentCode(getPaymentTransfer().getDepartmentCode());
        sendingConfirmData.setControlNumber(getPaymentTransfer().getTransferOperation().getNumber());
        return sendingConfirmData;
    }

    @Override
    protected SendingCancelData buildSendingCancelRequest() {
        SendingCancelData sendingCancelData = new SendingCancelData();
        sendingCancelData.setDepartmentCode(getPaymentTransfer().getDepartmentCode());
        sendingCancelData.setControlNumber(getPaymentTransfer().getTransferOperation().getNumber());
        return sendingCancelData;
    }
}
