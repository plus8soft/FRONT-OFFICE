/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
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
import org.springframework.stereotype.Service;
import web.configuration.Settings;
import web.entity.crm.Person;

@Service
public class FineractProductCommandService {

    private static final Logger LOG = LogManager.getLogger(FineractProductCommandService.class);

    private static final DateTimeFormatter FINERACT_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private static final int DEFAULT_PAYMENT_TYPE_ID = 1;

    private static final String DEFAULT_LOAN_STRATEGY = "mifos-standard-strategy";

    @Autowired
    private FineractIntegrationService fineractIntegrationService;

    @Autowired
    private FineractRestClient fineractRestClient;

    @Autowired
    private FineractProductService fineractProductService;

    @Autowired
    private Settings settings;

    public Optional<Long> resolveDefaultSavingsProductId() {
        if (settings.getFineractDefaultSavingsProductId() != null && settings.getFineractDefaultSavingsProductId() > 0) {
            return Optional.of(settings.getFineractDefaultSavingsProductId());
        }
        List<FineractProductOption> products = listSavingsProducts();
        if (products.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(products.get(0).getId());
    }

    public Optional<Long> resolveDefaultLoanProductId() {
        if (settings.getFineractDefaultLoanProductId() != null && settings.getFineractDefaultLoanProductId() > 0) {
            return Optional.of(settings.getFineractDefaultLoanProductId());
        }
        List<FineractProductOption> products = listLoanProducts();
        if (products.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(products.get(0).getId());
    }

    public List<FineractProductOption> listSavingsProducts() {
        if (!fineractIntegrationService.isEnabled()) {
            return Collections.emptyList();
        }
        try {
            JsonObject response = fineractRestClient.get("/savingsproducts");
            return mapProductOptions(pageItems(response), "name");
        } catch (Exception e) {
            LOG.warn("Failed to load Fineract savings products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<FineractProductOption> listLoanProducts() {
        if (!fineractIntegrationService.isEnabled()) {
            return Collections.emptyList();
        }
        try {
            JsonObject response = fineractRestClient.get("/loanproducts");
            return mapProductOptions(pageItems(response), "name");
        } catch (Exception e) {
            LOG.warn("Failed to load Fineract loan products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<String> openSavingsAccount(Person person, FineractNewSavingsTerms terms) {
        if (terms == null) {
            return Optional.of("Savings terms are not loaded.");
        }
        Optional<String> validation = terms.validate();
        if (validation.isPresent()) {
            return validation;
        }
        Optional<String> personValidation = validatePersonAndProduct(person, terms.getProductId());
        if (personValidation.isPresent()) {
            return personValidation;
        }
        try {
            long clientId = requireClientId(person);
            LocalDate businessDate = resolveBusinessDate(clientId);
            JsonObject body = new JsonObject();
            body.addProperty("clientId", clientId);
            body.addProperty("productId", terms.getProductId());
            if (terms.getNominalAnnualInterestRate() != null) {
                body.addProperty("nominalAnnualInterestRate", terms.getNominalAnnualInterestRate().toPlainString());
            }
            if (terms.getExternalReference() != null && !terms.getExternalReference().trim().isEmpty()) {
                body.addProperty("externalId", terms.getExternalReference().trim());
            }
            addFineractDate(body, "submittedOnDate", businessDate);
            long savingsId = resourceId(fineractRestClient.post("/savingsaccounts", body));
            if (terms.getNominalAnnualInterestRate() != null) {
                applySavingsInterestRateBeforeApproval(savingsId, terms.getNominalAnnualInterestRate(), businessDate);
            }
            postDatedCommand("/savingsaccounts/" + savingsId + "?command=approve", businessDate, "approvedOnDate");
            postDatedCommand("/savingsaccounts/" + savingsId + "?command=activate", businessDate, "activatedOnDate");
            fineractProductService.resetCache(person);
            return Optional.empty();
        } catch (Exception e) {
            return failure("open savings account", e);
        }
    }

    public FineractNewSavingsTerms loadSavingsTermsDefault(Long productId) {
        FineractNewSavingsTerms terms = new FineractNewSavingsTerms();
        terms.setProductId(productId);
        if (productId == null || !fineractIntegrationService.isEnabled()) {
            return terms;
        }
        try {
            JsonObject product = fineractRestClient.get("/savingsproducts/" + productId);
            terms.setNominalAnnualInterestRate(decimalValue(product, "nominalAnnualInterestRate", null));
        } catch (Exception e) {
            LOG.warn("Failed to load Fineract savings product {}: {}", productId, e.getMessage());
        }
        return terms;
    }

    public Optional<String> deposit(Person person, Long savingsAccountId, BigDecimal amount) {
        return savingsTransaction(person, savingsAccountId, amount, "deposit");
    }

    public Optional<String> withdraw(Person person, Long savingsAccountId, BigDecimal amount) {
        return savingsTransaction(person, savingsAccountId, amount, "withdrawal");
    }

    public Optional<String> openLoan(Person person, FineractNewLoanTerms terms) {
        if (terms == null) {
            return Optional.of("Loan terms are not loaded.");
        }

        Optional<String> validation = terms.validate();
        if (validation.isPresent()) {
            return validation;
        }
        Optional<String> personValidation = validatePersonAndProduct(person, terms.getProductId());
        if (personValidation.isPresent()) {
            return personValidation;
        }
        try {
            long clientId = requireClientId(person);
            LocalDate businessDate = resolveBusinessDate(clientId);
            JsonObject body = buildLoanBody(clientId, terms, businessDate);
            long loanId = resourceId(fineractRestClient.post("/loans", body));
            postDatedCommand("/loans/" + loanId + "?command=approve", businessDate, "approvedOnDate");
            JsonObject disburseBody = new JsonObject();
            addFineractDate(disburseBody, "actualDisbursementDate", businessDate);
            disburseBody.addProperty("transactionAmount", terms.getPrincipal().toPlainString());
            disburseBody.addProperty("paymentTypeId", DEFAULT_PAYMENT_TYPE_ID);
            addLocaleAndFormat(disburseBody);
            fineractRestClient.post("/loans/" + loanId + "?command=disburse", disburseBody);
            fineractProductService.resetCache(person);
            return Optional.empty();
        } catch (Exception e) {
            return failure("open loan", e);
        }
    }

    public FineractNewLoanTerms loadLoanTermsDefault(Long productId) {
        FineractNewLoanTerms terms = new FineractNewLoanTerms();
        terms.setProductId(productId);
        if (productId == null || !fineractIntegrationService.isEnabled()) {
            return terms;
        }
        try {
            JsonObject product = fineractRestClient.get("/loanproducts/" + productId);
            terms.setPrincipal(new BigDecimal("5000"));
            terms.setNumberOfRepayments(intValue(product, "numberOfRepayments", 12));
            terms.setRepaymentEvery(intValue(product, "repaymentEvery", 1));
            terms.setRepaymentFrequencyType(intValue(product, "repaymentFrequencyType", 2));
            terms.setInterestRatePerPeriod(decimalValue(product, "interestRatePerPeriod", BigDecimal.ZERO));
            terms.setInterestRateFrequencyType(intValue(product, "interestRateFrequencyType", 2));
            terms.setAmortizationType(intValue(product, "amortizationType", 1));
            terms.setInterestType(intValue(product, "interestType", 0));
            terms.setInterestCalculationPeriodType(intValue(product, "interestCalculationPeriodType", 1));
            String strategy = firstNonBlank(textValue(product, "transactionProcessingStrategyCode"),
                                            nestedCode(product, "transactionProcessingStrategy"));
            terms.setTransactionProcessingStrategyCode(strategy == null ? DEFAULT_LOAN_STRATEGY : strategy);
        } catch (Exception e) {
            LOG.warn("Failed to load Fineract loan product {}: {}", productId, e.getMessage());
            terms.setPrincipal(new BigDecimal("5000"));
            terms.setNumberOfRepayments(12);
            terms.setRepaymentEvery(1);
            terms.setRepaymentFrequencyType(2);
            terms.setInterestRatePerPeriod(BigDecimal.ZERO);
            terms.setInterestRateFrequencyType(2);
            terms.setAmortizationType(1);
            terms.setInterestType(0);
            terms.setInterestCalculationPeriodType(1);
            terms.setTransactionProcessingStrategyCode(DEFAULT_LOAN_STRATEGY);
        }
        return terms;
    }

    private Optional<String> savingsTransaction(Person person, Long savingsAccountId, BigDecimal amount, String command) {
        if (person == null || person.getId() == null) {
            return Optional.of("Client is not loaded.");
        }
        if (savingsAccountId == null) {
            return Optional.of("Select a savings account first.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("Enter an amount greater than zero.");
        }
        if (!fineractIntegrationService.isEnabled()) {
            return Optional.of("Fineract integration is disabled.");
        }
        try {
            long clientId = requireClientId(person);
            LocalDate businessDate = resolveBusinessDate(clientId);
            JsonObject body = new JsonObject();
            addFineractDate(body, "transactionDate", businessDate);
            body.addProperty("transactionAmount", amount.toPlainString());
            body.addProperty("paymentTypeId", DEFAULT_PAYMENT_TYPE_ID);
            addLocaleAndFormat(body);
            fineractRestClient.post("/savingsaccounts/" + savingsAccountId + "/transactions?command=" + command, body);
            fineractProductService.resetCache(person);
            return Optional.empty();
        } catch (Exception e) {
            return failure(command + " savings", e);
        }
    }

    private JsonObject buildLoanBody(long clientId, FineractNewLoanTerms terms, LocalDate businessDate) {
        JsonObject body = new JsonObject();
        body.addProperty("clientId", clientId);
        body.addProperty("productId", terms.getProductId());
        body.addProperty("principal", terms.getPrincipal().toPlainString());
        body.addProperty("loanType", "individual");
        body.addProperty("numberOfRepayments", terms.getNumberOfRepayments());
        body.addProperty("repaymentEvery", terms.getRepaymentEvery());
        body.addProperty("repaymentFrequencyType", terms.getRepaymentFrequencyType());
        body.addProperty("interestRatePerPeriod", terms.getInterestRatePerPeriod().toPlainString());
        body.addProperty("interestRateFrequencyType", terms.getInterestRateFrequencyType());
        body.addProperty("amortizationType", terms.getAmortizationType());
        body.addProperty("interestType", terms.getInterestType());
        body.addProperty("interestCalculationPeriodType", terms.getInterestCalculationPeriodType());
        int loanTerm = terms.getNumberOfRepayments() * terms.getRepaymentEvery();
        body.addProperty("loanTermFrequency", loanTerm);
        body.addProperty("loanTermFrequencyType", terms.getRepaymentFrequencyType());
        body.addProperty("transactionProcessingStrategyCode",
                         terms.getTransactionProcessingStrategyCode() == null ? DEFAULT_LOAN_STRATEGY
                                 : terms.getTransactionProcessingStrategyCode());
        if (terms.getExternalReference() != null && !terms.getExternalReference().trim().isEmpty()) {
            body.addProperty("externalId", terms.getExternalReference().trim());
        }
        addFineractDate(body, "submittedOnDate", businessDate);
        addFineractDate(body, "expectedDisbursementDate", businessDate);
        addLocaleAndFormat(body);
        return body;
    }

    private void postDatedCommand(String path, LocalDate businessDate, String dateField) {
        JsonObject body = new JsonObject();
        addFineractDate(body, dateField, businessDate);
        addLocaleAndFormat(body);
        fineractRestClient.post(path, body);
    }

    private void applySavingsInterestRateBeforeApproval(long savingsId, BigDecimal interestRate, LocalDate businessDate) {
        JsonObject body = new JsonObject();
        body.addProperty("nominalAnnualInterestRate", interestRate.toPlainString());
        addFineractDate(body, "expectedActivationDate", businessDate);
        addLocaleAndFormat(body);
        fineractRestClient.put("/savingsaccounts/" + savingsId, body);
    }

    private Optional<String> validatePersonAndProduct(Person person, Long productId) {
        if (person == null || person.getId() == null) {
            return Optional.of("Client is not loaded.");
        }
        if (productId == null) {
            return Optional.of("Select a product.");
        }
        if (!fineractIntegrationService.isEnabled()) {
            return Optional.of("Fineract integration is disabled.");
        }
        if (!fineractIntegrationService.findClientIdByPerson(person).isPresent()) {
            return Optional.of("Client is not linked in Fineract yet. Use Sync to Fineract first.");
        }
        return Optional.empty();
    }

    private long requireClientId(Person person) {
        return fineractIntegrationService.findClientIdByPerson(person)
                                         .orElseThrow(() -> new FineractException("Fineract client not found"));
    }

    private LocalDate resolveBusinessDate(long clientId) {
        LocalDate date = LocalDate.now();
        try {
            JsonObject client = fineractRestClient.get("/clients/" + clientId);
            JsonObject timeline = objectValue(client, "timeline");
            if (timeline != null) {
                LocalDate activation = parseFineractDate(timeline.get("activatedOnDate"));
                if (activation != null && date.isBefore(activation)) {
                    return activation;
                }
            }
        } catch (Exception e) {
            LOG.debug("Could not read Fineract client activation date: {}", e.getMessage());
        }
        return date;
    }

    private static List<FineractProductOption> mapProductOptions(JsonArray items, String nameField) {
        List<FineractProductOption> options = new ArrayList<>();
        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            Long id = longValue(item, "id");
            String name = textValue(item, nameField);
            if (id != null && name != null) {
                options.add(new FineractProductOption(id, name));
            }
        }
        return options;
    }

    private static long resourceId(JsonObject response) {
        if (response.has("resourceId")) {
            return response.get("resourceId").getAsLong();
        }
        if (response.has("loanId")) {
            return response.get("loanId").getAsLong();
        }
        if (response.has("savingsId")) {
            return response.get("savingsId").getAsLong();
        }
        throw new FineractException("Fineract response has no resource id: " + response);
    }

    private static void addFineractDate(JsonObject body, String field, LocalDate date) {
        body.addProperty(field, date.format(FINERACT_DATE));
        addLocaleAndFormat(body);
    }

    private static void addLocaleAndFormat(JsonObject body) {
        if (!body.has("locale")) {
            body.addProperty("locale", "en");
        }
        if (!body.has("dateFormat")) {
            body.addProperty("dateFormat", "dd MMMM yyyy");
        }
    }

    private static JsonArray pageItems(JsonObject response) {
        if (response == null) {
            return new JsonArray();
        }
        if (response.has("pageItems") && response.get("pageItems").isJsonArray()) {
            return response.getAsJsonArray("pageItems");
        }
        if (response.has("value") && response.get("value").isJsonArray()) {
            return response.getAsJsonArray("value");
        }
        return new JsonArray();
    }

    private static void copyEnumIdProperty(JsonObject source, JsonObject target, String field) {
        Integer value = enumIdValue(source, field, null);
        if (value != null) {
            target.addProperty(field, value);
        }
    }

    private static Integer enumIdValue(JsonObject item, String field, Integer defaultValue) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return defaultValue;
        }
        JsonElement element = item.get(field);
        if (element.isJsonObject() && element.getAsJsonObject().has("id")) {
            return element.getAsJsonObject().get("id").getAsInt();
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    private static int intValue(JsonObject item, String field, int defaultValue) {
        Integer value = enumIdValue(item, field, defaultValue);
        return value == null ? defaultValue : value;
    }

    private static BigDecimal decimalValue(JsonObject item, String field, BigDecimal defaultValue) {
        if (item == null || !item.has(field) || item.get(field).isJsonNull()) {
            return defaultValue;
        }
        return item.get(field).getAsBigDecimal();
    }

    private static void copyNumberProperty(JsonObject source, JsonObject target, String field) {
        if (source.has(field) && !source.get(field).isJsonNull()) {
            target.addProperty(field, source.get(field).getAsNumber());
        }
    }

    private static JsonObject objectValue(JsonObject item, String field) {
        if (item == null || !item.has(field) || !item.get(field).isJsonObject()) {
            return null;
        }
        return item.getAsJsonObject(field);
    }

    private static String nestedCode(JsonObject item, String field) {
        JsonObject nested = objectValue(item, field);
        return nested == null ? null : textValue(nested, "code");
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

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private Optional<String> failure(String operation, Exception e) {
        LOG.warn("Fineract {} failed: {}", operation, e.getMessage(), e);
        if (e instanceof FineractException) {
            return Optional.of("Fineract: " + e.getMessage());
        }
        return Optional.of("Fineract operation failed. Check server log for details.");
    }
}
