/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.bank;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.BankRepository;

@Getter
@Setter
@Log4j2
public class BankView implements Serializable {

    @Autowired
    private BankRepository bankRepository;

    private BankFilter filter;

    private BankModel model;

    private DictionaryParameter dictionary;

    @Transactional
    public void init(BankModel bankModel, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        model = bankModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:banks")).reset();
    }

    public List<String> completeName(String name) {
        return bankRepository.findBankByName(name);
    }
}
