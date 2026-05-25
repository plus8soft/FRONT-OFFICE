/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payaction;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.PayActionRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class PayActionView implements Message, Serializable {

    @Autowired
    private PayActionRepository payActionRepository;

    private DictionaryParameter dictionary;

    private PayActionFilter filter;

    private PayActionModel model;

    public void init(PayActionModel model, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        this.model = model;
        this.model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:pay-actions")).reset();
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
            payActionRepository.delete(model.getSelected());
            model.setSelected(null);
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:pay-actions")).loadLazyData();
            addInfoMessage("Data saved successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }
}
