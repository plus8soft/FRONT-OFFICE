/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.User;
import web.entity.crm.Contact;
import web.entity.crm.Contact_;
import web.entity.crm.Document;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.crm.PersonAddress_;
import web.repository.crm.ContactRepository;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.utils.Addresses;
import web.utils.Documents;

@Configurable
public class BasicDataValidator implements PersonValidator {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Override
    @Transactional
    public void validate(User user, Person person) throws PersonValidateException {
        List<String> details = new ArrayList<>();
        if (person.getLastname() == null) {
            details.add("-field \"Last Name\" is not filled (basic information)");
        }
        if (person.getFirstname() == null) {
            details.add("-field \"First Name\" is not filled (basic information)");
        }
        if (person.getResidentCountry() == null) {
            details.add("-field \"Resident\" is not filled (basic information)");
        }
        if (person.getBirthDate() == null) {
            details.add("-field \"Birth Date\" is not filled (basic information)");
        }
        if (person.getGender() == null) {
            details.add("-field \"Gender\" is not filled (basic information)");
        }
        if (person.getBirthPlace() == null) {
            details.add("-field \"Birth Place\" is not filled (basic information)");
        }
        if (person.getBirthCountry() == null) {
            details.add("-field \"Birth Country\" is not filled (basic information)");
        }
        if (person.getCitizenship() == null) {
            details.add("-field \"Citizenship\" is not filled (basic information)");
        }
        Document mainDocument = documentRepository.findMain(person);
        if (Documents.NATIONAL_PASSPORT_CODE.equals(mainDocument.getType())) {
            if (mainDocument.getSeries() == null) {
                details.add("-field \"Series\" is not filled (identity document)");
            }
            if (mainDocument.getIssuanceUnitCode() == null) {
                details.add("-field \"Issuance Unit Code\" is not filled (identity document)");
            }
        }
        if (mainDocument.getNumber() == null) {
            details.add("-field \"Number\" is not filled (identity document)");
        }
        if (mainDocument.getIssuanceUnit() == null) {
            details.add("-field \"Issuance Place\" is not filled (identity document)");
        }
        if (mainDocument.getIssuanceDate() == null) {
            details.add("-field \"Issuance Date\" is not filled (identity document)");
        }
        // CIS/SNG region: additional documents — not required in ENG build (panel hidden on edit-client form)
        // CIS/SNG region: temporary (staying) address — not required in ENG build
        PersonAddress residentialPersonAddress = findPersonAddress(person, Addresses.RESIDENTIAL_TYPE);
        if (residentialPersonAddress == null) {
            details.add("-residential address data is not filled");
        } else if (residentialPersonAddress.getMatchType() == null
                && residentialPersonAddress.getAddress().getCountry() == null) {
            details.add("-field \"Country\" is not filled (residential address)");
        }
        List<Contact> contacts = contactRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get(Contact_.person), person)));
        if (contacts.isEmpty()) {
            details.add("-\"Contacts\" are not filled (specify at least one)");
        } else {
            contacts.forEach(contact -> {
                if (contact.getType() == null) {
                    details.add("-field \"Contact Type\" is not filled (contact information)");
                }
                if (contact.getData() == null) {
                    details.add("-field \"Contact\" is not filled (contact information)");
                }
            });
        }
        if (!details.isEmpty()) {
            throw new PersonValidateException("Incomplete form data", details);
        }
    }

    private PersonAddress findPersonAddress(Person person, String type) {
        return personAddressRepository.findOne((root, query, cb) -> {
            root.fetch(PersonAddress_.address);
            return cb.and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), type));
        });
    }
}
