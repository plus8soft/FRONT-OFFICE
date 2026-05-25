/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientedit;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.CroppedImage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import web.dictionary.AttractionSourceDictionary;
import web.dictionary.BusinessTypeDictionary;
import web.dictionary.ClientStatusDictionary;
import web.dictionary.ContactTypeDictionary;
import web.dictionary.Unit;
import web.dictionary.DocumentTypeDictionary;
import web.dictionary.Unit;
import web.entity.crm.AdditionMethod;
import web.entity.crm.Address;
import web.entity.crm.Contact;
import web.entity.crm.Document;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.crm.PersonAddress_;
import web.entity.crm.Photo;
import web.entity.crm.Photo_;
import web.entity.dict.Country;
import web.lob.entity.crm.LobPhoto;
import web.lob.repositories.LobPhotoRepository;
import web.repository.back.BackException;
import web.repository.crm.AddressRepository;
import web.repository.crm.ContactRepository;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.crm.PersonRepository;
import web.repository.crm.PhotoRepository;
import web.repository.dict.CountryRepository;
import web.integration.fineract.FineractClientStatus;
import web.integration.fineract.FineractIntegrationService;
import web.service.crm.PersonService;
import web.session.UserSession;
import web.utils.Addresses;
import web.utils.Contacts;
import web.utils.ClientPlaceholderImage;
import web.utils.Documents;
import web.view.Message;
import web.view.component.AddressAutoComplete;

@Getter
@Setter
@Log4j2
public class EditClientView implements Message, Serializable {

    private static final String IMAGE_PATH = "/image/no-icon-client.png";

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private LobPhotoRepository lobPhotoRepository;

    @Autowired
    private ContactTypeDictionary contactTypeDictionary;

    @Autowired
    private DocumentTypeDictionary documentTypeDictionary;

    @Autowired
    private BusinessTypeDictionary businessTypeDictionary;

    @Autowired
    private ClientStatusDictionary clientStatusDictionary;

    @Autowired
    private AttractionSourceDictionary attractionSourceDictionary;


    @Autowired
    private PersonService personService;

    @Autowired
    private FineractIntegrationService fineractIntegrationService;

    private FineractClientStatus fineractClientStatus = FineractClientStatus.disabled();

    private String residentialAddressMatcher;

    private String correspondenceAddressMatcher;


    private AddressAutoComplete accommodationAddressAutoComplete;

    private AddressAutoComplete residentialAddressAutoComplete;

    private AddressAutoComplete correspondenceAddressAutoComplete;

    private Person person;

    private Document mainDocument;

    private List<Document> additionalDocuments;


    private PersonAddress accommodationPersonAddress;

    private PersonAddress residentialPersonAddress;

    private PersonAddress correspondencePersonAddress;

    private List<PersonAddress> externalPersonAddresses;

    private List<PersonAddress> personAddresses;

    private List<Address> addressAutoCompletes;

    private List<Contact> removedContacts;

    private List<Contact> contacts;

    private List<Document> removedDocuments;

    private List<Document> documents;

    private List<Unit> contactLocalTypes;

    private List<Country> countries;

    private PersonDiffContainer personDiffContainer;

    private byte[] emptyIcon;

    private Contact oldEditedContact;

    private Contact editedContact;

    private Document oldEditedDocument;

    private Document editedDocument;

    private CroppedImage croppedImage;

    private byte[] image;

    private Instant imageDate;

    private boolean localFile;

    private boolean cameraLive;

    private boolean webCameraSource;

    private Long personId;

    private Photo removedPhoto;

    private Photo photo;

    public void init(Long personId, String documentType, String documentSeries, String documentNumber) {
        emptyIcon = ClientPlaceholderImage.load();
        countries = countryRepository.findAllByOrderByNameAsc();
        List<PersonAddress> oldPersonAddresses;
        if (personId == null) {
            this.person = createPerson(documentType, documentSeries, documentNumber);
            oldPersonAddresses = this.person.getPersonAddresses();
        } else {
            this.person = personRepository.findOne(personId);
            oldPersonAddresses = personAddressRepository.findAll((root, query, cb) -> {
                root.fetch(PersonAddress_.address);
                return cb.equal(root.get(PersonAddress_.person), this.person);
            });
        }
        personDiffContainer = new PersonDiffContainer(this.person, oldPersonAddresses);
        defineFormData(oldPersonAddresses);
        refreshFineractStatus();
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

    private Person createPerson(String documentType, String documentSeries, String documentNumber) {
        Document document = new Document();
        document.setType(documentType);
        document.setSeries(documentSeries);
        document.setNumber(documentNumber);
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        Person person = new Person();
        person.setDocuments(documents);
        person.setContacts(new ArrayList<>());
        PersonAddress accommodationPersonAddress = new PersonAddress();
        accommodationPersonAddress.setType(Addresses.STAYING_TYPE);
        accommodationPersonAddress.setAddress(new Address());
        PersonAddress residentialPersonAddress = new PersonAddress();
        residentialPersonAddress.setType(Addresses.RESIDENTIAL_TYPE);
        residentialPersonAddress.setAddress(new Address());
        PersonAddress correspondencePersonAddress = new PersonAddress();
        correspondencePersonAddress.setType(Addresses.CORRESPONDENCE_TYPE);
        correspondencePersonAddress.setAddress(new Address());
        person.setPersonAddresses(
                Stream.of(accommodationPersonAddress, residentialPersonAddress, correspondencePersonAddress)
                      .collect(Collectors.toList()));
        return person;
    }

    private void defineFormData(List<PersonAddress> oldPersonAddresses) {
        photo = person.getId() == null ? null : photoRepository.findOne((root, query, cb) -> cb.equal(root.get(Photo_.person), person));
        if (photo != null) {
            LobPhoto lobPhoto = lobPhotoRepository.findOne(photo.getId());
            photo.setImage(lobPhoto == null ? null : lobPhoto.getData());
        }
        contacts = new ArrayList<>(person.getContacts());
        mainDocument = getActualMainDocument();
        additionalDocuments = person.getDocuments().stream()
                                    .filter(document -> !Documents.NATIONAL_PASSPORT_CODE.equals(document.getType()) &&
                                                        !Documents.FOREIGN_CITIZEN_PASSPORT_CODE.equals(document.getType()))
                                    .collect(Collectors.toList());
        accommodationPersonAddress = getPersonAddressByType(oldPersonAddresses, Addresses.STAYING_TYPE);
        accommodationAddressAutoComplete = new AddressAutoComplete(accommodationPersonAddress.getAddress());
        residentialPersonAddress = getPersonAddressByType(oldPersonAddresses, Addresses.RESIDENTIAL_TYPE);
        residentialAddressAutoComplete = new AddressAutoComplete(residentialPersonAddress.getAddress());
        residentialAddressMatcher = residentialPersonAddress.getMatchType();
        correspondencePersonAddress = getPersonAddressByType(oldPersonAddresses, Addresses.CORRESPONDENCE_TYPE);
        correspondenceAddressAutoComplete = new AddressAutoComplete(correspondencePersonAddress.getAddress());
        correspondenceAddressMatcher = correspondencePersonAddress.getMatchType();
        externalPersonAddresses = oldPersonAddresses.stream().filter(current -> !Addresses.RESIDENTIAL_TYPE.equals(current.getType()) &&
                                                                                !Addresses.STAYING_TYPE.equals(current.getType()) &&
                                                                                !Addresses.CORRESPONDENCE_TYPE.equals(current.getType()))
                                                    .collect(Collectors.toList());
        removedContacts = new ArrayList<>();
        removedDocuments = new ArrayList<>();
    }

    public int calculateAge() {
        return Period.between(person.getBirthDate(), LocalDate.now(userSession.getUser().getDepartment().getZoneId())).getYears();
    }

    public void onDocumentTypeChange() {
        Document oldMainDocument = personDiffContainer.getOldDocuments().stream()
                                                      .filter(document -> Documents.NATIONAL_PASSPORT_CODE.equals(document.getType()) ||
                                                                          Documents.FOREIGN_CITIZEN_PASSPORT_CODE.equals(document.getType()))
                                                      .findAny().orElse(null);
        if (oldMainDocument != null) {
            if (mainDocument.getType() != null && Objects.equals(mainDocument.getType(), oldMainDocument.getType())) {
                mainDocument.setIssuanceUnit(oldMainDocument.getIssuanceUnit());
                mainDocument.setIssuanceUnitCode(oldMainDocument.getIssuanceUnitCode());
                mainDocument.setIssuanceDate(oldMainDocument.getIssuanceDate());
                mainDocument.setValidUntilDate(oldMainDocument.getValidUntilDate());
                mainDocument.setNumber(oldMainDocument.getNumber());
                mainDocument.setSeries(oldMainDocument.getSeries());
            } else {
                mainDocument.setIssuanceUnit(null);
                mainDocument.setIssuanceUnitCode(null);
                mainDocument.setIssuanceDate(null);
                mainDocument.setValidUntilDate(null);
                mainDocument.setNumber(null);
                mainDocument.setSeries(null);
            }
        }
    }

    public void addContact() {
        Contact contact = new Contact();
        contact.setPerson(person);
        contact.setType(Contacts.MOBILE_PHONE_TYPE);
        contacts.add(contact);
    }

    public void removeContact(int index) {
        Contact removedContact;
        if ((removedContact = contacts.remove(index)).getId() != null) {
            removedContacts.add(removedContact);
        }
    }

    public void onContactEdit(RowEditEvent event) {
        editedContact = (Contact) event.getObject();
        BeanUtils.copyProperties(editedContact, oldEditedContact = new Contact());
    }

    public void onCancelContactEdit() {
        BeanUtils.copyProperties(oldEditedContact, editedContact);
    }

    public void onCommitContactEdit() {
        if (editedContact.isMain()) {
            contacts.stream().filter(contact -> editedContact != contact && editedContact.getType().equals(contact.getType()) && contact.isMain())
                    .forEach(contact -> contact.setMain(false));
        }
    }

    public void onContactTypeChange(Contact contact) {
        if (contact != null) {
            contact.setData(null);
        }
    }

    public void onContactMainChange(Contact contact) {
        if (contact != null && contact.isMain() && contact.getType() != null) {
            contacts.stream()
                    .filter(c -> c != contact && contact.getType().equals(c.getType()) && c.isMain())
                    .forEach(c -> c.setMain(false));
        }
    }

    public String contactTypeLabel(String type) {
        if (type == null) {
            return "";
        }
        Unit<String> unit = contactTypeDictionary.findOne(type);
        return unit != null ? unit.getValue() : type;
    }

    public boolean isContactTypeUnused(String type) {
        if (type == null) {
            return false;
        }
        Unit<String> unit = contactTypeDictionary.findOne(type);
        return unit != null && unit.isUnused();
    }

    public boolean showGenericContactInput(String type) {
        return type == null || (!Contacts.EMAIL_TYPE.equals(type) && !Contacts.MOBILE_PHONE_TYPE.equals(type)
                && !Contacts.SMS_CLIENT_BANK_TYPE.equals(type) && !Contacts.HOME_PHONE_TYPE.equals(type)
                && !Contacts.WORK_PHONE_TYPE.equals(type) && !Contacts.FAX_TYPE.equals(type));
    }

    private void dropBlankContacts() {
        contacts.removeIf(contact -> contact.getType() == null
                && (contact.getData() == null || contact.getData().trim().isEmpty()));
    }

    private boolean hasIncompleteContacts() {
        return contacts.isEmpty() || contacts.stream().anyMatch(contact -> contact.getType() == null
                || contact.getData() == null || contact.getData().trim().isEmpty());
    }

    // CIS/SNG region: extra identity documents (panel hidden in ENG build — drop rows user cannot edit)
    private void dropIncompleteAdditionalDocuments() {
        additionalDocuments.removeIf(document -> document.getType() == null
                || document.getNumber() == null || document.getIssuanceDate() == null);
    }

    public void onDocumentEdit(RowEditEvent event) {
        editedDocument = (Document) event.getObject();
        BeanUtils.copyProperties(editedDocument, oldEditedDocument = new Document());
    }

    public void onCancelDocumentEdit() {
        BeanUtils.copyProperties(oldEditedDocument, editedDocument);
    }

    public void onAccommodationStateChange() {
        // Registration address removed - accommodation address is independent
    }

    public void onResidentialAddressStateChange() {
        residentialAddressAutoComplete
                .setAddress(residentialAddressMatcher != null ? getAddressAutoComplete(residentialAddressMatcher).getAddress() : new Address());
        residentialPersonAddress.setMatchType(residentialAddressMatcher);
    }

    public void onCorrespondenceAddressStateChange() {
        correspondenceAddressAutoComplete
                .setAddress(correspondenceAddressMatcher != null ? getAddressAutoComplete(correspondenceAddressMatcher).getAddress() : new Address());
        correspondencePersonAddress.setMatchType(correspondenceAddressMatcher);
    }

    public void addDocument() {
        Document newDocument = new Document();
        newDocument.setPerson(person);
        additionalDocuments.add(newDocument);
    }

    public void removeDocument(int index) {
        Document removedDocument;
        if ((removedDocument = additionalDocuments.remove(index)).getId() != null) {
            removedDocuments.add(removedDocument);
        }
    }

    public String toBase64Photo() {
        return Base64.getEncoder().encodeToString(photo == null ? emptyIcon : photo.getImage());
    }

    public String toBase64() {
        return Base64.getEncoder().encodeToString(image == null ? emptyIcon : image);
    }

    public void uploadIcon(FileUploadEvent event) {
        image = event.getFile().getContents();
        imageDate = Instant.now();
    }

    public void savePhotoChanges() {
        if (localFile && imageDate == null) {
            croppedImage = null;
            addErrorMessage("Photo date field is not filled");
            RequestContext.getCurrentInstance().addCallbackParam("error", true);
        }
        if (FacesContext.getCurrentInstance().getMessageList().isEmpty()) {
            photo = photo == null ? personDiffContainer.getOldPhoto() == null ? new Photo() : personDiffContainer.getOldPhoto() : photo;
            photo.setImage(croppedImage.getBytes());
            photo.setDateTime(imageDate);
            photo.setAdditionMethod(webCameraSource ? AdditionMethod.WEB_CAMERA : localFile ? AdditionMethod.FILE : null);
            image = null;
            croppedImage = null;
            imageDate = null;
            webCameraSource = false;
            cameraLive = false;
            localFile = false;
        }
    }

    public void cancelPhotoChanges() {
        image = null;
        imageDate = null;
        croppedImage = null;
        webCameraSource = false;
        cameraLive = false;
        localFile = false;
    }

    public void showCameraError() {
        cameraLive = false;
        addErrorMessage("Webcam is not configured or missing");
    }

    public String next() {
        try {
            fillCisNameCasesFromNominative(person);
            dropIncompleteAdditionalDocuments();
            dropBlankContacts();
            if (hasIncompleteContacts()) {
                addErrorMessage("Contacts data is not filled");
            }
            if (FacesContext.getCurrentInstance().getMessageList().isEmpty()) {
                documents = new ArrayList<>();
                documents.addAll(additionalDocuments);
                documents.add(mainDocument);
                accommodationPersonAddress = buildPersonAddress(accommodationPersonAddress);
                residentialPersonAddress = buildPersonAddress(residentialAddressAutoComplete, residentialPersonAddress, residentialAddressMatcher);
                correspondencePersonAddress =
                        buildPersonAddress(correspondenceAddressAutoComplete, correspondencePersonAddress, correspondenceAddressMatcher);
                personAddresses =
                        Stream.of(accommodationPersonAddress, residentialPersonAddress, correspondencePersonAddress)
                              .collect(Collectors.toList());
                personAddresses.addAll(externalPersonAddresses);
                personDiffContainer.setPerson(person);
                personDiffContainer.setContacts(contacts);
                personDiffContainer.setDocuments(documents);
                personDiffContainer.setPersonAddresses(personAddresses);
                person = personService.save(userSession.getUser(), personDiffContainer, removedContacts, removedDocuments, photo, removedPhoto);
                if (person != null && person.getId() != null) {
                    refreshFineractStatus();
                    personService.consumeFineractSyncWarning().ifPresent(this::addWarnMessage);
                }
                return Boolean.TRUE.equals(person.getTerrorist()) || Boolean.TRUE.equals(person.getFmsInvalid()) ? null : "next";
            }
        } catch (BackException e) {
            log.error(e.getMessage(), e);
            addErrorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data");
        }
        return null;
    }

    public String back() {
        return "back";
    }

    public void takePicture() {
        image = Base64.getDecoder().decode(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("image"));
        imageDate = Instant.now();
    }

    public void initCamera() {
        webCameraSource = true;
        cameraLive = true;
        localFile = false;
    }

    public void initUpload() {
        imageDate = Instant.now();
        localFile = true;
        webCameraSource = false;
        cameraLive = false;
    }

    public void changeCameraState() {
        cameraLive = !cameraLive;
        croppedImage = null;
    }

    public void removePhoto() {
        removedPhoto = personDiffContainer.getOldPhoto() == null ? null : photo;
        photo = null;
    }

    private PersonAddress getPersonAddressByType(List<PersonAddress> personAddresses, String type) {
        return personAddresses.stream().filter(e -> e.getType().equals(type)).findFirst().orElseGet(() -> {
            PersonAddress personAddressTmp = new PersonAddress();
            personAddressTmp.setType(type);
            personAddressTmp.setPerson(person);
            personAddressTmp.setAddress(new Address());
            return personAddressTmp;
        });
    }

    private Document getActualMainDocument() {
        return person.getDocuments().stream().filter(document -> Documents.NATIONAL_PASSPORT_CODE.equals(document.getType()) ||
                                                                 Documents.FOREIGN_CITIZEN_PASSPORT_CODE.equals(document.getType())).findAny()
                     .orElseGet(() -> {
                         Document mainDocumentTmp = new Document();
                         mainDocumentTmp.setPerson(person);
                         return mainDocumentTmp;
                     });
    }

    // CIS/SNG region: genitive/dative/instrumental name cases; ENG form uses nominative only
    private void fillCisNameCasesFromNominative(Person person) {
        String last = person.getLastname();
        String first = person.getFirstname();
        if (last != null) {
            if (person.getLastnameGenitive() == null) {
                person.setLastnameGenitive(last);
            }
            if (person.getLastnameDative() == null) {
                person.setLastnameDative(last);
            }
            if (person.getLastnameInstrumental() == null) {
                person.setLastnameInstrumental(last);
            }
        }
        if (first != null) {
            if (person.getFirstnameGenitive() == null) {
                person.setFirstnameGenitive(first);
            }
            if (person.getFirstnameDative() == null) {
                person.setFirstnameDative(first);
            }
            if (person.getFirstnameInstrumental() == null) {
                person.setFirstnameInstrumental(first);
            }
        }
    }

    private PersonAddress buildPersonAddress(AddressAutoComplete autoComplete, PersonAddress result, String matcher) {
        result.setAddress((Address) (matcher != null ? getAddressAutoComplete(matcher) : autoComplete).getAddress());
        return result;
    }


    private PersonAddress buildPersonAddress(PersonAddress result) {
        result.setAddress((Address) accommodationAddressAutoComplete.getAddress());
        return result;
    }

    private AddressAutoComplete getAddressAutoComplete(String matches) {
        AddressAutoComplete result;
        switch (matches) {
            case Addresses.CORRESPONDENCE_TYPE:
                result = correspondenceAddressAutoComplete;
                break;
            case Addresses.RESIDENTIAL_TYPE:
                result = residentialAddressAutoComplete;
                break;
            case Addresses.STAYING_TYPE:
                result = accommodationAddressAutoComplete;
                break;
            default:
                result = null;
                break;
        }
        return result;
    }
}
