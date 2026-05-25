/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.currencyexchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.CurrencyOperation;
import web.entity.ce.Rule;
import web.entity.crm.Contact;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress_;
import web.entity.log.AddressHistory;
import web.entity.log.PersonHistory;
import web.repository.back.BackException;
import web.repository.ce.CurrencyOperationRepository;
import web.repository.ce.RuleRepository;
import web.repository.crm.AddressRepository;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.crm.PersonRepository;
import web.service.ce.CurrencyExchangeService;
import web.service.ce.OperationReportService;
import web.service.crm.ContactService;
import web.session.UserSession;
import web.utils.Addresses;
import web.utils.Contacts;

@Configurable
@Getter
@Setter
@Log4j2
public class StepFour implements Serializable {

    @Autowired
    private CurrencyOperationRepository currencyOperationRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private OperationReportService operationReportService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserSession userSession;

    private String errorMessage;

    private CurrencyOperation operation;

    private String reportId;

    private String reportIdRequestConfirmationMoney;

    private String reportIdConsentPersonalData;

    private String reportIdMessageFinancialMonitoring;

    private boolean showReport;

    public void init(CurrencyOperation operation, Long personId) {
        try {
            this.operation = operation;
            if (personId != null) {
                operation.setPersonHistory(getPersonHistory(personId));
            }
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            currencyExchangeService.saveOperation(userSession.getUser(), operation, locale);
            reportId = operationReportService.print(operation, userSession, locale);
            if (operation.getPersonHistory() != null) {
                BigDecimal sum = currencyOperationRepository
                        .getBaseAmountByPersonAndDate(operation.getPersonHistory().getPerson(), operation.getDate().with(LocalTime.MIDNIGHT),
                                                  operation.getDate().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1));
                Rule largeSum = ruleRepository.findBySystemNameAndSystem("LARGE_SUMS_CONTROL", true);
                Rule financialMonitoringSum = ruleRepository.findBySystemNameAndSystem("FIN_MON", true);
                if (sum.compareTo(largeSum.getMin()) >= 0) {
                    reportIdRequestConfirmationMoney = operationReportService.printRequestConfirmation(operation, locale);
                }
                if (sum.compareTo(financialMonitoringSum.getMin()) >= 0) {
                    reportIdMessageFinancialMonitoring = operationReportService.printMessageFinancialMonitoring(operation, userSession, locale);
                }
                reportIdConsentPersonalData = operationReportService.printConsentPersonalData(operation, userSession, locale);
            }
            showReport = true;
        } catch (BackException e) {
            log.error(e.getMessage(), e);
            errorMessage = e.getMessage();
        } catch (CurrencyExchangeException e) {
            log.error(e.getMessage(), e);
            errorMessage = e.getMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            errorMessage = "Error processing operation";
        }
    }

    @Transactional
    private PersonHistory getPersonHistory(Long personId) {
        Person person = personRepository.findOne(personId);
        PersonHistory personHistory = new PersonHistory();
        personHistory.setPerson(person);
        personHistory.setDocument(documentRepository.findMain(person));
        personHistory.setAddress(new AddressHistory(personAddressRepository.findOne((root, query, cb) -> {
            root.fetch(PersonAddress_.address);
            return cb.and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), Addresses.STAYING_TYPE));
        })));
        personHistory.setHomePhone(
                Optional.ofNullable(contactService.findMainOrAnyOtherContact(person, Contacts.HOME_PHONE_TYPE)).map(Contact::getData).orElse(null));
        personHistory.setMobilePhone(Optional.ofNullable(
                Optional.ofNullable(contactService.findMainOrAnyOtherContact(person, Contacts.SMS_CLIENT_BANK_TYPE))
                        .orElse(contactService.findMainOrAnyOtherContact(person, Contacts.MOBILE_PHONE_TYPE))).map(Contact::getData).orElse(null));
        personHistory.setEmail(
                Optional.ofNullable(contactService.findMainOrAnyOtherContact(person, Contacts.EMAIL_TYPE)).map(Contact::getData).orElse(null));
        return personHistory;
    }

    public void closeReport(CloseEvent event) {
        showReport = false;
        ((Dialog) event.getComponent()).setVisible(true);
    }

    public void openReport() {
        showReport = true;
    }

    public String cancel() {
        return "cancel";
    }
}
