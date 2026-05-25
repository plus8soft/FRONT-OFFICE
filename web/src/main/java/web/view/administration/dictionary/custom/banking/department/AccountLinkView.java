/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.department;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.dict.Account;
import web.entity.dict.Currency;
import web.entity.dict.DictionaryParameter;
import web.repository.back.BackException;
import web.repository.dict.CurrencyRepository;
import web.service.administration.department.DepartmentService;
import web.service.dict.AccountLinkService;
import web.session.UserSession;
import web.view.Message;
import web.view.administration.auditlogs.XmlStreamedContentProducer;
import web.view.administration.dictionary.custom.banking.account.AccountFilter;

@Getter
@Setter
@Log4j2
public class AccountLinkView implements Message, Serializable, XmlStreamedContentProducer {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AccountLinkService accountLinkService;

    @Autowired
    private UserSession userSession;

    private AccountFilter filter;

    private AccountLinkModel model;

    private List<Department> departments;

    private List<Currency> currencies;

    private DictionaryParameter dictionaryParameter;

    private List<Account> accounts;

    private List<ErrorInformation> errorInformations;

    private String accountType;

    @Transactional
    public void init(AccountLinkModel accountLinkModel, DictionaryParameter dictionaryParameter) {
        this.dictionaryParameter = dictionaryParameter;
        departments = departmentService.getDepartmentFlatTree();
        currencies = currencyRepository.findAll();
        filter = new AccountFilter();
        model = accountLinkModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:accountLinks")).reset();
    }

    public void updateInformationBack() {
        try {
            accounts = accountLinkService.getBackAccounts(userSession.getUser().getLogin(), filter);
            errorInformations = checkAccounts(accounts);
        } catch (BackException e) {
            log.error(e.getMessage(), e);
            addErrorMessage(e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("error", true);
        }
    }

    private List<ErrorInformation> checkAccounts(List<Account> accounts) {
        List<Account> wrongAccounts =
                accounts.stream().filter(account -> currencies.stream().noneMatch(currency -> currency.getId().equals(account.getCurrency().getId())))
                        .collect(Collectors.toList());
        List<ErrorInformation> errorInformations = new ArrayList<>();
        wrongAccounts.forEach(account -> errorInformations
                .add(new ErrorInformation(account.getId(), "Currency not found", account.getCurrency().getIso(), account.getCurrency().getId())));
        accounts.removeAll(wrongAccounts);
        return errorInformations;
    }

    public void makeChanges() {
        try {
            accountLinkService.update(filter, accounts);
            addInfoMessage(dictionaryParameter.getName() + " updated successfully");
            updateFilter();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }

    @Override
    public String generateXml() {
        StringBuilder xmlText = new StringBuilder();
        xmlText.append("<Data_Loading_Errors>");
        errorInformations.forEach(item -> {
            xmlText.append("<Error>");
            xmlText.append("<Account_Number>").append(item.getAccountNumber()).append("</Account_Number>");
            xmlText.append("<Message>").append(item.getMessage()).append("</Message>");
            xmlText.append("<Object>").append(item.getObjectName()).append("</Object>");
            xmlText.append("<Object_ID>").append(item.getObjectId()).append("</Object_ID>");
            xmlText.append("</Error>");
        });
        return xmlText.append("</Data_Loading_Errors>").toString();
    }

    public StreamedContent fileDownload() {
        return fileDownload(
                LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()).format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) +
                "_error_update_back.xml");
    }

    public void selectAccountType() {
        if (Objects.nonNull(accountType)) {
            filter.setNumberAccount(
                    Objects.nonNull(filter.getNumberAccount()) ? accountType.concat(filter.getNumberAccount().substring(5)) : accountType);
        }
    }

    public void changeAccountNumber() {
        accountType =
                Objects.nonNull(filter.getNumberAccount()) && filter.getNumberAccount().length() > 4 ? filter.getNumberAccount().substring(0, 5) :
                null;
    }
}
