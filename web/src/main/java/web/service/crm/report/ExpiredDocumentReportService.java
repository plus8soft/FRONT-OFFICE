/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.entity.crm.Contact_;
import web.entity.crm.Document;
import web.entity.crm.Document_;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress_;
import web.entity.dict.ContextType;
import web.entity.dict.DocumentType;
import web.entity.dict.ReportTemplate;
import web.entity.dict.ReportTemplateContext;
import web.entity.dict.ReportTemplate_;
import web.repository.crm.ContactRepository;
import web.repository.crm.DocumentCopyRepository;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.dict.ReportTemplateRepository;
import web.service.back.CreditInfoCache;
import web.service.back.CreditInfoWrapper;
import web.service.back.DepositInfoCache;
import web.service.back.DepositInfoWrapper;
import web.service.crm.report.context.address.Address;
import web.service.crm.report.context.address.AddressContext;
import web.service.crm.report.context.address.PersonAddress;
import web.service.crm.report.context.document.additional.AdditionalDocumentContext;
import web.service.crm.report.context.document.expired.ExpiredDocument;
import web.service.crm.report.context.document.expired.ExpiredDocumentContext;
import web.service.crm.report.context.document.main.MainDocumentContext;
import web.service.crm.report.context.person.PersonContext;
import web.service.crm.report.context.product.credit.Credit;
import web.service.crm.report.context.product.credit.CreditContext;
import web.service.crm.report.context.product.deposit.Deposit;
import web.service.crm.report.context.product.deposit.DepositContext;
import web.service.crm.report.context.user.UserContext;
import web.service.report.MsReportService;
import web.session.UserSession;

@Service
public class ExpiredDocumentReportService {

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private DocumentCopyRepository documentCopyRepository;

    @Autowired(required = false)
    private MsReportService msReportService;

    @Autowired
    private DepositInfoCache depositInfoCache;

    @Autowired
    private CreditInfoCache creditInfoCache;

    public String build(DocumentType documentType, Person person, UserSession userSession) {
        try {
            ReportTemplate reportTemplate = reportTemplateRepository.findOne((root, query, cb) -> {
                root.fetch(ReportTemplate_.reportTemplateContexts);
                return cb.equal(root, documentType.getReportTemplate());
            });
            List<ContextType> contextTypes =
                    reportTemplate.getReportTemplateContexts().stream().map(ReportTemplateContext::getType).collect(Collectors.toList());
            List<Object> contexts = new ArrayList<>();
            contextTypes.forEach(contextType -> {
                switch (contextType) {
                    case CREDITS:
                        CreditInfoWrapper creditInfoWrapper = creditInfoCache.loadCreditInfo(userSession.getUser().getLogin(), person);
                        contexts.add(new CreditContext(creditInfoWrapper.getCreationDateTime(), creditInfoWrapper.getCredits().stream()
                                                                                                                 .map(credit -> new Credit(
                                                                                                                         credit.getProductName(),
                                                                                                                         credit.getInterestRate(),
                                                                                                                         credit.getNumber(), null,
                                                                                                                         credit.getDateClose(),
                                                                                                                         credit.getDebt(),
                                                                                                                         credit.getPayAmount(),
                                                                                                                         credit.getCurrency()))
                                                                                                                 .collect(Collectors.toList())));
                        break;
                    case DEPOSITS:
                        DepositInfoWrapper depositInfoWrapper = depositInfoCache.loadDepositInfo(userSession.getUser().getLogin(), person);
                        contexts.add(new DepositContext(depositInfoWrapper.getCreationDateTime(), depositInfoWrapper.getDeposits().stream()
                                                                                                                    .map(deposit -> new Deposit(
                                                                                                                            deposit.getProductName(),
                                                                                                                            deposit.getInterestRate(),
                                                                                                                            deposit.getNumber(),
                                                                                                                            deposit.getDateOpen(),
                                                                                                                            deposit.getDateClose(),
                                                                                                                            deposit.getBalance(),
                                                                                                                            deposit.getCurrency()))
                                                                                                                    .collect(Collectors.toList())));
                        break;
                    case USER:
                        contexts.add(new UserContext(userSession.getUser().getLastname(), userSession.getUser().getFirstname(),
                                                     userSession.getUser().getPatronymic(), userSession.getUser().getPositionText()));
                        break;
                    case CLIENT_INFO:
                        PersonContext personContext = new PersonContext();
                        personContext.setLastName(person.getLastname());
                        personContext.setFirstName(person.getFirstname());
                        personContext.setPatronymic(person.getPatronymic());
                        personContext.setCountry(person.getBirthCountry());
                        personContext.setCitizenship(person.getCitizenship());
                        personContext.setResident(person.getResidentCountry());
                        contexts.add(personContext);
                        break;
                    case ADDRESSES:
                        AddressContext addressContext = new AddressContext();
                        addressContext.setPersonAddresses(personAddressRepository.findAll((root, query, cb) -> {
                            root.fetch(PersonAddress_.address);
                            return cb.equal(root.get(PersonAddress_.person), person);
                        }).stream().map(personAddress -> {
                            PersonAddress reportPersonAddress = new PersonAddress();
                            reportPersonAddress.setType(personAddress.getType());
                            reportPersonAddress.setMatchType(personAddress.getMatchType());
                            reportPersonAddress.setAddress(new Address(personAddress.getAddress()));
                            return reportPersonAddress;
                        }).collect(Collectors.toList()));
                        contexts.add(addressContext);
                        break;
                    case MAIN_DOCUMENT:
                        MainDocumentContext mainDocumentContext = new MainDocumentContext();
                        Document mainDocument = documentRepository.findMain(person);
                        mainDocumentContext.setDocument(
                                new web.service.crm.report.context.document.Document(mainDocument.getType(), mainDocument.getSeries(),
                                                                                     mainDocument.getNumber(), mainDocument.getIssuanceUnit(),
                                                                                     mainDocument.getIssuanceUnitCode(),
                                                                                     mainDocument.getIssuanceDate(),
                                                                                     mainDocument.getValidUntilDate()));
                        contexts.add(mainDocumentContext);
                        break;
                    case ADDITIONAL_DOCUMENTS:
                        AdditionalDocumentContext additionalDocumentContext = new AdditionalDocumentContext();
                        additionalDocumentContext.setDocuments(
                                documentRepository.findAll((root, query, cb) -> cb.equal(root.get(Document_.person), person)).stream()
                                                  .map(additionalDocument -> new web.service.crm.report.context.document.Document(
                                                          additionalDocument.getType(), additionalDocument.getSeries(),
                                                          additionalDocument.getNumber(), additionalDocument.getIssuanceUnit(),
                                                          additionalDocument.getIssuanceUnitCode(), additionalDocument.getIssuanceDate(),
                                                          additionalDocument.getValidUntilDate())).collect(Collectors.toList()));
                        contexts.add(additionalDocumentContext);
                        break;
                    case EXPIRED_DOCUMENTS:
                        contexts.add(new ExpiredDocumentContext(
                                documentCopyRepository.findExpiredCopies(person, LocalDate.now()).stream().map(documentCopy -> {
                                    ExpiredDocument expiredDocument = new ExpiredDocument();
                                    expiredDocument.setCreationDate(documentCopy.getCreationDate());
                                    expiredDocument.setVersion(documentCopy.getVersion());
                                    expiredDocument.setValidUntilDate(documentCopy.getValidUntilDate());
                                    expiredDocument.setName(documentCopy.getName());
                                    return expiredDocument;
                                }).collect(Collectors.toList())));
                        break;
                }
            });
            return msReportService.build(reportTemplate.getFile(), FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                                         userSession.getUser().getDepartment().getZoneId(), contexts.toArray());
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Report generation is not available: " + e.getMessage() + ". MS Word is required for PDF report generation.", e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
