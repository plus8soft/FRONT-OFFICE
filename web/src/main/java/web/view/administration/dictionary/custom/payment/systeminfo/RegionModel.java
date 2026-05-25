/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.systeminfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import web.entity.dict.Country_;
import web.entity.dict.PaymentPoint_;
import web.entity.dict.Region;
import web.entity.dict.Region_;
import web.repository.dict.RegionRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
public class RegionModel extends AbstractVirtualScrollLazyModel<Region, Long> {

    @Autowired
    private RegionRepository regionRepository;

    private RegionFilter filter;

    @Override
    protected Function<Region, Long> keyFunction() {
        return Region::getId;
    }

    @Override
    protected long count() {
        return regionRepository.count(getSpecification());
    }

    @Override
    public List<Region> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return regionRepository.findAll((root, query, cb) -> {
            root.fetch(Region_.country);
            query.orderBy(cb.asc(root.get(Region_.country).get(Country_.name)), cb.asc(root.get(Region_.name)));
            return getSpecification().toPredicate(root, query, cb);
        }, first, pageSize, new Sort(Region_.name.getName()));
    }

    private Specification<Region> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.join(Region_.paymentPoints).get(PaymentPoint_.paymentSystem), filter.getPaymentSystem()));
            if (filter.getName() != null) {
                predicates.add(cb.like(cb.upper(root.get(Region_.name)), String.format("%s%%", filter.getName().toUpperCase())));
            }
            if (filter.isExtendedSearch()) {
                if (filter.getStatus() != null) {
                    predicates.add(cb.equal(root.get(Region_.enabled), filter.getStatus()));
                }
                if (filter.getCountry() != null) {
                    predicates.add(cb.equal(root.get(Region_.country), filter.getCountry()));
                }
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
