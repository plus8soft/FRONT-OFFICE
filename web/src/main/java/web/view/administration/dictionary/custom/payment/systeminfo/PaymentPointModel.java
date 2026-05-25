/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.systeminfo;

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
import org.springframework.data.jpa.domain.Specification;
import web.entity.dict.Country_;
import web.entity.dict.PaymentPoint;
import web.entity.dict.PaymentPoint_;
import web.repository.dict.PaymentPointRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class PaymentPointModel extends AbstractVirtualScrollLazyModel<PaymentPoint, Long> {

    @Autowired
    private PaymentPointRepository paymentPointRepository;

    private PaymentPointFilter filter;

    private PaymentPoint selected;

    @Override
    protected Function<PaymentPoint, Long> keyFunction() {
        return PaymentPoint::getId;
    }

    @Override
    protected long count() {
        return paymentPointRepository.count(getSpecification());
    }

    @Override
    public List<PaymentPoint> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return paymentPointRepository.findAll((root, query, cb) -> {
            root.fetch(PaymentPoint_.country);
            query.orderBy(cb.asc(root.get(PaymentPoint_.country).get(Country_.name)), cb.asc(root.get(PaymentPoint_.name)));
            return getSpecification().toPredicate(root, query, cb);
        }, first, pageSize);
    }

    private Specification<PaymentPoint> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(PaymentPoint_.paymentSystem), filter.getPaymentSystem()));
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(PaymentPoint_.name)), String.format("%%%s%%", filter.getName().toUpperCase())));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getPaymentSystem())) {
                    predicates.add(cb.equal(root.get(PaymentPoint_.paymentSystem), filter.getPaymentSystem()));
                }
                if (Objects.nonNull(filter.getAddress())) {
                    predicates.add(cb.like(cb.upper(root.get(PaymentPoint_.address)), String.format("%%%s%%", filter.getAddress().toUpperCase())));
                }
                if (Objects.nonNull(filter.getCountry())) {
                    predicates.add(cb.equal(root.get(PaymentPoint_.country), filter.getCountry()));
                }
                if (Objects.nonNull(filter.getRegion())) {
                    predicates.add(cb.equal(root.get(PaymentPoint_.region), filter.getRegion()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
