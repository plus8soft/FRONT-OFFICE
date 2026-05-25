/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.tariff;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.PaymentSystem;
import web.repository.dict.PaymentSystemRepository;

@Getter
@Setter
@Log4j2
public class PaymentTariffView implements Serializable {

    @Autowired
    private PaymentSystemRepository paymentSystemRepository;

    private List<PaymentSystem> paymentSystems;

    private PaymentTariffFilter filter;

    private PaymentTariffModel model;

    private DictionaryParameter dictionary;

    public void init(PaymentTariffModel paymentTariffModel, DictionaryParameter dictionary) {
        paymentSystems = paymentSystemRepository.findAll();
        this.dictionary = dictionary;
        filter = new PaymentTariffFilter();
        model = paymentTariffModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:paymentTariffs")).reset();
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
