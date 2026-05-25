/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import web.entity.crm.Person;
import web.entity.dict.Currency;
import web.repository.back.crm.credit.OutputCreditInfo;
import web.repository.back.crm.deposit.OutputDepositInfo;
import web.service.back.CreditInfoWrapper;
import web.service.back.DepositInfoWrapper;

@Service
public class FineractProductService {

    private static final Logger LOG = LogManager.getLogger(FineractProductService.class);

    private static final DateTimeFormatter FINERACT_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    @Autowired
    private FineractIntegrationService fineractIntegrationService;

    @Autowired
    private FineractRestClient fineractRestClient;

    @Cacheable(value = "fineractDeposits", key = "#person.id", sync = true)
    public DepositInfoWrapper loadDeposits(Person person) {
        DepositInfoWrapper wrapper = new DepositInfoWrapper();
        wrapper.setCreationDateTime(Instant.now());
        if (person == null || person.getId() == null) {
            wrapper.setDeposits(Collections.emptyList());
            return wrapper;
        }
        Optional<Long> clientId = resolveFineractClientId(person, wrapper);
        if (!clientId.isPresent()) {
            return wrapper;
        }
        try {
            JsonObject response = fineractRestClient.get("/savingsaccounts?clientId=" + clientId.get());
            wrapper.setDeposits(mapSavingsAccounts(pageItems(response)));
        } catch (Exception e) {
            LOG.warn("Fineract savings load failed for person {}: {}", person.getId(), e.getMessage());
            wrapper.setErrorMessage("Could not load savings accounts from Fineract");
            wrapper.setDeposits(Collections.emptyList());
        }
        return wrapper;
    }

    @Cacheable(value = "fineractCredits", key = "#person.id", sync = true)
    public CreditInfoWrapper loadCredits(Person person) {
        CreditInfoWrapper wrapper = new CreditInfoWrapper();
        wrapper.setCreationDateTime(Instant.now());
        if (person == null || person.getId() == null) {
            wrapper.setCredits(Collections.emptyList());
            return wrapper;
        }
        Optional<Long> clientId = resolveFineractClientId(person, wrapper);
        if (!clientId.isPresent()) {
            return wrapper;
        }
        try {
            JsonObject response = fineractRestClient.get("/loans?clientId=" + clientId.get());
            wrapper.setCredits(mapLoans(pageItems(response)));
        } catch (Exception e) {
            LOG.warn("Fineract loans load failed for person {}: {}", person.getId(), e.getMessage());
            wrapper.setErrorMessage("Could not load loans from Fineract");
            wrapper.setCredits(Collections.emptyList());
        }
        return wrapper;
    }

    @CacheEvict(value = {"fineractDeposits", "fineractCredits"}, key = "#person.id", beforeInvocation = true)
    public void resetCache(Person person) {
    }

    private Optional<Long> resolveFineractClientId(Person person, DepositInfoWrapper wrapper) {
        if (!fineractIntegrationService.isEnabled()) {
            wrapper.setErrorMessage("Fineract integration is disabled");
            wrapper.setDeposits(Collections.emptyList());
            return Optional.empty();
        }
        if (!fineractIntegrationService.isAvailable()) {
            wrapper.setErrorMessage("Fineract is not reachable");
            wrapper.setDeposits(Collections.emptyList());
            return Optional.empty();
        }
        Optional<Long> clientId = fineractIntegrationService.findClientIdByPerson(person);
        if (!clientId.isPresent()) {
            wrapper.setErrorMessage("Client is not linked in Fineract yet");
            wrapper.setDeposits(Collections.emptyList());
        }
        return clientId;
    }

    private Optional<Long> resolveFineractClientId(Person person, CreditInfoWrapper wrapper) {
        if (!fineractIntegrationService.isEnabled()) {
            wrapper.setErrorMessage("Fineract integration is disabled");
            wrapper.setCredits(Collections.emptyList());
            return Optional.empty();
        }
        if (!fineractIntegrationService.isAvailable()) {
            wrapper.setErrorMessage("Fineract is not reachable");
            wrapper.setCredits(Collections.emptyList());
            return Optional.empty();
        }
        Optional<Long> clientId = fineractIntegrationService.findClientIdByPerson(person);
        if (!clientId.isPresent()) {
            wrapper.setErrorMessage("Client is not linked in Fineract yet");
            wrapper.setCredits(Collections.emptyList());
        }
        return clientId;
    }

    private static JsonArray pageItems(JsonObject response) {
        if (response == null || !response.has("pageItems") || !response.get("pageItems").isJsonArray()) {
            return new JsonArray();
        }
        return response.getAsJsonArray("pageItems");
    }

    private static List<OutputDepositInfo> mapSavingsAccounts(JsonArray items) {
        List<OutputDepositInfo> deposits = new ArrayList<>();
        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            OutputDepositInfo deposit = new OutputDepositInfo();
            deposit.setId(longValue(item, "id"));
            String externalId = textValue(item, "externalId");
            String product = firstNonBlank(textValue(item, "productName"), textValue(item, "savingsProductName"));
            deposit.setProductName(formatProductDisplayName(product, externalId));
            deposit.setNumber(textValue(item, "accountNo"));
            deposit.setInterestRate(doubleValue(item, "nominalAnnualInterestRate"));
            JsonObject timeline = objectValue(item, "timeline");
            if (timeline != null) {
                deposit.setDateOpen(parseFineractDate(timeline.get("submittedOnDate")));
                deposit.setDateClose(firstNonNull(parseFineractDate(timeline.get("expectedMaturityDate")),
                                                 parseFineractDate(timeline.get("closedOnDate"))));
            }
            deposit.setBalance(firstNonNull(decimalFromSummary(item, "accountBalance"), decimalValue(item, "accountBalance")));
            deposit.setCurrency(currencyFrom(item));
            if (isClosedAccount(item)) {
                if (deposit.getDateClose() == null) {
                    deposit.setDateClose(LocalDate.now().minusDays(1));
                }
            }
            deposits.add(deposit);
        }
        return deposits;
    }

    private static List<OutputCreditInfo> mapLoans(JsonArray items) {
        List<OutputCreditInfo> credits = new ArrayList<>();
        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            OutputCreditInfo credit = new OutputCreditInfo();
            credit.setId(longValue(item, "id"));
            String externalId = textValue(item, "externalId");
            String product = firstNonBlank(textValue(item, "productName"), textValue(item, "loanProductName"));
            credit.setProductName(formatProductDisplayName(product, externalId));
            credit.setNumber(textValue(item, "accountNo"));
            credit.setInterestRate(doubleValue(item, "annualInterestRate"));
            JsonObject timeline = objectValue(item, "timeline");
            if (timeline != null) {
                credit.setDateClose(firstNonNull(parseFineractDate(timeline.get("expectedMaturityDate")),
                                               parseFineractDate(timeline.get("closedOnDate"))));
            }
            credit.setDebt(firstNonNull(decimalFromSummary(item, "totalOutstanding"), decimalValue(item, "totalOutstanding")));
            credit.setPayAmount(firstNonNull(decimalFromSummary(item, "totalOverdue"), decimalFromSummary(item, "totalExpectedRepayment")));
            credit.setCurrency(currencyFrom(item));
            if (isClosedAccount(item)) {
                if (credit.getDateClose() == null) {
                    credit.setDateClose(LocalDate.now().minusDays(1));
                }
            }
            credits.add(credit);
        }
        return credits;
    }

    private static boolean isClosedAccount(JsonObject item) {
        JsonObject status = objectValue(item, "status");
        if (status == null) {
            return false;
        }
        String value = textValue(status, "value");
        if (value != null && value.toLowerCase(Locale.ENGLISH).contains("closed")) {
            return true;
        }
        String code = textValue(status, "code");
        return code != null && code.toLowerCase(Locale.ENGLISH).contains("closed");
    }

    private static Currency currencyFrom(JsonObject item) {
        JsonObject currency = objectValue(item, "currency");
        if (currency == null) {
            return null;
        }
        Currency result = new Currency();
        result.setId(firstNonBlank(textValue(currency, "code"), textValue(currency, "name")));
        result.setName(firstNonBlank(textValue(currency, "name"), textValue(currency, "code")));
        return result;
    }

    private static BigDecimal decimalFromSummary(JsonObject item, String field) {
        JsonObject summary = objectValue(item, "summary");
        return summary == null ? null : decimalValue(summary, field);
    }

    private static JsonObject objectValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || !item.get(field).isJsonObject()) {
            return null;
        }
        return item.getAsJsonObject(field);
    }

    private static String textValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return null;
        }
        return item.get(field).getAsString();
    }

    private static Long longValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return null;
        }
        return item.get(field).getAsLong();
    }

    private static Double doubleValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return null;
        }
        return item.get(field).getAsDouble();
    }

    private static BigDecimal decimalValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return null;
        }
        return item.get(field).getAsBigDecimal();
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

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static String formatProductDisplayName(String product, String externalId) {
        String name = product != null && !product.trim().isEmpty() ? product.trim() : null;
        String reference = externalId != null && !externalId.trim().isEmpty() ? externalId.trim() : null;
        if (name == null && reference == null) {
            return null;
        }
        if (name == null) {
            return reference;
        }
        if (reference == null) {
            return name;
        }
        return name + " (" + reference + ")";
    }
}
