/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.country;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.CountryRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class CountryView implements Message, Serializable {

    @Autowired
    private CountryRepository countryRepository;

    private DictionaryParameter dictionary;

    private CountryFilter filter;

    private CountryModel model;

    public void init(CountryModel countryModel, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        model = countryModel;
        model.setFilter(filter.clone());
    }

    public void delete() {
        try {
            countryRepository.delete(model.getSelected());
            model.setSelected(null);
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:countries")).loadLazyData();
            addInfoMessage("Data deleted successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while deleting data.");
        }
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:countries")).reset();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }
}
