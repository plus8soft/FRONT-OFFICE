/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.elements;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.dictionary.DictionaryValueCache;
import web.entity.core.Dictionary;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class DictionaryValueView implements Message, Serializable {

    @Autowired
    private DictionaryValueCache dictionaryValueCache;

    private DictionaryValueModel model;

    private DictionaryValueFilter filter;

    @Transactional
    public void init(DictionaryValueModel dictionaryValueModel, Dictionary dictionary) {
        filter = new DictionaryValueFilter();
        filter.setDictionary(dictionary);
        model = dictionaryValueModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:items")).reset();
    }

    public void delete() {
        dictionaryValueCache.delete(model.getSelected());
        model.setSelected(null);
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:items")).loadLazyData();
    }
}
