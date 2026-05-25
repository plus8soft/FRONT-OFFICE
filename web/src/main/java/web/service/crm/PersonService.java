/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.configuration.Settings;
import web.entity.core.User;
import web.entity.crm.BaseDocument;
import web.entity.crm.Contact;
import web.entity.crm.Document;
import web.entity.crm.Document_;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.crm.Photo;
import web.integration.fineract.FineractIntegrationService;
import web.lob.entity.crm.LobPhoto;
import web.lob.repositories.LobPhotoRepository;
import web.repository.back.BackException;
import web.repository.back.crm.check.OutputPassportCheck;
import web.repository.back.crm.check.OutputTerroristCheck;
import web.repository.crm.AddressRepository;
import web.repository.crm.ContactRepository;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.crm.PersonRepository;
import web.repository.crm.PhotoRepository;
import web.service.back.PersonBackService;
import web.utils.Documents;
import web.view.ce.clientedit.PersonDiffContainer;

@Service
@Transactional
public class PersonService {

    @Autowired
    private Settings settings;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PersonBackService personBackService;

    @Autowired
    private ChangeService changeService;

    @Autowired
    private LobPhotoRepository lobPhotoRepository;

    @Autowired
    private FineractIntegrationService fineractIntegrationService;

    private Optional<String> fineractSyncWarning = Optional.empty();

    public Optional<String> consumeFineractSyncWarning() {
        Optional<String> warning = fineractSyncWarning;
        fineractSyncWarning = Optional.empty();
        return warning;
    }

    public Person receivePersonByMainDocument(User user, String type, String series, String number) {
        if (!settings.isBackEnabled()) {
            return findPersonInCrm(type, series, number);
        }
        Person person;
        try {
            person = findPersonInCrm(type, series, number);
            if (person == null) {
                person = personBackService.findPerson(user.getLogin(), type, series, number);
            }
        } catch (NonUniqueResultException e) {
            throw new BackException("More than one client found", e);
        }
        if (person != null) {
            syncPersonFromExternalSource(person);
        }
        return person;
    }

    private Person findPersonInCrm(String type, String series, String number) {
        try {
            return Optional.ofNullable(documentRepository.findOne((root, query, cb) -> {
                root.fetch(Document_.person);
                return cb.and(cb.equal(root.get(Document_.type), type),
                              series == null ? cb.isNull(root.get(Document_.series)) : cb.equal(root.get(Document_.series), series),
                              cb.equal(root.get(Document_.number), number));
            })).map(Document::getPerson).orElse(null);
        } catch (NonUniqueResultException e) {
            throw new BackException("More than one client found", e);
        }
    }

    private void syncPersonFromExternalSource(Person person) {
        personRepository.save(person);
        person.getPersonAddresses().forEach(personAddress -> {
            addressRepository.save(personAddress.getAddress());
            personAddressRepository.save(personAddress);
        });
        documentRepository.save(person.getDocuments());
        contactRepository.save(person.getContacts());
    }

    private OutputTerroristCheck checkOnTerrorist(String user, Long externalId) {
        if (!settings.isBackEnabled()) {
            return null;
        }
        return personBackService.checkOnTerrorist(user, externalId);
    }

    private OutputTerroristCheck checkOnTerrorist(String user, Person person, BaseDocument document) {
        if (!settings.isBackEnabled()) {
            return null;
        }
        return personBackService.checkOnTerrorist(user, person, document);
    }

    private OutputPassportCheck checkPassport(String user, BaseDocument document) {
        if (!settings.isBackEnabled()) {
            return null;
        }
        return personBackService.checkPassport(user, document);
    }

    public Person checkPerson(User user, Long externalId, Person person, BaseDocument document) {
        OutputTerroristCheck outputTerroristCheck;
        person.setTerrorist(
                (outputTerroristCheck = checkOnTerrorist(user.getLogin(), externalId)) == null ? null : outputTerroristCheck.getTerrorist());
        if (!Boolean.TRUE.equals(person.getTerrorist()) && document != null
                && Documents.NATIONAL_PASSPORT_CODE.equals(document.getType())) {
            OutputPassportCheck outputPassportCheck;
            person.setFmsInvalid(
                    (outputPassportCheck = checkPassport(user.getLogin(), document)) == null ? null : outputPassportCheck.getFmsInvalid());
        }
        return person;
    }

    private Person checkPerson(User user, Person person, BaseDocument document) {
        OutputTerroristCheck outputTerroristCheck;
        person.setTerrorist(
                (outputTerroristCheck = checkOnTerrorist(user.getLogin(), person, document)) == null ? null : outputTerroristCheck.getTerrorist());
        if (!Boolean.TRUE.equals(person.getTerrorist()) && document != null
                && Documents.NATIONAL_PASSPORT_CODE.equals(document.getType())) {
            OutputPassportCheck outputPassportCheck;
            person.setFmsInvalid(
                    (outputPassportCheck = checkPassport(user.getLogin(), document)) == null ? null : outputPassportCheck.getFmsInvalid());
        }
        return person;
    }

    public Person save(User user, PersonDiffContainer personDiffContainer, List<Contact> removedContacts, List<Document> removedDocuments,
                       Photo photo, Photo removedPhoto) {
        Person result = persistPerson(user, personDiffContainer, removedContacts, removedDocuments, photo, removedPhoto);
        fineractSyncWarning = Optional.empty();
        if (result != null && result.getId() != null
                && !Boolean.TRUE.equals(result.getTerrorist())
                && !Boolean.TRUE.equals(result.getFmsInvalid())) {
            fineractSyncWarning = fineractIntegrationService.syncPersonAfterSave(result);
        }
        return result;
    }

    private Person persistPerson(User user, PersonDiffContainer personDiffContainer, List<Contact> removedContacts, List<Document> removedDocuments,
                                 Photo photo, Photo removedPhoto) {
        Person saved;
        BaseDocument document = personDiffContainer.getDocuments().stream()
                                                   .filter(doc -> Documents.NATIONAL_PASSPORT_CODE.equals(doc.getType()) ||
                                                                  Documents.FOREIGN_CITIZEN_PASSPORT_CODE.equals(doc.getType())).findAny()
                                                   .orElse(null);
        Person checkedPerson = checkPerson(user, personDiffContainer.getPerson(), document);
        if (Boolean.TRUE.equals(checkedPerson.getTerrorist())) {
            saved = checkedPerson;
        } else if (Boolean.TRUE.equals(checkedPerson.getFmsInvalid())) {
            saved = checkedPerson;
        } else if (checkedPerson.getExternalId() == null) {
            personDiffContainer.getPerson().setDocuments(new ArrayList<>());
            personDiffContainer.getPerson().setPersonAddresses(new ArrayList<>());
            personDiffContainer.getPerson().setContacts(new ArrayList<>());
            Person person = personRepository.save(personDiffContainer.getPerson());
            if (photo != null) {
                photo.setPerson(person);
                lobPhotoRepository.save(new LobPhoto(photoRepository.save(photo).getId(), photo.getImage()));
            }
            personDiffContainer.getDocuments().forEach(doc -> {
                if (!removedDocuments.contains(doc)) {
                    doc.setPerson(person);
                    person.getDocuments().add(documentRepository.save(doc));
                }
            });
            personDiffContainer.getPersonAddresses().forEach(personAddress -> {
                personAddress.setPerson(person);
                personAddress.setAddress(addressRepository.save(personAddress.getAddress()));
                person.getPersonAddresses().add(personAddressRepository.save(personAddress));
            });
            personDiffContainer.getContacts().forEach(contact -> {
                if (!removedContacts.contains(contact)) {
                    contact.setPerson(person);
                    person.getContacts().add(contactRepository.save(contact));
                }
            });
            saved = settings.isBackEnabled()
                    ? personRepository.save(personBackService.createPerson(user.getLogin(), user.getDepartment().getExternalId(), person))
                    : personRepository.save(person);
            saved.setTerrorist(checkedPerson.getTerrorist());
            saved.setFmsInvalid(checkedPerson.getFmsInvalid());
            changeService.addHistoryLog(user, personDiffContainer, removedDocuments, removedContacts);
        } else {
            personDiffContainer.getPerson().setDocuments(new ArrayList<>());
            personDiffContainer.getPerson().setPersonAddresses(new ArrayList<>());
            personDiffContainer.getPerson().setContacts(new ArrayList<>());
            Person person = personRepository.save(personDiffContainer.getPerson());
            if (photo != null) {
                photo.setPerson(person);
                lobPhotoRepository.save(new LobPhoto(photoRepository.save(photo).getId(), photo.getImage()));
            } else if (removedPhoto != null) {
                photoRepository.delete(removedPhoto);
                lobPhotoRepository.delete(removedPhoto.getId());
            }
            documentRepository.delete(removedDocuments);
            documentRepository.save(personDiffContainer.getDocuments());
            contactRepository.delete(removedContacts);
            contactRepository.save(personDiffContainer.getContacts());
            addressRepository.save(personDiffContainer.getPersonAddresses().stream().map(PersonAddress::getAddress).collect(Collectors.toList()));
            personAddressRepository.save(personDiffContainer.getPersonAddresses());
            if (settings.isBackEnabled()) {
                personBackService.updatePerson(user.getLogin(), personDiffContainer.getPerson(), personDiffContainer.getPersonAddresses(),
                                               personDiffContainer.getDocuments(), personDiffContainer.getContacts(),
                                               personDiffContainer.getOldPerson(), personDiffContainer.getOldPersonAddresses(),
                                               personDiffContainer.getOldDocuments(), personDiffContainer.getOldContacts());
            }
            saved = person;
            saved.setTerrorist(checkedPerson.getTerrorist());
            saved.setFmsInvalid(checkedPerson.getFmsInvalid());
            changeService.addHistoryLog(user, personDiffContainer, removedDocuments, removedContacts);
        }
        return saved;
    }
}
