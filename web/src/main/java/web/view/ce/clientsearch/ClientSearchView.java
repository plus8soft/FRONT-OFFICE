/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientsearch;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContextHolder;
import web.component.StringValueConverter;
import web.component.ValueConverter;
import web.dictionary.DocumentTypeDictionary;
import web.dictionary.Unit;
import web.entity.dict.Country;
import web.entity.crm.Contact;
import web.entity.crm.Contact_;
import web.entity.crm.Document;
import web.entity.crm.DocumentCopy;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.crm.PersonAddress_;
import web.entity.crm.Photo;
import web.entity.crm.Photo_;
import web.entity.dict.ReportTemplate_;
import web.entity.dict.ReportType;
import web.integration.fineract.FineractClientHit;
import web.integration.fineract.FineractClientStatus;
import web.integration.fineract.FineractException;
import web.integration.fineract.FineractIntegrationService;
import web.lob.entity.crm.LobDocumentCopy;
import web.lob.entity.crm.LobPhoto;
import web.lob.repositories.LobDocumentCopyRepository;
import web.lob.repositories.LobPhotoRepository;
import web.projection.PersonAutoComplete;
import web.repository.back.crm.credit.OutputCreditInfo;
import web.repository.back.crm.deposit.OutputDepositInfo;
import web.repository.crm.ContactRepository;
import web.repository.crm.DocumentCopyRepository;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.crm.PersonRepository;
import web.repository.crm.PhotoRepository;
import web.repository.dict.CountryRepository;
import web.repository.dict.DocumentTypeRepository;
import web.repository.dict.ReportTemplateRepository;
import web.service.back.CreditInfoCache;
import web.service.back.CreditInfoWrapper;
import web.service.back.DepositInfoCache;
import web.service.back.DepositInfoWrapper;
import web.service.crm.PersonService;
import web.service.crm.report.ExpiredDocumentReportService;
import web.service.crm.report.context.product.ProductBriefSummary;
import web.service.crm.validator.BasicDataValidator;
import web.service.crm.validator.PassportValidator;
import web.service.crm.validator.PersonValidateException;
import web.service.crm.validator.PersonValidator;
import web.service.crm.validator.TerroristValidator;
import web.service.report.MsReportService;
import web.session.Menu;
import web.session.UserSession;
import web.utils.Addresses;
import web.utils.ClientPlaceholderImage;
import web.utils.DateTimes;
import web.utils.Documents;
import web.view.Message;
import web.view.converter.AutoCompletePojoConverter;

@Getter
@Setter
@Log4j2
public class ClientSearchView implements Message, Serializable {

    public static final Map<String, String> MATCH_ADDRESS_VALUES = new HashMap<String, String>() {
        {
            put(Addresses.STAYING_TYPE, "matches staying address");
            put(Addresses.STAYING_TYPE, "matches staying address");
            put(Addresses.CORRESPONDENCE_TYPE, "matches correspondence address");
            put(Addresses.RESIDENTIAL_TYPE, "matches actual address");
        }
    };

    private static final String CLIENT_SEARCH_MENU = "menu-single-window-client-search";

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private DocumentCopyRepository documentCopyRepository;

    @Autowired
    private LobDocumentCopyRepository lobDocumentCopyRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private LobPhotoRepository lobPhotoRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private PersonService personService;

    @Autowired
    private ExpiredDocumentReportService expiredDocumentReportService;

    @Autowired(required = false)
    private MsReportService msReportService;

    @Autowired
    private DocumentTypeDictionary documentTypeDictionary;

    @Autowired
    private DepositInfoCache depositInfoCache;

    @Autowired
    private CreditInfoCache creditInfoCache;

    @Autowired
    private UserSession userSession;

    @Autowired
    private FineractIntegrationService fineractIntegrationService;

    private FineractClientStatus fineractClientStatus = FineractClientStatus.disabled();

    private String fineractSearchQuery;

    private List<FineractClientHit> fineractSearchResults = Collections.emptyList();

    private Document mainDocument;

    private PersonAddress stayingPersonAddress;

    private PersonAddress accommodationPersonAddress;

    private PersonAddress residentialPersonAddress;

    private PersonAddress correspondencePersonAddress;

    private List<Contact> contacts;

    private Photo photo;

    private byte[] emptyIcon;

    private List<Document> additionalDocuments;

    private List<DocumentCopy> invalidDocuments;

    private List<PersonValidator> validators;

    private PersonValidateException validateException;

    private String birthValue;

    private Person person;

    private String searchingDocument;

    private boolean showReport;

    private boolean showProductsReport;

    private AutoCompletePojoConverter<PersonAutoComplete> converter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), elem -> String.valueOf(elem.getDocumentId()));

    private ValueConverter<PersonAutoComplete, String> valueConverter = new StringValueConverter<PersonAutoComplete>() {
        @Override
        public String toTarget(PersonAutoComplete source) {
            return Stream.of(source.getDocumentSeries(), source.getDocumentNumber(), source.getDocumentType()).filter(Objects::nonNull)
                         .collect(Collectors.joining(" "));
        }
    };

    private Document document;

    private boolean advancedSearch;

    private Integer stepNumber;

    private Integer stepsCount;

    private String headerLabel;

    private Long personId;

    private DocumentCopy selectedInvalidDocument;

    private DepositInfoWrapper depositsInfo;

    private List<OutputDepositInfo> filteredDeposits;

    private CreditInfoWrapper creditsInfo;

    private String productsReport;

    private List<OutputCreditInfo> filteredCredits;

    private boolean allProducts;

    private String expiredDocumentReport;

    public void init(Long conversationPersonId) {
        person = null;
        document = document == null ? new Document() : document;
        allProducts = false;
        Long idToLoad = conversationPersonId != null ? conversationPersonId : this.personId;
        if (idToLoad != null) {
            person = personRepository.findOne(idToLoad);
            if (person != null && isClientSearchMenuContext()) {
                RequestContextHolder.getRequestContext().getConversationScope().put("CS_CLIENT_ID", idToLoad);
                this.personId = null;
            }
        }
        emptyIcon = ClientPlaceholderImage.load();
        if (person != null) {
            mainDocument = documentRepository.findMain(person);
            additionalDocuments = documentRepository.findAdditional(person);
            stayingPersonAddress = personAddressRepository.findOne((root, query, cb) -> {
                root.fetch(PersonAddress_.address);
                return cb
                        .and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), Addresses.STAYING_TYPE));
            });
            accommodationPersonAddress = personAddressRepository.findOne((root, query, cb) -> {
                root.fetch(PersonAddress_.address);
                return cb.and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), Addresses.STAYING_TYPE));
            });
            residentialPersonAddress = personAddressRepository.findOne((root, query, cb) -> {
                root.fetch(PersonAddress_.address);
                return cb.and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), Addresses.RESIDENTIAL_TYPE));
            });
            correspondencePersonAddress = personAddressRepository.findOne((root, query, cb) -> {
                root.fetch(PersonAddress_.address);
                return cb.and(cb.equal(root.get(PersonAddress_.person), person),
                              cb.equal(root.get(PersonAddress_.type), Addresses.CORRESPONDENCE_TYPE));
            });
            contacts = contactRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get(Contact_.person), person)));
            photo = photoRepository.findOne((root, query, cb) -> cb.equal(root.get(Photo_.person), person));
            if (photo != null) {
                LobPhoto lobPhoto = lobPhotoRepository.findOne(photo.getId());
                photo.setImage(lobPhoto == null ? null : lobPhoto.getData());
            }
            try {
                invalidDocuments = documentCopyRepository.findExpiredCopies(person, LocalDate.now()).stream().map(documentCopy -> {
                    LobDocumentCopy lob = lobDocumentCopyRepository.findOne(documentCopy.getId());
                    if (lob != null) {
                        documentCopy.setFile(lob.getData());
                    }
                    return documentCopy;
                }).collect(Collectors.toList());
                invalidDocuments.addAll(documentTypeRepository.findDismissRequiredDocuments(person).stream().map(type -> {
                    DocumentCopy copy = new DocumentCopy();
                    copy.setName(type.getName());
                    copy.setBankDocumentType(type);
                    return copy;
                }).collect(Collectors.toList()));
            } catch (Exception e) {
                log.warn("Could not load document copies for person {}: {}", person.getId(), e.getMessage());
                invalidDocuments = Collections.emptyList();
            }
            birthValue = Stream.of(countryName(person.getBirthCountry()), person.getBirthPlace()).filter(Objects::nonNull)
                               .collect(Collectors.joining(", "));
            validateException = null;
            refreshFineractStatus();
        } else {
            fineractClientStatus = FineractClientStatus.disabled();
        }
    }

    public void onRefreshFineractStatus() {
        refreshFineractStatus();
    }

    public void onSyncToFineract() {
        if (person == null || person.getId() == null) {
            addWarnMessage("Save the client in CRM before syncing to Fineract.");
            return;
        }
        if (fineractIntegrationService.syncPersonAfterSave(person).isPresent()) {
            addWarnMessage(FineractIntegrationService.SYNC_USER_WARNING);
        } else if (fineractIntegrationService.isEnabled()) {
            addInfoMessage(FineractIntegrationService.SYNC_USER_SUCCESS);
        }
        refreshFineractStatus();
    }

    private void refreshFineractStatus() {
        fineractClientStatus = fineractIntegrationService.resolveStatus(person);
    }

    public boolean isFineractSearchEnabled() {
        return fineractIntegrationService.isEnabled();
    }

    public void searchFineractClients() {
        fineractSearchResults = Collections.emptyList();
        String query = resolveFineractSearchQuery();
        if (query == null || query.trim().isEmpty()) {
            addWarnMessage("Enter a name, phone, or account number to search Fineract.");
            return;
        }
        fineractSearchQuery = query.trim();
        fineractSearchResults = queryFineractClients(fineractSearchQuery, false);
    }

    public List<String> getFineractAutoCompletes(String query) {
        if (!fineractIntegrationService.isEnabled() || query == null || query.trim().length() < 2) {
            fineractSearchResults = Collections.emptyList();
            return Collections.emptyList();
        }
        fineractSearchQuery = query.trim();
        List<FineractClientHit> hits = queryFineractClients(fineractSearchQuery, true);
        fineractSearchResults = hits;
        return hits.stream().map(ClientSearchView::formatFineractHitLabel).collect(Collectors.toList());
    }

    private static String formatFineractHitLabel(FineractClientHit hit) {
        if (hit == null) {
            return "";
        }
        if (hit.getAccountNo() != null && !hit.getAccountNo().isEmpty()) {
            return hit.getDisplayName() + " (" + hit.getAccountNo() + ")";
        }
        return hit.getDisplayName() != null ? hit.getDisplayName() : "";
    }

    private List<FineractClientHit> queryFineractClients(String query, boolean silent) {
        try {
            List<FineractClientHit> hits = fineractIntegrationService.searchClientsNotInCrm(query);
            if (hits.isEmpty() && !silent) {
                addInfoMessage("No Fineract clients found that are not linked to CRM.");
            }
            return hits;
        } catch (FineractException e) {
            if (!silent) {
                addWarnMessage(e.getMessage());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Fineract search failed: {}", e.getMessage());
            if (!silent) {
                addWarnMessage("Fineract search failed: " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    public void searchFineractAfterNotFound() {
        fineractSearchQuery = resolveFineractSearchQuery();
        searchFineractClients();
    }

    public String importFineractClientById(Long fineractClientId) {
        if (fineractClientId == null) {
            addWarnMessage("Select a Fineract client first.");
            return null;
        }
        try {
            Person imported = fineractIntegrationService.importClientToCrm(fineractClientId);
            if (imported == null || imported.getId() == null) {
                addWarnMessage("Could not import client from Fineract.");
                return null;
            }
            if (isClientSearchMenuContext()) {
                RequestContextHolder.getRequestContext().getConversationScope().put("CS_CLIENT_ID", imported.getId());
                this.personId = null;
            }
            init(imported.getId());
            fineractSearchResults = Collections.emptyList();
            fineractSearchQuery = null;
            addInfoMessage(FineractIntegrationService.IMPORT_USER_SUCCESS);
            return null;
        } catch (FineractException e) {
            addWarnMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Fineract import failed: {}", e.getMessage(), e);
            addWarnMessage("Could not import client from Fineract: " + e.getMessage());
            return null;
        }
    }

    public String importFineractClient(FineractClientHit hit) {
        return hit == null ? null : importFineractClientById(hit.getFineractClientId());
    }

    public void linkFineractClientById(Long fineractClientId) {
        if (fineractClientId == null) {
            return;
        }
        if (person == null || person.getId() == null) {
            addWarnMessage("Open a CRM client first, or use Create in CRM.");
            return;
        }
        try {
            fineractIntegrationService.linkFineractClientToPerson(fineractClientId, person);
            refreshFineractStatus();
            searchFineractClients();
            addInfoMessage(FineractIntegrationService.LINK_USER_SUCCESS);
        } catch (FineractException e) {
            addWarnMessage(e.getMessage());
        } catch (Exception e) {
            log.warn("Fineract link failed: {}", e.getMessage());
            addWarnMessage("Could not link client to Fineract: " + e.getMessage());
        }
    }

    public void linkFineractClient(FineractClientHit hit) {
        if (hit != null) {
            linkFineractClientById(hit.getFineractClientId());
        }
    }

    public boolean isFineractClientLinkAvailable() {
        return person != null && person.getId() != null;
    }

    private String resolveFineractSearchQuery() {
        if (fineractSearchQuery != null && !fineractSearchQuery.trim().isEmpty()) {
            return fineractSearchQuery.trim();
        }
        if (document != null) {
            String fromDocument = Stream.of(document.getSeries(), document.getNumber()).filter(Objects::nonNull)
                                        .map(String::trim).filter(value -> !value.isEmpty())
                                        .collect(Collectors.joining(" "));
            if (!fromDocument.isEmpty()) {
                return fromDocument;
            }
        }
        if (searchingDocument != null && !searchingDocument.trim().isEmpty()) {
            return searchingDocument.trim();
        }
        return null;
    }


    public String toBase64() {
        return Base64.getEncoder().encodeToString(photo == null ? emptyIcon : photo.getImage());
    }

    public int calculateAge() {
        if (person == null || person.getBirthDate() == null) {
            return 0;
        }
        return Period.between(person.getBirthDate(), LocalDate.now(userSession.getUser().getDepartment().getZoneId())).getYears();
    }

    public String buildDocumentValue(Document document) {
        if (document == null) {
            return "";
        }
        String issuance =
                document.getIssuanceDate() == null ? null : String.join(" ", "issued:", document.getIssuanceDate().format(DateTimes.DATE_FORMATTER));
        String valid = document.getValidUntilDate() == null ? null :
                       String.join(" ", "valid until:", document.getValidUntilDate().format(DateTimes.DATE_FORMATTER));
        return Stream.of(Stream.of(document.getSeries(), document.getNumber()).filter(string -> string != null && !string.isEmpty())
                               .collect(Collectors.joining(" ")), issuance, valid).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

    public List<Integer> initStepList() {
        return IntStream.range(1, stepsCount + 1).boxed().collect(Collectors.toList());
    }

    public String validatePersonData() {
        try {
            Stream<PersonValidator> allValidators = Stream.of(new TerroristValidator(), new PassportValidator(), new BasicDataValidator());
            for (PersonValidator validator : (validators != null ? Stream.concat(allValidators, validators.stream()) : allValidators)
                    .collect(Collectors.toList())) {
                validator.validate(userSession.getUser(), person);
            }
            return "next";
        } catch (PersonValidateException e) {
            validateException = e;
            return null;
        }
    }

    public Collection<PersonAutoComplete> getAutoCompletes(String query) {
        Collection<PersonAutoComplete> result = null;
        String clearQuery = query.trim().replaceFirst(" +", " ");
        document = new Document();
        if (clearQuery.contains(" ")) {
            String[] split = clearQuery.split(" ");
            document.setSeries(split[0]);
            document.setNumber(split[1]);
            if (clearQuery.length() > 6) {
                converter.setSource(personRepository.findByLikeDocument(document));
                result = converter.getSource();
            }
        } else {
            document.setNumber(clearQuery);
            if (clearQuery.length() > 4) {
                converter.setSource(personRepository.findByLikeDocument(document));
                result = converter.getSource();
            }
        }
        return result;
    }

    public void onSelectPerson(SelectEvent event) {
        PersonAutoComplete autoComplete = (PersonAutoComplete) event.getObject();
        String selectedType = documentTypeLabel(autoComplete.getDocumentType());
        searchingDocument = Stream.of(autoComplete.getDocumentSeries(), autoComplete.getDocumentNumber(), selectedType).filter(Objects::nonNull)
                                  .collect(Collectors.joining(" "));
        document.setSeries(autoComplete.getDocumentSeries());
        document.setNumber(autoComplete.getDocumentNumber());
        document.setType(autoComplete.getDocumentType());
        ((RequestControlContext) RequestContextHolder.getRequestContext()).handleEvent(new Event(this, onSelectPersonDefine(autoComplete)));
    }

    public String onSelectPersonDefine(PersonAutoComplete autoComplete) {
        if (isClientSearchMenuContext()) {
            personId = null;
            person = new Person();
            person.setId(autoComplete.getPersonId());
            return "start-client-session";
        } else {
            personId = autoComplete.getPersonId();
            person = new Person();
            person.setId(null);
            return "non-session-client";
        }
    }

    public void openExactSearch() {
        advancedSearch = true;
        if (document == null) {
            document = new Document();
        }
        if ((document.getSeries() == null && document.getNumber() == null) ||
                (document.getSeries() != null && document.getSeries().length() == 4)) {
            document.setType(Documents.NATIONAL_PASSPORT_CODE);
        } else {
            document.setType(Documents.FOREIGN_CITIZEN_PASSPORT_CODE);
        }
    }

    public void onAdvancedSearchChange() {
        advancedSearch = !advancedSearch;
        if (advancedSearch) {
            document.setType((document.getSeries() == null && document.getNumber() == null) ||
                             (document.getSeries() != null && document.getSeries().length() == 4) ? Documents.NATIONAL_PASSPORT_CODE :
                             Documents.FOREIGN_CITIZEN_PASSPORT_CODE);
            searchingDocument = null;
        }
    }

    public void onDocumentTypeChanged() {
        document.setSeries(null);
        document.setNumber(null);
    }

    public String findPersonByString() {
        String redirectPath = null;
        if (searchingDocument != null) {
            String clearQuery = searchingDocument.trim().replaceFirst(" +", " ");
            if (searchingDocument.contains(" ")) {
                String[] split = clearQuery.trim().split(" ");
                document.setType((split[0] != null && split[1] != null && split[0].length() == 4 && split[1].length() == 6) ?
                                 Documents.NATIONAL_PASSPORT_CODE : Documents.FOREIGN_CITIZEN_PASSPORT_CODE);
                document.setSeries(split[0]);
                document.setNumber(split[1]);
                redirectPath = findPerson();
            } else {
                document.setType(Documents.FOREIGN_CITIZEN_PASSPORT_CODE);
                document.setSeries(null);
                document.setNumber(clearQuery);
                redirectPath = findPerson();
            }
        }
        return redirectPath;
    }

    public String findPerson() {
        String redirectPath;
        searchingDocument = null;
        person = personService.receivePersonByMainDocument(userSession.getUser(), document.getType(), document.getSeries(), document.getNumber());
        if (person == null) {
            RequestContext.getCurrentInstance().addCallbackParam("empty", true);
            if (fineractIntegrationService.isEnabled()) {
                searchFineractAfterNotFound();
                RequestContext.getCurrentInstance().addCallbackParam("fineractHits", !fineractSearchResults.isEmpty());
            }
            redirectPath = null;
        } else {
            redirectPath = resolveSession();
        }
        return redirectPath;
    }

    private String resolveSession() {
        if (isClientSearchMenuContext()) {
            personId = null;
            return "start-client-session";
        } else {
            personId = person.getId();
            person = new Person();
            person.setId(null);
            return "non-session-client";
        }
    }

    public String finishSession() {
        document = new Document();
        searchingDocument = null;
        person = null;
        fineractSearchResults = Collections.emptyList();
        return "end";
    }

    public boolean isClientSessionOpen() {
        return person != null && isClientSearchMenuContext() && stepsCount == null && stepNumber == null;
    }

    public void closeDialog(CloseEvent event) {
        showReport = false;
        showProductsReport = false;
        ((Dialog) event.getComponent()).setVisible(true);
    }

    public void findDeposits() {
        depositsInfo = depositInfoCache.loadDepositInfo(userSession.getUser().getLogin(), person);
        filterDeposits();
    }

    public void findCredits() {
        creditsInfo = creditInfoCache.loadCreditInfo(userSession.getUser().getLogin(), person);
        filterCredits();
    }

    public void updateProducts() {
        depositInfoCache.resetCache(person);
        creditInfoCache.resetCache(person);
    }

    public String addPerson() {
        person = new Person();
        return "to-edit-client";
    }

    public void printExpiredDocumentReport() {
        expiredDocumentReport = expiredDocumentReportService.build(selectedInvalidDocument.getBankDocumentType(), person, userSession);
        showReport = true;
    }

    public void showAllProducts() {
        filterDeposits();
        filterCredits();
    }

    public void printReport() {
        Instant dateTime = creditsInfo.getCreationDateTime() == null ? depositsInfo.getCreationDateTime() : creditsInfo.getCreationDateTime();
        showProductsReport = true;
        productsReport = build(dateTime, filteredCredits, filteredDeposits);
    }

    private String build(Instant creationDateTime, List<OutputCreditInfo> filteredCredits, List<OutputDepositInfo> filteredDeposits) {
        try {
            if (msReportService == null) {
                throw new UnsupportedOperationException("MS Word/COM4J is not available on this platform. PDF report generation requires Windows with MS Word installed.");
            }
            return msReportService
                    .build(reportTemplateRepository.findOne((root, query, cb) -> cb.equal(root.get(ReportTemplate_.systemName), ReportType.PRODUCTS))
                                                   .getFile(), FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                           userSession.getUser().getDepartment().getZoneId(),
                           new ProductBriefSummary(creationDateTime, filteredCredits, filteredDeposits));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while generating report");
        }
        showProductsReport = false;
        return null;
    }

    private void filterDeposits() {
        if (depositsInfo.getDeposits() != null) {
            filteredDeposits = allProducts ? depositsInfo.getDeposits() :
                               depositsInfo.getDeposits().stream()
                                           .filter(deposit -> deposit.getDateClose() == null || LocalDate.now().isBefore(deposit.getDateClose()))
                                           .collect(Collectors.toList());
        } else {
            filteredDeposits = null;
        }
    }

    private void filterCredits() {
        if (creditsInfo.getCredits() != null) {
            filteredCredits = allProducts ? creditsInfo.getCredits() :
                              creditsInfo.getCredits().stream()
                                         .filter(credit -> credit.getDateClose() == null || LocalDate.now().isBefore(credit.getDateClose()))
                                         .collect(Collectors.toList());
        } else {
            filteredCredits = null;
        }
    }

    public String toEditClient() {
        return "to-edit-client";
    }

    private boolean isClientSearchMenuContext() {
        Menu menu = Menu.getInstance();
        if (menu == null || menu.getTask() == null) {
            return true;
        }
        return CLIENT_SEARCH_MENU.equals(menu.getTask().getSystemName());
    }

    private String countryName(String countryId) {
        if (countryId == null) {
            return null;
        }
        Country country = countryRepository.findOne(countryId);
        return country != null ? country.getName() : countryId;
    }

    private String documentTypeLabel(String type) {
        if (type == null) {
            return "";
        }
        Unit<String> unit = documentTypeDictionary.findOne(type);
        return unit != null ? unit.getValue() : type;
    }
}
