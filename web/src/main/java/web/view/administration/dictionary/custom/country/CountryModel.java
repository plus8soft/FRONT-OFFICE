/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.country;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import web.entity.dict.Country;
import web.entity.dict.Country_;
import web.repository.dict.CountryRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class CountryModel extends AbstractVirtualScrollLazyModel<Country, String> {

    @Autowired
    private CountryRepository countryRepository;

    private CountryFilter filter;

    private Country selected;

    @Override
    protected Function<Country, String> keyFunction() {
        return Country::getId;
    }

    @Override
    protected long count() {
        return countryRepository.count(getSpecification());
    }

    @Override
    public List<Country> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return countryRepository.findAll(getSpecification(), first, pageSize, new Sort(Country_.name.getName()));
    }

    private Specification<Country> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(Country_.name)), String.format("%s%%", filter.getName().toUpperCase())));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getStatus())) {
                    predicates.add(cb.equal(root.get(Country_.enabled), filter.getStatus()));
                }
                if (Objects.nonNull(filter.getAlpha2())) {
                    predicates.add(cb.like(root.get(Country_.alpha2), String.format("%s%%", filter.getAlpha2().toUpperCase())));
                }
                if (Objects.nonNull(filter.getAlpha3())) {
                    predicates.add(cb.like(root.get(Country_.alpha3), String.format("%s%%", filter.getAlpha3().toUpperCase())));
                }
                if (Objects.nonNull(filter.getIso())) {
                    predicates.add(cb.like(root.get(Country_.id), String.format("%s%%", filter.getIso())));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
