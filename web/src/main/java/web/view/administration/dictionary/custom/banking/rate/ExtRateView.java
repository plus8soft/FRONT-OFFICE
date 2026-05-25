/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.rate;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.Currency;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.CurrencyRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class ExtRateView implements Message, Serializable {

    @Autowired
    private CurrencyRepository currencyRepository;

    private ExtRateFilter filter;

    private ExtRateModel model;

    private DictionaryParameter dictionary;

    private List<Currency> currencies;

    public void init(ExtRateModel model, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        filter = new ExtRateFilter();
        this.model = model;
        this.model.setFilter(filter.clone());
        currencies = currencyRepository.findAll();
    }

    public void updateFilter() {
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:extRates")).reset();
    }
}
