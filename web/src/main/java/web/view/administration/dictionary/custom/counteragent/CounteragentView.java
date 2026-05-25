/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.Bank;
import web.entity.dict.Bank_;
import web.entity.dict.CounteragentPayAction;
import web.entity.dict.CounteragentPayAction_;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.BankRepository;
import web.repository.dict.CounteragentPayActionRepository;
import web.repository.dict.CounteragentRepository;
import web.view.Message;
import web.view.converter.AutoCompletePojoConverter;

@Getter
@Setter
@Log4j2
public class CounteragentView implements Message, Serializable {

    @Autowired
    private CounteragentRepository counteragentRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CounteragentPayActionRepository counteragentPayActionRepository;

    private DictionaryParameter dictionary;

    private CounteragentFilter filter;

    private CounteragentModel model;

    private Bank bank;

    private List<CounteragentPayAction> payActions;

    private AutoCompletePojoConverter<Bank> converter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), bank -> String.valueOf(bank.getId()));

    public void init(CounteragentModel model, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        this.model = model;
        this.model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    public void delete() {
        try {
            counteragentRepository.delete(model.getSelected());
            model.setSelected(null);
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:counteragents")).loadLazyData();
            addInfoMessage("Data saved successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }

    public Collection<Bank> completeName(String nameOrRoutingNumber) {
        converter.setSource(bankRepository.findAll((root, query, cb) -> cb
                .or(cb.like(root.get(Bank_.name), "%" + nameOrRoutingNumber + "%"), cb.like(root.get(Bank_.routingNumber), "%" + nameOrRoutingNumber + "%"))));
        return converter.getSource();
    }

    public void select() {
        bank = bankRepository.findOne((root, query, cb) -> cb.equal(root.get(Bank_.routingNumber), model.getSelected().getRoutingNumber()));
        payActions = counteragentPayActionRepository
                .findAll((root, query, cb) -> cb.equal(root.get(CounteragentPayAction_.counteragent), model.getSelected()));
    }
}
