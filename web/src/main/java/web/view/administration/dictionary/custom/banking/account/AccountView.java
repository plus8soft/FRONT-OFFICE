/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.account;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.dict.Currency;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.AccountRepository;
import web.repository.dict.CurrencyRepository;
import web.service.administration.department.DepartmentService;

@Getter
@Setter
@Log4j2
public class AccountView implements Serializable {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AccountRepository accountRepository;

    private AccountFilter filter;

    private AccountModel model;

    private List<Department> departments;

    private List<Currency> currencies;

    private String accountType;

    private DictionaryParameter dictionary;

    @Transactional
    public void init(AccountModel accountModel, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        departments = departmentService.getDepartmentFlatTree();
        currencies = currencyRepository.findAll();
        filter = new AccountFilter();
        model = accountModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:accounts")).reset();
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
