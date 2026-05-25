/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.repository.core.DictionaryRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class StandardDictionaryView implements Message, Serializable {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    private StandardDictionaryModel model;

    private StandardDictionaryFilter filter;

    @Transactional
    public void init(StandardDictionaryModel standardDictionaryModel) {
        filter.setName(null);
        model = standardDictionaryModel;
        model.setFilter(filter.clone());
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:dictionaries")).reset();
    }

    public List<String> completeGroup(String group) {
        return dictionaryRepository.findDictionaryByGroup(group);
    }
}
