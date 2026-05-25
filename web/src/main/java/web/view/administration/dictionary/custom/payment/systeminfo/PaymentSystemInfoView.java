/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.systeminfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import web.entity.dict.Country;
import web.entity.dict.PaymentSystem;
import web.entity.dict.Region;
import web.entity.dict.Region_;
import web.repository.dict.CountryRepository;
import web.repository.dict.PaymentPointRepository;
import web.repository.dict.RegionRepository;
import web.view.converter.AutoCompletePojoConverter;

@Getter
@Setter
@Log4j2
public class PaymentSystemInfoView implements Serializable {

    @Autowired
    private PaymentPointRepository paymentPointRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private RegionRepository regionRepository;

    private RegionFilter regionFilter;

    private RegionModel regionModel;

    private PaymentPointFilter paymentPointFilter;

    private PaymentPointModel paymentPointModel;

    private PaymentSystem paymentSystem;

    private List<Country> countries;

    private AutoCompletePojoConverter<Region> converter =
            new AutoCompletePojoConverter<>(Collections.emptyList(), region -> String.valueOf(region.getId()));

    public void init(RegionModel regionModel, PaymentPointModel paymentPointModel, PaymentSystem paymentSystem) {
        this.paymentSystem = paymentSystem;
        countries = countryRepository.findAllByOrderByNameAsc();
        regionFilter = new RegionFilter();
        regionFilter.setPaymentSystem(paymentSystem);
        this.regionModel = regionModel;
        this.regionModel.setFilter(regionFilter.clone());
        paymentPointFilter = new PaymentPointFilter();
        paymentPointFilter.setPaymentSystem(paymentSystem);
        this.paymentPointModel = paymentPointModel;
        this.paymentPointModel.setFilter(paymentPointFilter.clone());
    }

    private void updateRegionFilter() {
        regionModel.setFilter(regionFilter.clone());
        regionModel.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("content:tab:regions")).reset();
    }

    public void extendedRegionSearch() {
        regionFilter.setExtendedSearch(true);
        updateRegionFilter();
    }

    public void fastRegionSearch() {
        regionFilter.setExtendedSearch(false);
        updateRegionFilter();
    }

    private void updatePaymentPointFilter() {
        paymentPointModel.setSelected(null);
        paymentPointModel.setFilter(paymentPointFilter.clone());
        paymentPointModel.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:tab:paymentPoints")).reset();
    }

    public void extendedPaymentPointSearch() {
        paymentPointFilter.setExtendedSearch(true);
        updatePaymentPointFilter();
    }

    public void fastPaymentPointSearch() {
        paymentPointFilter.setExtendedSearch(false);
        updatePaymentPointFilter();
    }

    public Collection<Region> completeRegion(String name) {
        converter.setSource(regionRepository.findAll((root, query, cb) -> {
            Predicate predicateRegion = cb.like(cb.upper(root.get(Region_.name)), String.format("%s%%", name.toUpperCase()));
            return Objects.nonNull(paymentPointFilter.getCountry()) ?
                   cb.and(predicateRegion, cb.equal(root.get(Region_.country), paymentPointFilter.getCountry())) : predicateRegion;
        }, new Sort(Region_.name.getName())));
        return converter.getSource();
    }
}
