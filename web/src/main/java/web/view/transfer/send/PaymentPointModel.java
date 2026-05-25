/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
        return paymentPointRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<PaymentPoint> getSpecification() {
        return (Root<PaymentPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(PaymentPoint_.paymentSystem), filter.getPaymentSystem()));
            predicates.add(cb.isMember(filter.getCurrency(), root.get(PaymentPoint_.currencies)));
            predicates.add(cb.equal(root.get(PaymentPoint_.region), filter.getRegion()));
            predicates.add(cb.equal(root.get(PaymentPoint_.country), filter.getCountry()));
            if (Objects.nonNull(filter.getAddress())) {
                predicates.add(cb.like(cb.upper(root.get(PaymentPoint_.address)), String.format("%%%s%%", filter.getAddress().toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
