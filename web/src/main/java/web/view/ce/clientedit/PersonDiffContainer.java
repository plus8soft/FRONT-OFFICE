/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientedit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.crm.Address;
import web.entity.crm.Contact;
import web.entity.crm.Document;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.entity.crm.Photo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonDiffContainer implements Serializable {

    private Person person;

    private List<Document> documents;

    private List<Contact> contacts;

    private List<PersonAddress> personAddresses;

    private Person oldPerson;

    private List<Document> oldDocuments;

    private List<Contact> oldContacts;

    private List<PersonAddress> oldPersonAddresses;

    private Photo oldPhoto;

    public PersonDiffContainer(Person person, List<PersonAddress> personAddresses) {
        oldPerson = new Person();
        oldPerson.setId(person.getId());
        oldPerson.setLastname(person.getLastname());
        oldPerson.setFirstname(person.getFirstname());
        oldPerson.setPatronymic(person.getPatronymic());
        oldPerson.setLastnameDative(person.getLastnameDative());
        oldPerson.setFirstnameDative(person.getFirstnameDative());
        oldPerson.setPatronymicDative(person.getPatronymicDative());
        oldPerson.setLastnameGenitive(person.getLastnameGenitive());
        oldPerson.setFirstnameGenitive(person.getFirstnameGenitive());
        oldPerson.setPatronymicGenitive(person.getPatronymicGenitive());
        oldPerson.setLastnameInstrumental(person.getLastnameInstrumental());
        oldPerson.setFirstnameInstrumental(person.getFirstnameInstrumental());
        oldPerson.setPatronymicInstrumental(person.getPatronymicInstrumental());
        oldPerson.setGender(person.getGender());
        oldPerson.setBirthCountry(person.getBirthCountry());
        oldPerson.setBirthPlace(person.getBirthPlace());
        oldPerson.setBirthDate(person.getBirthDate());
        oldPerson.setBusinessType(person.getBusinessType());
        oldPerson.setCitizenship(person.getCitizenship());
        oldPerson.setResidentCountry(person.getResidentCountry());
        oldPerson.setEin(person.getEin());
        oldPerson.setStatus(person.getStatus());
        oldPerson.setAttractionSource(person.getAttractionSource());
        oldPhoto = person.getPhoto() == null ? null :
                   new Photo(person.getPhoto().getId(), person.getPhoto().getImage(), person.getPhoto().getDateTime(),
                             person.getPhoto().getAdditionMethod(), person);
        oldDocuments = new ArrayList<>();
        person.getDocuments().forEach(document -> {
            Document newDocument = new Document();
            newDocument.setId(document.getId());
            newDocument.setIssuanceUnitCode(document.getIssuanceUnitCode());
            newDocument.setIssuanceUnit(document.getIssuanceUnit());
            newDocument.setNumber(document.getNumber());
            newDocument.setSeries(document.getSeries());
            newDocument.setType(document.getType());
            newDocument.setValidUntilDate(document.getValidUntilDate());
            newDocument.setIssuanceDate(document.getIssuanceDate());
            oldDocuments.add(newDocument);
        });
        oldContacts = new ArrayList<>();
        person.getContacts().forEach(contact -> {
            Contact newContact = new Contact();
            newContact.setId(contact.getId());
            newContact.setType(contact.getType());
            newContact.setData(contact.getData());
            newContact.setDescription(contact.getDescription());
            newContact.setMain(contact.isMain());
            newContact.setNotification(contact.isNotification());
            oldContacts.add(newContact);
        });
        oldPersonAddresses = new ArrayList<>();
        personAddresses.forEach(personAddress -> {
            PersonAddress newPersonAddress = new PersonAddress();
            newPersonAddress.setType(personAddress.getType());
            Address address = personAddress.getAddress();
            Address newAddress = new Address();
            newAddress.setId(address.getId());
            newAddress.setCode(address.getCode());
            newAddress.setCountry(address.getCountry());
            newAddress.setPostalCode(address.getPostalCode());
            newAddress.setRegionType(address.getRegionType());
            newAddress.setRegion(address.getRegion());
            newAddress.setDistrictType(address.getDistrictType());
            newAddress.setDistrict(address.getDistrict());
            newAddress.setCityType(address.getCityType());
            newAddress.setCity(address.getCity());
            newAddress.setLocalityType(address.getLocalityType());
            newAddress.setLocality(address.getLocality());
            newAddress.setStreetType(address.getStreetType());
            newAddress.setStreet(address.getStreet());
            newAddress.setHouse(address.getHouse());
            newAddress.setHousing(address.getHousing());
            newAddress.setStructure(address.getStructure());
            newAddress.setFlat(address.getFlat());
            newPersonAddress.setId(personAddress.getId());
            newPersonAddress.setAddress(newAddress);
            newPersonAddress.setMatchType(personAddress.getMatchType());
            oldPersonAddresses.add(newPersonAddress);
        });
    }
}
