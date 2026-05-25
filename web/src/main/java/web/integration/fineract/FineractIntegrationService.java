/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.configuration.Settings;
import web.entity.crm.Contact;
import web.entity.crm.Person;
import web.repository.crm.ContactRepository;
import web.repository.crm.PersonRepository;
import web.utils.Contacts;

@Service
public class FineractIntegrationService {

    private static final Logger LOG = LogManager.getLogger(FineractIntegrationService.class);

    static final String EXTERNAL_ID_PREFIX = "FO-";

    /** Shown in UI when CRM save succeeded but Fineract sync failed. */
    public static final String SYNC_USER_WARNING = "Client saved. Fineract was not updated — try again later.";

    public static final String SYNC_USER_SUCCESS = "Client synced to Fineract.";

    public static final String IMPORT_USER_SUCCESS = "Client imported from Fineract into CRM.";

    public static final String LINK_USER_SUCCESS = "Client linked to Fineract.";

    private static final DateTimeFormatter FINERACT_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private static final int SEARCH_LIMIT = 20;

    @Autowired
    private Settings settings;

    @Autowired
    private FineractRestClient fineractRestClient;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ContactRepository contactRepository;

    public boolean isEnabled() {
        return settings.isFineractEnabled() && fineractRestClient.isConfigured();
    }

    public String externalIdForPerson(Person person) {
        return EXTERNAL_ID_PREFIX + person.getId();
    }

    public boolean isAvailable() {
        if (!isEnabled()) {
            return false;
        }
        try {
            fineractRestClient.get("/offices");
            return true;
        } catch (Exception e) {
            LOG.warn("Fineract health check failed: {}", e.getMessage());
            return false;
        }
    }

    public FineractClientStatus resolveStatus(Person person) {
        if (!settings.isFineractEnabled()) {
            return FineractClientStatus.disabled();
        }
        if (!fineractRestClient.isConfigured()) {
            return FineractClientStatus.notConfigured();
        }
        if (person == null || person.getId() == null) {
            return FineractClientStatus.notSynced(null);
        }
        String externalId = externalIdForPerson(person);
        if (!isAvailable()) {
            return FineractClientStatus.unavailable(null);
        }
        try {
            Optional<Long> clientId = findClientIdByPerson(person);
            if (!clientId.isPresent()) {
                return FineractClientStatus.notSynced(externalId);
            }
            return FineractClientStatus.synced(clientId.get(), externalId);
        } catch (Exception e) {
            LOG.warn("Fineract status check failed for person {}: {}", person.getId(), e.getMessage());
            return FineractClientStatus.syncError(e.getMessage());
        }
    }

    public Optional<JsonObject> getClient(long fineractClientId) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        return Optional.of(fineractRestClient.get("/clients/" + fineractClientId));
    }

    public Optional<Long> findClientIdByPerson(Person person) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        JsonObject response = fineractRestClient.get("/clients?externalId=" + encode(externalIdForPerson(person)));
        JsonArray pageItems = response.has("pageItems") ? response.getAsJsonArray("pageItems") : null;
        if (pageItems == null || pageItems.size() == 0) {
            return Optional.empty();
        }
        JsonObject client = pageItems.get(0).getAsJsonObject();
        if (!client.has("id")) {
            return Optional.empty();
        }
        return Optional.of(client.get("id").getAsLong());
    }

    public long createClient(Person person) {
        if (!isEnabled()) {
            throw new FineractException("Fineract integration is disabled");
        }
        LocalDate today = LocalDate.now();
        String todayFormatted = today.format(FINERACT_DATE);
        JsonObject body = buildClientBody(person);
        body.addProperty("officeId", settings.getFineractOfficeId());
        body.addProperty("legalFormId", settings.getFineractLegalFormId());
        body.addProperty("externalId", externalIdForPerson(person));
        body.addProperty("active", true);
        body.addProperty("activationDate", todayFormatted);
        body.addProperty("submittedOnDate", todayFormatted);

        JsonObject response = fineractRestClient.post("/clients", body);
        return extractClientId(response);
    }

    public void updateClient(Person person, long fineractClientId) {
        if (!isEnabled()) {
            throw new FineractException("Fineract integration is disabled");
        }
        fineractRestClient.put("/clients/" + fineractClientId, buildClientBody(person));
    }

    /**
     * Search Fineract clients and return only those not linked to an existing CRM person ({@code FO-{id}}).
     */
    public List<FineractClientHit> searchClientsNotInCrm(String query) {
        if (!isEnabled()) {
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, FineractClientHit> unique = new LinkedHashMap<>();
        for (FineractClientHit hit : searchClients(query.trim())) {
            if (!isLinkedInCrm(hit.getExternalId())) {
                unique.putIfAbsent(hit.getFineractClientId(), hit);
            }
        }
        return new ArrayList<>(unique.values());
    }

    public List<FineractClientHit> searchClients(String query) {
        if (!isEnabled()) {
            return Collections.emptyList();
        }
        JsonObject response = fineractRestClient.get("/clients?search=" + encode(query) + "&limit=" + SEARCH_LIMIT);
        return parseClientHits(response);
    }

    public boolean isLinkedInCrm(String fineractExternalId) {
        return resolveCrmPersonId(fineractExternalId).isPresent();
    }

    public Person importClientToCrm(long fineractClientId) {
        if (!isEnabled()) {
            throw new FineractException("Fineract integration is disabled");
        }
        JsonObject client = getClient(fineractClientId)
                .orElseThrow(() -> new FineractException("Fineract client #" + fineractClientId + " not found"));
        FineractClientHit hit = mapClientHit(client);
        if (isLinkedInCrm(hit.getExternalId())) {
            throw new FineractException("Fineract client is already linked to CRM (" + hit.getExternalId() + ")");
        }
        Person person = mapToPerson(client);
        person = personRepository.save(person);
        saveMobileContact(person, hit.getMobileNo());
        linkFineractClientToPerson(fineractClientId, person);
        LOG.info("Imported Fineract client {} into CRM person {}", fineractClientId, person.getId());
        return personRepository.findOne(person.getId());
    }

    public void linkFineractClientToPerson(long fineractClientId, Person person) {
        if (!isEnabled()) {
            throw new FineractException("Fineract integration is disabled");
        }
        if (person == null || person.getId() == null) {
            throw new FineractException("Save the client in CRM before linking to Fineract");
        }
        JsonObject client = getClient(fineractClientId)
                .orElseThrow(() -> new FineractException("Fineract client #" + fineractClientId + " not found"));
        String externalId = textValue(client, "externalId");
        if (isLinkedInCrm(externalId) && !externalIdForPerson(person).equals(externalId)) {
            throw new FineractException("Fineract client is already linked to another CRM person (" + externalId + ")");
        }
        Optional<Long> existingForPerson = findClientIdByPerson(person);
        if (existingForPerson.isPresent() && !existingForPerson.get().equals(fineractClientId)) {
            throw new FineractException("CRM client is already linked to Fineract client #" + existingForPerson.get());
        }
        JsonObject body = buildClientBody(person);
        body.addProperty("externalId", externalIdForPerson(person));
        fineractRestClient.put("/clients/" + fineractClientId, body);
        LOG.info("Linked Fineract client {} to CRM person {} ({})", fineractClientId, person.getId(), externalIdForPerson(person));
    }

    /**
     * Best-effort sync after CRM save. Does not modify {@link Person#getExternalId()} (legacy back-office id).
     */
    public Optional<String> syncPersonAfterSave(Person person) {
        if (!isEnabled() || person == null || person.getId() == null) {
            return Optional.empty();
        }
        try {
            Optional<Long> existingId = findClientIdByPerson(person);
            if (existingId.isPresent()) {
                updateClient(person, existingId.get());
                LOG.info("Updated Fineract client {} for person {} ({})", existingId.get(), person.getId(),
                         externalIdForPerson(person));
            } else {
                long clientId = createClient(person);
                LOG.info("Created Fineract client {} for person {} ({})", clientId, person.getId(), externalIdForPerson(person));
            }
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Fineract sync failed for person {}: {}", person.getId(), e.getMessage(), e);
            return Optional.of(SYNC_USER_WARNING);
        }
    }

    private JsonObject buildClientBody(Person person) {
        JsonObject body = new JsonObject();
        body.addProperty("firstname", nullToEmpty(person.getFirstname()));
        body.addProperty("lastname", nullToEmpty(person.getLastname()));
        if (person.getPatronymic() != null && !person.getPatronymic().trim().isEmpty()) {
            body.addProperty("middlename", person.getPatronymic());
        }
        body.addProperty("dateFormat", "dd MMMM yyyy");
        body.addProperty("locale", "en");
        if (person.getBirthDate() != null) {
            body.addProperty("dateOfBirth", person.getBirthDate().format(FINERACT_DATE));
        }
        return body;
    }

    private static long extractClientId(JsonObject response) {
        if (response.has("clientId")) {
            return response.get("clientId").getAsLong();
        }
        if (response.has("resourceId")) {
            return response.get("resourceId").getAsLong();
        }
        throw new FineractException("Fineract client creation response has no clientId: " + response);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Optional<Long> resolveCrmPersonId(String fineractExternalId) {
        if (fineractExternalId == null || !fineractExternalId.startsWith(EXTERNAL_ID_PREFIX)) {
            return Optional.empty();
        }
        try {
            long personId = Long.parseLong(fineractExternalId.substring(EXTERNAL_ID_PREFIX.length()));
            return personRepository.findOne(personId) != null ? Optional.of(personId) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private List<FineractClientHit> parseClientHits(JsonObject response) {
        if (response == null || !response.has("pageItems")) {
            return Collections.emptyList();
        }
        JsonArray pageItems = response.getAsJsonArray("pageItems");
        List<FineractClientHit> hits = new ArrayList<>();
        for (int i = 0; i < pageItems.size(); i++) {
            hits.add(mapClientHit(pageItems.get(i).getAsJsonObject()));
        }
        return hits;
    }

    private FineractClientHit mapClientHit(JsonObject client) {
        return new FineractClientHit(
                longValue(client, "id"),
                textValue(client, "displayName"),
                textValue(client, "firstname"),
                textValue(client, "lastname"),
                textValue(client, "middlename"),
                textValue(client, "externalId"),
                textValue(client, "accountNo"),
                textValue(client, "mobileNo"),
                parseFineractDate(client.get("dateOfBirth")));
    }

    private Person mapToPerson(JsonObject client) {
        Person person = new Person();
        person.setFirstname(textValue(client, "firstname"));
        person.setLastname(textValue(client, "lastname"));
        person.setPatronymic(textValue(client, "middlename"));
        person.setBirthDate(parseFineractDate(client.get("dateOfBirth")));
        return person;
    }

    private void saveMobileContact(Person person, String mobileNo) {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            return;
        }
        Contact contact = new Contact();
        contact.setPerson(person);
        contact.setType(Contacts.MOBILE_PHONE_TYPE);
        contact.setData(mobileNo.trim());
        contactRepository.save(contact);
    }

    private static Long longValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return null;
        }
        return item.get(field).getAsLong();
    }

    private static String textValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return null;
        }
        String value = item.get(field).getAsString();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static LocalDate parseFineractDate(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() >= 3) {
                return LocalDate.of(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
            }
            return null;
        }
        if (element.isJsonPrimitive()) {
            String value = element.getAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(value, FINERACT_DATE);
            } catch (Exception ignored) {
                try {
                    return LocalDate.parse(value);
                } catch (Exception ignoredAgain) {
                    return null;
                }
            }
        }
        return null;
    }
}
