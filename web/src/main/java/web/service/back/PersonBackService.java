/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import web.entity.crm.BaseDocument;
import web.entity.crm.Contact;
import web.entity.crm.Document;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress;
import web.repository.back.BackException;
import web.repository.back.crm.check.OutputPassportCheck;
import web.repository.back.crm.check.OutputTerroristCheck;

/**
 * Stub back-service for client (person) data — search, create, update,
 * sanctions / passport checks, and product (deposit / credit) lookup.
 *
 * <p>All methods that need a banking core throw {@link BackException}
 * with {@link BackIntegrationMessages#CORE_NOT_CONNECTED}. {@link #findDeposits}
 * and {@link #findCredits} return a populated wrapper with an error message
 * instead of throwing so the products tabs in the UI keep rendering cleanly.
 *
 * <p>Replace the bodies with calls to your core (REST / SOAP / JDBC / etc.)
 * to bring real client integration online. Keep method signatures unchanged.
 */
@Service
@Log4j2
public class PersonBackService {

    public Person createPerson(String userName, Long departmentId, Person person) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public void updatePerson(String userName, Person person, List<PersonAddress> personAddresses, List<Document> documents, List<Contact> contacts,
                             Person oldPerson, List<PersonAddress> oldPersonAddresses, List<Document> oldDocuments, List<Contact> oldContacts) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public DepositInfoWrapper findDeposits(String userName, Person person) {
        DepositInfoWrapper depositInfoWrapper = new DepositInfoWrapper();
        depositInfoWrapper.setCreationDateTime(Instant.now());
        depositInfoWrapper.setErrorMessage(BackIntegrationMessages.CORE_NOT_CONNECTED);
        return depositInfoWrapper;
    }

    public CreditInfoWrapper findCredits(String userName, Person person) {
        CreditInfoWrapper creditInfoWrapper = new CreditInfoWrapper();
        creditInfoWrapper.setCreationDateTime(Instant.now());
        creditInfoWrapper.setErrorMessage(BackIntegrationMessages.CORE_NOT_CONNECTED);
        return creditInfoWrapper;
    }

    public Person findPerson(String userName, String type, String series, String number) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public OutputTerroristCheck checkOnTerrorist(String user, Long externalId) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public OutputTerroristCheck checkOnTerrorist(String user, Person person, BaseDocument document) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public OutputPassportCheck checkPassport(String user, BaseDocument document) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }
}
